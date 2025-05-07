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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code ScanOptions} contains the options for the scan runner.
 *
 * @since 0.10.0
 */
public class ScanOptions {
    private final PrintStream outputStream;
    private final Project project;
    private final boolean platformTriggered;
    private final String targetDir;
    private final boolean scanReport;
    private final boolean listRules;
    private final List<String> includeRules;
    private final List<String> excludeRules;
    private final List<String> platforms;

    private ScanOptions(Builder builder) {
        this.outputStream = builder.outputStream;
        this.project = builder.project;
        this.platformTriggered = builder.platformTriggered;
        this.targetDir = builder.targetDir;
        this.scanReport = builder.scanReport;
        this.listRules = builder.listRules;
        this.includeRules = builder.includeRules;
        this.excludeRules = builder.excludeRules;
        this.platforms = builder.platforms;
    }

    static Builder builder() {
        return new Builder();
    }

    PrintStream outputStream() {
        return outputStream;
    }

    Project project() {
        return project;
    }

    boolean platformTriggered() {
        return platformTriggered;
    }

    String targetDir() {
        return targetDir;
    }

    boolean scanReport() {
        return scanReport;
    }

    boolean listRules() {
        return listRules;
    }

    List<String> includeRules() {
        return includeRules;
    }

    List<String> excludeRules() {
        return excludeRules;
    }

    List<String> platforms() {
        return platforms;
    }

    static class Builder {
        private PrintStream outputStream;
        private Project project;
        private boolean platformTriggered;
        private String targetDir;
        private boolean scanReport;
        private boolean listRules;
        private List<String> includeRules = new ArrayList<>();
        private List<String> excludeRules = new ArrayList<>();
        private List<String> platforms = new ArrayList<>();

        private Builder() {
        }

        Builder setOutputStream(PrintStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        Builder setProject(Project project) {
            this.project = project;
            return this;
        }

        Builder setPlatformTriggered(boolean platformTriggered) {
            this.platformTriggered = platformTriggered;
            return this;
        }

        Builder setTargetDir(String targetDir) {
            this.targetDir = targetDir;
            return this;
        }

        Builder setScanReport(boolean scanReport) {
            this.scanReport = scanReport;
            return this;
        }

        Builder setListRules(boolean listRules) {
            this.listRules = listRules;
            return this;
        }

        Builder setIncludeRules(List<String> includeRules) {
            this.includeRules = includeRules;
            return this;
        }

        Builder setExcludeRules(List<String> excludeRules) {
            this.excludeRules = excludeRules;
            return this;
        }

        Builder setPlatforms(List<String> platforms) {
            this.platforms = platforms;
            return this;
        }

        ScanOptions build() {
            return new ScanOptions(this);
        }
    }
}
