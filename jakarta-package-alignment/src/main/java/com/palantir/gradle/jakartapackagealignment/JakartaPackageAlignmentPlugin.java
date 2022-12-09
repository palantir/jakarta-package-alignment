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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class JakartaPackageAlignmentPlugin implements Plugin<Project> {
    @Override
    public final void apply(Project project) {
        SourceSetContainer sourceSets = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSets != null) {
            sourceSets.configureEach(sourceSet -> {
                configureAllConfigurationsForSourceSet(project, sourceSet);
            });
        }
    }

    private void configureAllConfigurationsForSourceSet(Project project, SourceSet sourceSet) {
        // see: https://docs.gradle.org/current/userguide/java_plugin.html#java_source_set_configurations
        Set<String> configurationsToConfigure = ImmutableSet.of(
                sourceSet.getCompileClasspathConfigurationName(),
                sourceSet.getRuntimeClasspathConfigurationName(),
                sourceSet.getAnnotationProcessorConfigurationName());

        project.getConfigurations().configureEach(configuration -> {
            if (configurationsToConfigure.contains(configuration.getName())) {
                configureConfigurationForSubstitution(configuration);
            }
        });
    }

    private void configureConfigurationForSubstitution(Configuration configuration) {
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
    }
}
