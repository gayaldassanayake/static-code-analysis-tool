/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.scan.internal;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.scan.Issue;
import io.ballerina.scan.PlatformPluginContext;
import io.ballerina.scan.Rule;
import io.ballerina.scan.StaticCodeAnalysisPlatformPlugin;
import io.ballerina.scan.utils.DiagnosticCode;
import io.ballerina.scan.utils.DiagnosticLog;
import io.ballerina.scan.utils.ScanTomlFile;
import io.ballerina.scan.utils.ScanToolException;
import io.ballerina.scan.utils.ScanUtils;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * {@code ScanRunner} is responsible for executing the core scan logic.
 *
 * @since 0.10.0
 */
public class ScanRunner {
    private final PrintStream outputStream;
    private final Project project;
    private final boolean platformTriggered;
    private final String targetDir;
    private final boolean scanReport;
    private final boolean listRules;
    private final List<String> includeRules;
    private final List<String> excludeRules;
    private final List<String> platforms;

    private final List<Rule> allRules = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();

    ScanRunner(ScanOptions options) {
        this.outputStream = options.outputStream();
        this.project = options.project();
        this.platformTriggered = options.platformTriggered();
        this.targetDir = options.targetDir();
        this.scanReport = options.scanReport();
        this.listRules = options.listRules();
        this.includeRules = new ArrayList<>(options.includeRules());
        this.excludeRules = new ArrayList<>(options.excludeRules());
        this.platforms = new ArrayList<>(options.platforms());
    }

    void execute() {
        if (ProjectUtils.isProjectEmpty(project)) {
            outputStream.println(DiagnosticLog.error(DiagnosticCode.EMPTY_PACKAGE));
            return;
        }

        Optional<ScanTomlFile> scanTomlFile = ScanUtils.loadScanTomlConfigurations(project, outputStream);
        if (scanTomlFile.isEmpty()) {
            return;
        }

        ProjectAnalyzer projectAnalyzer = getProjectAnalyzer(project, scanTomlFile.get());
        List<Rule> coreRules = CoreRule.rules();
        Map<String, List<Rule>> externalAnalyzers;
        try {
            externalAnalyzers = projectAnalyzer.getExternalAnalyzers();
        } catch (RuntimeException ex) {
            outputStream.println(ex.getMessage());
            return;
        }

        allRules.addAll(coreRules);
        externalAnalyzers.values().forEach(allRules::addAll);
        if (listRules) {
            ScanUtils.printRulesToConsole(allRules, outputStream);
            return;
        }

        List<String> externalJarFilePaths = new ArrayList<>();
        Map<String, PlatformPluginContext> platformContexts = new HashMap<>();
        scanTomlFile.get().getPlatforms().forEach(platform -> {
            String platformName = platform.name();
            externalJarFilePaths.add(platform.path());
            Map<String, String> platformArgs = new HashMap<>();
            platform.arguments().forEach((key, value) -> platformArgs.put(key, value.toString()));
            platformContexts.put(platformName, new PlatformPluginContextImpl(platformArgs, platformTriggered));
            if (!platformTriggered || platforms.size() != 1 || !platforms.contains(platformName)) {
                platforms.add(platformName);
            }
        });

        URLClassLoader ucl = loadPlatformPlugins(externalJarFilePaths);
        ServiceLoader<StaticCodeAnalysisPlatformPlugin> scannerPlatformPlugins = ServiceLoader.load(
                StaticCodeAnalysisPlatformPlugin.class, ucl);
        scannerPlatformPlugins.forEach(staticCodeAnalysisPlatformPlugin -> {
            if (platforms.contains(staticCodeAnalysisPlatformPlugin.platform())) {
                PlatformPluginContext platformPluginContext = platformContexts.get(staticCodeAnalysisPlatformPlugin
                        .platform());
                staticCodeAnalysisPlatformPlugin.init(platformPluginContext);
            }
        });

        scanTomlFile.get().getRulesToInclude().stream().map(ScanTomlFile.RuleToFilter::id).forEach(includeRules::add);
        scanTomlFile.get().getRulesToExclude().stream().map(ScanTomlFile.RuleToFilter::id).forEach(excludeRules::add);
        if (!includeRules.isEmpty() && !excludeRules.isEmpty()) {
            outputStream.println(DiagnosticLog.error(DiagnosticCode.ATTEMPT_TO_INCLUDE_AND_EXCLUDE));
            return;
        }

        outputStream.println();
        outputStream.println("Running Scans");

        issues.addAll(projectAnalyzer.analyze(coreRules));
        issues.addAll(projectAnalyzer.runExternalAnalyzers(externalAnalyzers));

        if (!includeRules.isEmpty()) {
            issues.removeIf(issue -> !includeRules.contains(issue.rule().id()));
        }
        if (!excludeRules.isEmpty()) {
            issues.removeIf(issue -> excludeRules.contains(issue.rule().id()));
        }

        if (platforms.isEmpty() && !platformTriggered) {
            ScanUtils.printToConsole(issues, outputStream);
            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                Path reportPath = ScanUtils.saveToDirectory(issues, project, targetDir);
                outputStream.println();
                outputStream.println("View scan results at:");
                outputStream.println("\t" + reportPath.toUri() + System.lineSeparator());
                if (scanReport) {
                    Path scanReportPath = ScanUtils.generateScanReport(issues, project, targetDir);
                    outputStream.println();
                    outputStream.println("View scan report at:");
                    outputStream.println("\t" + scanReportPath.toUri() + System.lineSeparator());
                }
            } else {
                if (targetDir != null) {
                    outputStream.println();
                    outputStream.println(DiagnosticLog.warning(DiagnosticCode.REPORT_NOT_SUPPORTED));
                }

                if (scanReport) {
                    outputStream.println();
                    outputStream.println(DiagnosticLog.warning(DiagnosticCode.SCAN_REPORT_NOT_SUPPORTED));
                }
            }
        }

        scannerPlatformPlugins.forEach(staticCodeAnalysisPlatformPlugin -> {
            if (platforms.contains(staticCodeAnalysisPlatformPlugin.platform())) {
                outputStream.println("Reporting issues to: " + staticCodeAnalysisPlatformPlugin.platform());
                staticCodeAnalysisPlatformPlugin.onScan(issues);
                platforms.removeAll(Collections.singleton(staticCodeAnalysisPlatformPlugin.platform()));
            }
        });
        platforms.forEach(remainingPlatform -> {
            outputStream.println();
            outputStream.println("The specified platform '" + remainingPlatform + "' is not available.");
            outputStream.println("Please ensure that the required platform plugin path is specified in 'Scan.toml'.");
        });
    }

    protected ProjectAnalyzer getProjectAnalyzer(Project project, ScanTomlFile scanTomlFile) {
        return new ProjectAnalyzer(project, scanTomlFile);
    }

    private URLClassLoader loadPlatformPlugins(List<String> jarPaths) {
        List<URL> jarUrls = new ArrayList<>(jarPaths.size());
        jarPaths.forEach(jarPath -> {
            try {
                URL jarUrl = Path.of(jarPath).toUri().toURL();
                jarUrls.add(jarUrl);
            } catch (MalformedURLException ex) {
                throw new ScanToolException(ex.getMessage());
            }
        });
        return new URLClassLoader(jarUrls.toArray(URL[]::new), this.getClass().getClassLoader());
    }

    List<Issue> getIssues() {
        return issues;
    }
}
