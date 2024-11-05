/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.invalid.ruleformat;

import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.scan.ScannerContext;

/**
 * Represents a compiler plugin for a custom module.
 *
 * @since 0.1.0
 * */
public class CustomStaticCodeAnalyzer extends CompilerPlugin {
    @Override
    public void init(CompilerPluginContext compilerPluginContext) {
        Object context = compilerPluginContext.userData().get("ScannerContext");
        if (context != null) {
            ScannerContext scannerContext = (ScannerContext) context;
            compilerPluginContext.addCodeAnalyzer(new CustomCodeAnalyzer(scannerContext));
        }
    }
}