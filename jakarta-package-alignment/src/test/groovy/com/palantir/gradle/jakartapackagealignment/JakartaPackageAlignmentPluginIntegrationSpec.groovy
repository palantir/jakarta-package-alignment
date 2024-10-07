/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.gradle.jakartapackagealignment

import groovy.transform.CompileStatic
import nebula.test.IntegrationSpec
import nebula.test.dependencies.DependencyGraph
import nebula.test.dependencies.GradleDependencyGenerator
import nebula.test.functional.ExecutionResult

/**
 * Integration tests for {@link JakartaPackageAlignmentPlugin}.
 */
class JakartaPackageAlignmentPluginIntegrationSpec extends IntegrationSpec {

    static def PLUGIN_NAME = "com.palantir.jakarta-package-alignment"
    private File mavenRepo
    private File versionsProps

    // borrowed from gcv
    protected File generateMavenRepo(String... graph) {
        DependencyGraph dependencyGraph = new DependencyGraph(graph)
        GradleDependencyGenerator generator = new GradleDependencyGenerator(
                dependencyGraph, new File(projectDir, "build/testrepogen").toString())
        return generator.generateTestMavenRepo()
    }

    void setup() {
        System.setProperty('ignoreDeprecations', 'true')

        // TODO: this could potentially be replaced by something that calls VersionMappings.getReplacement
        // at runtime to keep the jakarta and javax versions consistent, but we'd still need to hardcode versions
        // somewhere in this test code anyway...
        mavenRepo = generateMavenRepo(
                "org:direct-dep:1.0.0 -> org:transitive-dep:1.2.3",
                "org:transitive-dep:1.2.3 -> jakarta.servlet:jakarta.servlet-api:4.0.4",
                "org:direct-dep:2.0.0 -> org:transitive-dep:2.0.0",
                "org:transitive-dep:2.0.0 -> jakarta.servlet:jakarta.servlet-api:5.0.0",
                "jakarta.servlet:jakarta.servlet-api:4.0.4",
                "jakarta.servlet:jakarta.servlet-api:5.0.0",
                "javax.servlet:javax.servlet-api:4.0.1"
        )

        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
                dependencies {
                    classpath 'com.palantir.gradle.consistentversions:gradle-consistent-versions:2.25.0'
                }
            }
            apply plugin: "com.palantir.consistent-versions"
            apply plugin: "${PLUGIN_NAME}"
            apply plugin: "java-library"

            repositories {
                maven { url "file:///${mavenRepo.getAbsolutePath()}" }
            }
        """.stripIndent()

        versionsProps = createFile("versions.props")
    }

    def "forces downgrade of servlet-api when writing version locks"() {
        setup:
        buildFile << """
            dependencies {
                implementation "org:direct-dep"
            }
        """.stripIndent()
        versionsProps << """org:direct-dep = 1.0.0""".stripIndent()

        when:
        runTasks("--write-locks")

        then:
        def versionsLockFile = new File(projectDir, "versions.lock")
        fileExists("versions.lock")
        versionsLockFile.text.contains("javax.servlet:javax.servlet-api:4.0.1")
        !versionsLockFile.text.contains("jakarta.servlet:jakarta.servlet-api")
    }

    def "does not downgrade servlet-api when above known bad version"() {
        setup:
        buildFile << """
            dependencies {
                implementation "org:direct-dep"
            }
        """.stripIndent()
        versionsProps << """org:direct-dep = 2.0.0""".stripIndent()

        when:
        runTasks("--write-locks")

        then:
        def versionsLockFile = new File(projectDir, "versions.lock")
        fileExists("versions.lock")
        versionsLockFile.text.contains("jakarta.servlet:jakarta.servlet-api:5.0.0")
        !versionsLockFile.text.contains("javax.servlet:javax.servlet-api")
    }

    def "leaves usage of acceptable versions untouched, but still replaces bad versions"() {
        setup:
        mavenRepo = generateMavenRepo(
                "org:direct-dep:1.0.0 -> org:transitive-dep:1.2.3",
                "org:transitive-dep:1.2.3 -> jakarta.ws.rs:jakarta.ws.rs-api:2.1.6",
                "org:direct-dep:2.0.0 -> org:transitive-dep:2.0.0",
                "org:transitive-dep:2.0.0 -> jakarta.servlet:jakarta.servlet-api:5.0.0",
                "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0",
                "jakarta.ws.rs:jakarta.ws.rs-api:2.1.6",
                "javax.ws.rs:javax.ws.rs-api:2.1.1"
        )

        buildFile << """
            dependencies {
                implementation "org:direct-dep"
                implementation "jakarta.ws.rs:jakarta.ws.rs-api"
            }
        """.stripIndent()

        // no idea why stripIndent isnt working here, maybe the encoding on this file is weird?
        versionsProps << """org:direct-dep = 1.0.0\njakarta.ws.rs:jakarta.ws.rs-api = 3.1.0""".stripIndent()

        when:
        runTasksSuccessfully("--write-locks")
        ExecutionResult result = runTasksSuccessfully("dependencies", "--configuration", "runtimeClasspath")

        then:
        def versionsLockFile = new File(projectDir, "versions.lock")
        fileExists("versions.lock")
        versionsLockFile.text.contains("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
        versionsLockFile.text.contains("javax.ws.rs:javax.ws.rs-api:2.1.1")
        !versionsLockFile.text.contains("jakarta.ws.rs:jakarta.ws.rs-api:2.1.6")

        result.standardOutput.contains("jakarta.ws.rs:jakarta.ws.rs-api -> 3.1.0")
        result.standardOutput.contains("jakarta.ws.rs:jakarta.ws.rs-api:2.1.6 -> javax.ws.rs:javax.ws.rs-api:2.1.1")
    }
}
