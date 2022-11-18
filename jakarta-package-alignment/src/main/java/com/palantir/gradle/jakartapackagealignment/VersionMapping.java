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

import org.apache.maven.artifact.versioning.ComparableVersion;

final class VersionMapping {
    private final String jakartaGroupId;
    private final String jakartaArtifactId;
    private final ComparableVersion maxJakartaVersionWithJavaxNamespace;
    private final String mappedJavaeeCoord;

    VersionMapping(MavenCoordinate jakartaCoord, MavenCoordinate javaeeCoord) {
        this.jakartaGroupId = jakartaCoord.getGroupId();
        this.jakartaArtifactId = jakartaCoord.getArtifactId();
        this.maxJakartaVersionWithJavaxNamespace = new ComparableVersion(jakartaCoord.getVersion());
        this.mappedJavaeeCoord = javaeeCoord.toString();
    }

    String getJakartaGroupId() {
        return jakartaGroupId;
    }

    String getJakartaArtifactId() {
        return jakartaArtifactId;
    }

    ComparableVersion getMaxJakartaVersionWithJavaxNamespace() {
        return maxJakartaVersionWithJavaxNamespace;
    }

    String getMappedJavaeeCoord() {
        return mappedJavaeeCoord;
    }
}
