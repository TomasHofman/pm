/*
 * Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.provisioning.wildfly.build;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.provisioning.config.ConfigModel;
import org.jboss.provisioning.spec.FeaturePackDependencySpec;
import org.jboss.provisioning.util.PmCollections;

/**
 * Representation of the feature pack build config
 *
 * @author Stuart Douglas
 * @author Alexey Loubyansky
 */
public class WildFlyFeaturePackBuild {

    public static class Builder {

        private List<FeaturePackDependencySpec> dependencies = Collections.emptyList();
        private Set<String> schemaGroups = Collections.emptySet();
        private Set<String> defaultPackages = Collections.emptySet();
        private List<ConfigModel> configs = Collections.emptyList();

        private Builder() {
        }

        public Builder addDefaultPackage(String packageName) {
            defaultPackages = PmCollections.add(defaultPackages, packageName);
            return this;
        }

        public Builder addDependency(FeaturePackDependencySpec dependency) {
            dependencies = PmCollections.add(dependencies, dependency);
            return this;
        }

        public Builder addSchemaGroup(String groupId) {
            schemaGroups = PmCollections.add(schemaGroups, groupId);
            return this;
        }

        public Builder addConfig(ConfigModel config) {
            configs = PmCollections.add(configs, config);
            return this;
        }

        public WildFlyFeaturePackBuild build() {
            return new WildFlyFeaturePackBuild(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<FeaturePackDependencySpec> dependencies;
    private final Set<String> schemaGroups;
    private final Set<String> defaultPackages;
    private final List<ConfigModel> configs;

    private WildFlyFeaturePackBuild(Builder builder) {
        this.dependencies = PmCollections.unmodifiable(builder.dependencies);
        this.schemaGroups = PmCollections.unmodifiable(builder.schemaGroups);
        this.defaultPackages = PmCollections.unmodifiable(builder.defaultPackages);
        this.configs = PmCollections.unmodifiable(builder.configs);
    }

    public Collection<String> getDefaultPackages() {
        return defaultPackages;
    }

    public List<FeaturePackDependencySpec> getDependencies() {
        return dependencies;
    }

    public boolean hasSchemaGroups() {
        return !schemaGroups.isEmpty();
    }

    public boolean isSchemaGroup(String groupId) {
        return schemaGroups.contains(groupId);
    }

    public Set<String> getSchemaGroups() {
        return schemaGroups;
    }

    public boolean hasConfigs() {
        return !configs.isEmpty();
    }

    public List<ConfigModel> getConfigs() {
        return configs;
    }
}
