/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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

package org.jboss.provisioning.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.provisioning.util.PmCollections;
import org.jboss.provisioning.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigCustomizations {

    protected final boolean inheritConfigs;
    protected final boolean inheritModelOnlyConfigs;
    protected final Set<String> includedModels;
    protected final Map<String, Boolean> excludedModels;
    protected final Set<ConfigId> includedConfigs;
    protected final Map<String, Set<String>> excludedConfigs;
    protected final List<ConfigModel> definedConfigs;
    protected final boolean hasModelOnlyConfigs;

    protected ConfigCustomizations(ConfigCustomizationsBuilder<?> builder) {
        this.inheritConfigs = builder.inheritConfigs;
        this.inheritModelOnlyConfigs = builder.inheritModelOnlyConfigs;
        this.includedModels = PmCollections.unmodifiable(builder.includedModels);
        this.excludedModels = PmCollections.unmodifiable(builder.excludedModels);
        this.includedConfigs = PmCollections.unmodifiable(builder.includedConfigs);
        this.excludedConfigs = PmCollections.unmodifiable(builder.excludedConfigs);
        this.definedConfigs = PmCollections.unmodifiable(builder.definedConfigs);
        this.hasModelOnlyConfigs = builder.hasModelOnlyConfigs;
    }

    public boolean isInheritConfigs() {
        return inheritConfigs;
    }

    public boolean isInheritModelOnlyConfigs() {
        return inheritModelOnlyConfigs;
    }

    public boolean hasFullModelsIncluded() {
        return !includedModels.isEmpty();
    }

    public Set<String> getFullModelsIncluded() {
        return includedModels;
    }

    public boolean isConfigModelIncluded(ConfigId configId) {
        return includedModels.contains(configId.getModel());
    }

    public boolean hasFullModelsExcluded() {
        return !excludedModels.isEmpty();
    }

    public Map<String, Boolean> getFullModelsExcluded() {
        return excludedModels;
    }

    public boolean isConfigModelExcluded(ConfigId configId) {
        final Boolean namedOnly = excludedModels.get(configId.getModel());
        if(namedOnly == null) {
            return false;
        }
        return namedOnly ? configId.getName() != null : true;
    }

    public boolean hasExcludedConfigs() {
        return !excludedConfigs.isEmpty();
    }

    public boolean isConfigExcluded(ConfigId configId) {
        final Set<String> names = excludedConfigs.get(configId.getModel());
        return names == null ? false : names.contains(configId.getName());
    }

    public Set<String> getExcludedModels() {
        return excludedConfigs.keySet();
    }

    public Set<String> getExcludedConfigs(String model) {
        return excludedConfigs.get(model);
    }

    public boolean hasIncludedConfigs() {
        return !includedConfigs.isEmpty();
    }

    public boolean isConfigIncluded(ConfigId id) {
        return includedConfigs.contains(id);
    }

    public Set<ConfigId> getIncludedConfigs() {
        return includedConfigs;
    }

    public boolean hasDefinedConfigs() {
        return !definedConfigs.isEmpty();
    }

    public Collection<ConfigModel> getDefinedConfigs() {
        return definedConfigs;
    }

    public boolean hasModelOnlyConfigs() {
        return hasModelOnlyConfigs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definedConfigs == null) ? 0 : definedConfigs.hashCode());
        result = prime * result + ((excludedConfigs == null) ? 0 : excludedConfigs.hashCode());
        result = prime * result + ((excludedModels == null) ? 0 : excludedModels.hashCode());
        result = prime * result + ((includedConfigs == null) ? 0 : includedConfigs.hashCode());
        result = prime * result + ((includedModels == null) ? 0 : includedModels.hashCode());
        result = prime * result + (inheritConfigs ? 1231 : 1237);
        result = prime * result + (inheritModelOnlyConfigs ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigCustomizations other = (ConfigCustomizations) obj;
        if (definedConfigs == null) {
            if (other.definedConfigs != null)
                return false;
        } else if (!definedConfigs.equals(other.definedConfigs))
            return false;
        if (excludedConfigs == null) {
            if (other.excludedConfigs != null)
                return false;
        } else if (!excludedConfigs.equals(other.excludedConfigs))
            return false;
        if (excludedModels == null) {
            if (other.excludedModels != null)
                return false;
        } else if (!excludedModels.equals(other.excludedModels))
            return false;
        if (includedConfigs == null) {
            if (other.includedConfigs != null)
                return false;
        } else if (!includedConfigs.equals(other.includedConfigs))
            return false;
        if (includedModels == null) {
            if (other.includedModels != null)
                return false;
        } else if (!includedModels.equals(other.includedModels))
            return false;
        if (inheritConfigs != other.inheritConfigs)
            return false;
        if (inheritModelOnlyConfigs != other.inheritModelOnlyConfigs)
            return false;
        return true;
    }

    protected void append(StringBuilder builder) {
        if(!inheritConfigs) {
            builder.append(" inheritConfigs=false");
        }
        if(!inheritModelOnlyConfigs) {
            builder.append(" inheritModelOnlyConfigs=false");
        }
        if(!this.excludedModels.isEmpty()) {
            builder.append(" excluded models ");
            StringUtils.append(builder, excludedModels.entrySet());
        }
        if(!excludedConfigs.isEmpty()) {
            builder.append(" excluded configs ");
            StringUtils.append(builder, excludedConfigs.entrySet());
        }
        if(!includedConfigs.isEmpty()) {
            builder.append(" included configs ");
            StringUtils.append(builder, includedConfigs);
        }
        if(!definedConfigs.isEmpty()) {
            builder.append(" defined configs ");
            StringUtils.append(builder, definedConfigs);
        }
    }
}