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

import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolConfig;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.scan.Issue;
import io.ballerina.scan.RuleKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.scan.internal.ScanToolConstants.DiagnosticMessages.OPTION_TYPE_MISMATCH;
import static io.ballerina.scan.internal.ScanToolConstants.EXCLUDE_RULES;
import static io.ballerina.scan.internal.ScanToolConstants.INCLUDE_RULES;
import static io.ballerina.scan.internal.ScanToolConstants.LIST_RULES;
import static io.ballerina.scan.internal.ScanToolConstants.PLATFORMS;
import static io.ballerina.scan.internal.ScanToolConstants.PLATFORM_TRIGGERED;
import static io.ballerina.scan.internal.ScanToolConstants.SCAN_COMMAND;
import static io.ballerina.scan.internal.ScanToolConstants.SCAN_REPORT;

/**
 * {@code ScanBuildTool} is the entry point for the scan build tool.
 *
 * @since 0.10.0
 */
@ToolConfig(name = SCAN_COMMAND)
public class ScanBuildTool implements CodeGeneratorTool {

    @Override
    public void execute(ToolContext toolContext) {
        ScanOptions scanOptions = extractScanOptions(toolContext);
        ScanRunner scanRunner = new ScanRunner(scanOptions);
        scanRunner.execute();
        addDiagnostics(scanRunner.getIssues(), toolContext);
    }

    private ScanOptions extractScanOptions(ToolContext toolContext) {
        return ScanOptions.builder()
                .setOutputStream(System.out)
                .setProject(toolContext.currentPackage().project())
                .setPlatformTriggered(getBooleanOption(PLATFORM_TRIGGERED, toolContext))
                .setTargetDir(toolContext.cachePath().toAbsolutePath().toString())
                .setScanReport(getBooleanOption(SCAN_REPORT, toolContext))
                .setListRules(getBooleanOption(LIST_RULES, toolContext))
                .setIncludeRules(getStringArrayOption(INCLUDE_RULES, toolContext))
                .setExcludeRules(getStringArrayOption(EXCLUDE_RULES, toolContext))
                .setPlatforms(getStringArrayOption(PLATFORMS, toolContext))
                .build();
    }

    private void addDiagnostics(List<Issue> issues, ToolContext toolContext) {
        issues.forEach(issue -> {
            RuleKind ruleKind = issue.rule().kind();
            ScanToolConstants.DiagnosticMessages diagnosticMessage = switch (ruleKind) {
                case CODE_SMELL -> ScanToolConstants.DiagnosticMessages.ISSUE_CODE_SMELL;
                case BUG -> ScanToolConstants.DiagnosticMessages.ISSUE_BUG;
                case VULNERABILITY -> ScanToolConstants.DiagnosticMessages.ISSUE_VULNERABILITY;
            };
            String message = issue.rule().id() + ": " + issue.rule().description();
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    diagnosticMessage.getCode(),
                    message,
                    diagnosticMessage.getSeverity());
            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, issue.location());
            toolContext.reportDiagnostic(diagnostic);
        });
    }

    private boolean getBooleanOption(String optionName, ToolContext toolContext) {
        boolean option = false;
        Map<String, ToolContext.Option> toolContextOptions = toolContext.options();
        if (toolContextOptions.containsKey(optionName)) {
            Object value = toolContextOptions.get(optionName).value();
            if (value instanceof Boolean) {
                option = ((Boolean) value);
            } else {
                addInvalidOptionTomlDiagnostic(optionName, toolContext);
            }
        }
        return option;
    }

    private List<String> getStringArrayOption(String optionName, ToolContext toolContext) {
        List<String> option = new ArrayList<>();
        Map<String, ToolContext.Option> toolContextOptions = toolContext.options();
        if (toolContextOptions.containsKey(optionName)) {
            Object value = toolContextOptions.get(optionName).value();
            if (value instanceof List<?> list) {
                boolean allStrings = true;
                    for (Object item : list) {
                        if (item instanceof String) {
                            option.add((String) item);
                        } else {
                            allStrings = false;
                            break;
                        }
                    }
                    if (!allStrings) {
                        option.clear();
                        addInvalidOptionTomlDiagnostic(optionName, toolContext);
                    }
            } else {
                addInvalidOptionTomlDiagnostic(optionName, toolContext);
            }
        }
        return option;
    }

    private void addInvalidOptionTomlDiagnostic(String optionName, ToolContext toolContext) {
        ToolContext.Option toolContextOption = toolContext.options().get(optionName);
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                OPTION_TYPE_MISMATCH.getCode(),
                OPTION_TYPE_MISMATCH.getDescription(),
                OPTION_TYPE_MISMATCH.getSeverity());
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, toolContextOption.location());
        toolContext.reportDiagnostic(diagnostic);
    }

}
