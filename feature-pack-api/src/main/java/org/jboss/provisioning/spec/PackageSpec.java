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
package org.jboss.provisioning.spec;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.provisioning.parameters.BuilderWithParameters;
import org.jboss.provisioning.parameters.PackageParameter;
import org.jboss.provisioning.util.DescrFormatter;

/**
 * This class describes a package as it appears in a feature-pack specification.
 *
 * @author Alexey Loubyansky
 */
public class PackageSpec implements PackageDependencies {

    public static PackageSpec forName(String name) {
        return new PackageSpec(name);
    }

    public static class Builder implements BuilderWithParameters<Builder> {

        private String name;
        private PackageDependencyGroupSpec.Builder localDeps;
        private Map<String, PackageDependencyGroupSpec.Builder> externalDeps = Collections.emptyMap();
        private Map<String, PackageParameter> params = Collections.emptyMap();

        protected Builder() {
            this(null);
        }

        protected Builder(String name) {
            this.name = name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addDependency(String packageName) {
            getLocalGroupBuilder().addDependency(packageName);
            return this;
        }

        public Builder addDependency(String packageName, boolean optional) {
            getLocalGroupBuilder().addDependency(packageName, optional);
            return this;
        }

        public Builder addDependency(PackageDependencySpec dep) {
            getLocalGroupBuilder().addDependency(dep);
            return this;
        }

        public Builder addDependency(String groupName, String packageName) {
            getExternalGroupBuilder(groupName).addDependency(packageName);
            return this;
        }

        public Builder addDependency(String groupName, String packageName, boolean optional) {
            getExternalGroupBuilder(groupName).addDependency(packageName, optional);
            return this;
        }

        public Builder addDependency(String groupName, PackageDependencySpec dep) {
            getExternalGroupBuilder(groupName).addDependency(dep);
            return this;
        }

        public boolean hasDependencies() {
            return localDeps != null || !externalDeps.isEmpty();
        }

        @Override
        public Builder addParameter(PackageParameter param) {
            switch(params.size()) {
                case 0:
                    params = Collections.singletonMap(param.getName(), param);
                    break;
                case 1:
                    params = new HashMap<>(params);
                default:
                    params.put(param.getName(), param);
            }
            return this;
        }

        private PackageDependencyGroupSpec.Builder getLocalGroupBuilder() {
            if(localDeps == null) {
                localDeps = PackageDependencyGroupSpec.builder();
            }
            return localDeps;
        }

        private PackageDependencyGroupSpec.Builder getExternalGroupBuilder(String groupName) {
            PackageDependencyGroupSpec.Builder groupBuilder = externalDeps.get(groupName);
            if(groupBuilder == null) {
                groupBuilder = PackageDependencyGroupSpec.builder(groupName);
                switch(externalDeps.size()) {
                    case 0:
                        externalDeps = Collections.singletonMap(groupName, groupBuilder);
                        break;
                    case 1:
                        externalDeps = new LinkedHashMap<>(externalDeps);
                    default:
                        externalDeps.put(groupName, groupBuilder);
                }
            }
            return groupBuilder;
        }

        public PackageSpec build() {
            return new PackageSpec(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private final String name;
    private final PackageDependencyGroupSpec localDeps;
    private final Map<String, PackageDependencyGroupSpec> externalDeps;
    private final Map<String, PackageParameter> params;

    protected PackageSpec(String name) {
        this.name = name;
        localDeps = PackageDependencyGroupSpec.EMPTY_LOCAL;
        externalDeps = Collections.emptyMap();
        params = Collections.emptyMap();
    }

    protected PackageSpec(Builder builder) {
        this.name = builder.name;
        this.localDeps = builder.localDeps == null ? PackageDependencyGroupSpec.EMPTY_LOCAL : builder.localDeps.build();
        if(builder.externalDeps.isEmpty()) {
            externalDeps = Collections.emptyMap();
        } else {
            final int size = builder.externalDeps.size();
            if(size == 1) {
                final Map.Entry<String, PackageDependencyGroupSpec.Builder> entry = builder.externalDeps.entrySet().iterator().next();
                externalDeps = Collections.singletonMap(entry.getKey(), entry.getValue().build());
            } else {
                final Map<String, PackageDependencyGroupSpec> deps = new LinkedHashMap<>(size);
                for(Map.Entry<String, PackageDependencyGroupSpec.Builder> entry : builder.externalDeps.entrySet()) {
                    deps.put(entry.getKey(), entry.getValue().build());
                }
                externalDeps = Collections.unmodifiableMap(deps);
            }
        }
        this.params = builder.params.size() > 1 ? Collections.unmodifiableMap(builder.params) : builder.params;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean dependsOnPackages() {
        return !(localDeps.isEmpty() && externalDeps.isEmpty());
    }

    @Override
    public boolean dependsOnLocalPackages() {
        return !localDeps.isEmpty();
    }

    @Override
    public PackageDependencyGroupSpec getLocalPackageDependencies() {
        return localDeps;
    }

    @Override
    public boolean dependsOnExternalPackages() {
        return !externalDeps.isEmpty();
    }

    @Override
    public Collection<String> getPackageDependencySources() {
        return externalDeps.keySet();
    }

    @Override
    public PackageDependencyGroupSpec getExternalPackageDependencies(String groupName) {
        return externalDeps.get(groupName);
    }

    public boolean hasParameters() {
        return !params.isEmpty();
    }

    public boolean hasParameter(String name) {
        return params.containsKey(name);
    }

    public Collection<PackageParameter> getParameters() {
        return params.values();
    }

    void logContent(DescrFormatter logger) throws IOException {
        logger.print("Package ");
        logger.println(name);
        if(!localDeps.isEmpty()) {
            logger.increaseOffset();
            logger.println("Dependencies");
            logger.increaseOffset();
            for(PackageDependencySpec dependency : localDeps.getDescriptions()) {
                logger.println(dependency.toString());
            }
            logger.decreaseOffset();
            logger.decreaseOffset();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalDeps == null) ? 0 : externalDeps.hashCode());
        result = prime * result + ((localDeps == null) ? 0 : localDeps.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        PackageSpec other = (PackageSpec) obj;
        if (externalDeps == null) {
            if (other.externalDeps != null)
                return false;
        } else if (!externalDeps.equals(other.externalDeps))
            return false;
        if (localDeps == null) {
            if (other.localDeps != null)
                return false;
        } else if (!localDeps.equals(other.localDeps))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(name);
        if(!localDeps.isEmpty()) {
            buf.append(" depends on ").append(localDeps);
        }
        if(!externalDeps.isEmpty()) {
            buf.append(", ").append(externalDeps);
        }
        if(!params.isEmpty()) {
            buf.append(" parameters ").append(params);
        }
        buf.append(']');
        return buf.toString();
    }
}
