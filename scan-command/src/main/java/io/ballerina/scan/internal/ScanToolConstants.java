/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.scan.internal;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * {@code ScanToolConstants} contains the constant variables used within the Ballerina scan tool.
 *
 * @since 0.1.0
 */
public class ScanToolConstants {
    static final String SCAN_COMMAND = "gayal_scan";
    static final String BALLERINA_RULE_PREFIX = "ballerina:";
    static final String BALLERINA_ORG = "ballerina";
    static final String BALLERINAI_ORG = "ballerinai";
    static final String BALLERINAX_ORG = "ballerinax";
    static final String USE_IMPORT_AS_UNDERSCORE = " as _;";
    static final String IMPORT_GENERATOR_FILE = "scan_file";
    static final String RULES_FILE = "rules.json";
    static final String RULE_KIND = "kind";
    static final String BUG = "BUG";
    static final String VULNERABILITY = "VULNERABILITY";
    static final String CODE_SMELL = "CODE_SMELL";
    static final String RULE_ID = "id";
    static final String RULE_DESCRIPTION = "description";

    public static final String SCANNER_CONTEXT = "ScannerContext";
    public static final String FORWARD_SLASH = "/";

    // Scan build tool options
    public static final String PLATFORM_TRIGGERED = "platformTriggered";
    public static final String SCAN_REPORT = "scanReport";
    public static final String LIST_RULES = "listRules";
    public static final String INCLUDE_RULES = "includeRules";
    public static final String EXCLUDE_RULES = "excludeRules";
    public static final String PLATFORMS = "platforms";

    public enum DiagnosticMessages {
        ISSUE_CODE_SMELL("SCAN_101", DiagnosticSeverity.WARNING),
        ISSUE_BUG("SCAN_102", DiagnosticSeverity.WARNING),
        ISSUE_VULNERABILITY("SCAN_103", DiagnosticSeverity.WARNING),

        OPTION_TYPE_MISMATCH("SCAN_201", "expected and actual option types do not match.", DiagnosticSeverity.ERROR);

        private final String code;
        private final String description;
        private final DiagnosticSeverity severity;

        DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
            this.code = code;
            this.description = description;
            this.severity = severity;
        }

        DiagnosticMessages(String code, DiagnosticSeverity severity) {
            this.code = code;
            this.description = "";
            this.severity = severity;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public DiagnosticSeverity getSeverity() {
            return severity;
        }
    }

    private ScanToolConstants() {
    }
}
