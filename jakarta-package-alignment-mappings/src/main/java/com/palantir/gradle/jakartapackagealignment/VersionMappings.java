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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.artifact.versioning.ComparableVersion;

public final class VersionMappings {

    private static final Map<String, VersionMapping> mappings = createMappings();
    private static final ComparableVersion MINIMUM_VERSION_BOUND = new ComparableVersion("0.0.1");

    private VersionMappings() {}

    public static Optional<MavenCoordinate> getReplacement(String group, String name, String version) {
        String key = group + ":" + name;
        VersionMapping mapping = mappings.get(key);

        if (mapping != null) {
            ComparableVersion requestedVersion = new ComparableVersion(version);
            ComparableVersion maxJakartaVersionWithJavaxNamespace =
                    new ComparableVersion(mapping.getJakartaCoord().getVersion());

            // https://github.com/palantir/jakarta-package-alignment/issues/25
            // In some cases, getReplacement may be called with a weird version like "." or an empty string
            // ComparableVersion behaves a bit strangely in these cases, so to ensure we're checking a valid
            // version range, also compare it with an arbitrary minimum bound which we know does not exist
            // for any jakarta package. This ensures that we don't accidentally return an incorrect replacement
            // rule in these cases which can hose up dependency resolution.
            if (requestedVersion.compareTo(MINIMUM_VERSION_BOUND) > 0
                    && requestedVersion.compareTo(maxJakartaVersionWithJavaxNamespace) <= 0) {
                return Optional.of(mapping.getMappedJavaeeCoord());
            }
        }

        return Optional.empty();
    }

    public static Collection<VersionMapping> getMappings() {
        return mappings.values();
    }

