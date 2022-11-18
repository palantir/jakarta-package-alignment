/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.gradle.jakartapackagealignment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ModuleComponentSelector;

public class JakartaPackageAlignmentPlugin implements Plugin<Project> {
    @Override
    public final void apply(Project project) {
        project.getConfigurations().configureEach(configuration -> {
            configuration.getResolutionStrategy().getDependencySubstitution().all(dep -> {
                if (dep.getRequested() instanceof ModuleComponentSelector) {
                    ModuleComponentSelector selector = (ModuleComponentSelector) dep.getRequested();
                    VersionMappings.getReplacement(selector)
                            .ifPresent(replacement -> dep.useTarget(
                                    replacement,
                                    "forced to Java EE 8 dependency because the requested Jakarta "
                                            + "dependency is < Jakarta EE 9"));
                }
            });
        });
    }
}