    @SuppressWarnings("checkstyle:methodlength")
    private static Map<String, VersionMapping> createMappings() {
        Map<String, VersionMapping> allMappings = new HashMap<>();
        addMapping(
                allMappings,
                "com.sun.activation:jakarta.activation",
                new VersionMapping(
                        new MavenCoordinate("com.sun.activation", "jakarta.activation", "1.2.2"),
                        new MavenCoordinate("com.sun.activation", "javax.activation", "1.2.0")));
        addMapping(
                allMappings,
                "com.sun.mail:jakarta.mail",
                new VersionMapping(
                        new MavenCoordinate("com.sun.mail", "jakarta.mail", "1.6.7"),
                        new MavenCoordinate("com.sun.mail", "javax.mail", "1.6.2")));
        addMapping(
                allMappings,
                "jakarta.activation:jakarta.activation-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.activation", "jakarta.activation-api", "1.2.2"),
                        new MavenCoordinate("javax.activation", "javax.activation-api", "1.2.0")));
        addMapping(
                allMappings,
                "jakarta.annotation:jakarta.annotation-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.annotation", "jakarta.annotation-api", "1.3.5"),
                        new MavenCoordinate("javax.annotation", "javax.annotation-api", "1.3.2")));
        addMapping(
                allMappings,
                "jakarta.batch:jakarta.batch-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.batch", "jakarta.batch-api", "1.0.2"),
                        new MavenCoordinate("javax.batch", "javax.batch-api", "1.0.1")));
        addMapping(
                allMappings,
                "jakarta.ejb:jakarta.ejb-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.ejb", "jakarta.ejb-api", "3.2.6"),
                        new MavenCoordinate("javax.ejb", "javax.ejb-api", "3.2.2")));
        addMapping(
                allMappings,
                "jakarta.el:jakarta.el-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.el", "jakarta.el-api", "3.0.3"),
                        new MavenCoordinate("javax.el", "javax.el-api", "3.0.0")));
        addMapping(
                allMappings,
                "jakarta.enterprise.concurrent:jakarta.enterprise.concurrent-api",
                new VersionMapping(
                        new MavenCoordinate(
                                "jakarta.enterprise.concurrent", "jakarta.enterprise.concurrent-api", "1.1.2"),
                        new MavenCoordinate("javax.enterprise.concurrent", "javax.enterprise.concurrent-api", "1.1")));
        addMapping(
                allMappings,
                "jakarta.faces:jakarta.faces-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.faces", "jakarta.faces-api", "2.3.2"),
                        new MavenCoordinate("javax.faces", "javax.faces-api", "2.3")));
        addMapping(
                allMappings,
                "jakarta.inject:jakarta.inject-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.inject", "jakarta.inject-api", "1.0.5"),
                        new MavenCoordinate("javax.inject", "javax.inject", "1")));
        addMapping(
                allMappings,
                "jakarta.interceptor:jakarta.interceptor-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.interceptor", "jakarta.interceptor-api", "1.2.5"),
                        new MavenCoordinate("javax.interceptor", "javax.interceptor-api", "1.2.2")));
        addMapping(
                allMappings,
                "jakarta.jms:jakarta.jms-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.jms", "jakarta.jms-api", "2.0.3"),
                        new MavenCoordinate("javax.jms", "javax.jms-api", "2.0.1")));
        addMapping(
                allMappings,
                "jakarta.json.bind:jakarta.json.bind-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.json.bind", "jakarta.json.bind-api", "1.0.2"),
                        new MavenCoordinate("javax.json.bind", "javax.json.bind-api", "1.0")));
        addMapping(
                allMappings,
                "jakarta.json:jakarta.json-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.json", "jakarta.json-api", "1.1.6"),
                        new MavenCoordinate("javax.json", "javax.json-api", "1.1.4")));
        addMapping(
                allMappings,
                "jakarta.jws:jakarta.jws-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.jws", "jakarta.jws-api", "2.1.0"),
                        new MavenCoordinate("javax.jws", "javax.jws-api", "1.1")));
        addMapping(
                allMappings,
                "jakarta.mail:jakarta.mail-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.mail", "jakarta.mail-api", "1.6.7"),
                        new MavenCoordinate("javax.mail", "javax.mail-api", "1.6.2")));
        addMapping(
                allMappings,
                "jakarta.persistence:jakarta.persistence-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.persistence", "jakarta.persistence-api", "2.2.3"),
                        new MavenCoordinate("javax.persistence", "javax.persistence-api", "2.2")));
        addMapping(
                allMappings,
                "jakarta.resource:jakarta.resource-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.resource", "jakarta.resource-api", "1.7.4"),
                        new MavenCoordinate("javax.resource", "javax.resource-api", "1.7.1")));
        addMapping(
                allMappings,
                "jakarta.security.enterprise:jakarta.security.enterprise-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.security.enterprise", "jakarta.security.enterprise-api", "1.0.2"),
                        new MavenCoordinate("javax.security.enterprise", "javax.security.enterprise-api", "1.0")));
        addMapping(
                allMappings,
                "jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.servlet.jsp.jstl", "jakarta.servlet.jsp.jstl-api", "1.2.7"),
                        new MavenCoordinate("javax.servlet.jsp.jstl", "javax.servlet.jsp.jstl-api", "1.2.2")));
        addMapping(
                allMappings,
                "jakarta.servlet.jsp:jakarta.servlet.jsp-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.servlet.jsp", "jakarta.servlet.jsp-api", "2.3.6"),
                        new MavenCoordinate("javax.servlet.jsp", "javax.servlet.jsp-api", "2.3.3")));
        addMapping(
                allMappings,
                "jakarta.servlet:jakarta.servlet-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.servlet", "jakarta.servlet-api", "4.0.4"),
                        new MavenCoordinate("javax.servlet", "javax.servlet-api", "4.0.1")));
        addMapping(
                allMappings,
                "jakarta.transaction:jakarta.transaction-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.transaction", "jakarta.transaction-api", "1.3.3"),
                        new MavenCoordinate("javax.transaction", "javax.transaction-api", "1.3")));
        addMapping(
                allMappings,
                "jakarta.validation:jakarta.validation-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.validation", "jakarta.validation-api", "2.0.2"),
                        new MavenCoordinate("javax.validation", "validation-api", "2.0.1.Final")));
        addMapping(
                allMappings,
                "jakarta.websocket:jakarta.websocket-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.websocket", "jakarta.websocket-api", "1.1.2"),
                        new MavenCoordinate("javax.websocket", "javax.websocket-api", "1.1")));
        addMapping(
                allMappings,
                "jakarta.ws.rs:jakarta.ws.rs-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.ws.rs", "jakarta.ws.rs-api", "2.1.6"),
                        new MavenCoordinate("javax.ws.rs", "javax.ws.rs-api", "2.1.1")));
        addMapping(
                allMappings,
                "jakarta.xml.bind:jakarta.xml.bind-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.xml.bind", "jakarta.xml.bind-api", "2.3.3"),
                        new MavenCoordinate("javax.xml.bind", "jaxb-api", "2.3.1")));
        addMapping(
                allMappings,
                "jakarta.xml.soap:jakarta.xml.soap-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.xml.soap", "jakarta.xml.soap-api", "1.4.2"),
                        new MavenCoordinate("javax.xml.soap", "javax.xml.soap-api", "1.4.0")));
        addMapping(
                allMappings,
                "jakarta.xml.ws:jakarta.xml.ws-api",
                new VersionMapping(
                        new MavenCoordinate("jakarta.xml.ws", "jakarta.xml.ws-api", "2.3.3"),
                        new MavenCoordinate("javax.xml.ws", "jaxws-api", "2.3.1")));

        // glassfish hk2 repackages some components as an OSGi bundle
        // we assume no OSGi usage in conjunction with this plugin, keeping the hk2 dependency
        // around could lead to conflicting class names on the classpath
        addMapping(
                allMappings,
                "org.glassfish.hk2.external:jakarta.inject",
                new VersionMapping(
                        new MavenCoordinate("org.glassfish.hk2.external", "jakarta.inject", "2.6.1"),
                        new MavenCoordinate("javax.inject", "javax.inject", "1")));
        addMapping(
                allMappings,
                "org.glassfish.hk2.external:javax.inject",
                new VersionMapping(
                        new MavenCoordinate("org.glassfish.hk2.external", "javax.inject", "2.4.0"),
                        new MavenCoordinate("javax.inject", "javax.inject", "1")));

        return allMappings;
    }

    private static void addMapping(Map<String, VersionMapping> map, String key, VersionMapping value) {
        if (map.putIfAbsent(key, value) != null) {
            throw new IllegalArgumentException("duplicate key: " + key);
        }
    }
}
