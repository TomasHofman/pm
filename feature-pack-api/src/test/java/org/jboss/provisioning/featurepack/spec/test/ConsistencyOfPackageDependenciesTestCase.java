/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.jboss.provisioning.featurepack.spec.test;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.spec.FeaturePackSpec;
import org.jboss.provisioning.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConsistencyOfPackageDependenciesTestCase {

    @Test
    public void testInvalidRequiredDependency() throws Exception {

        final FeaturePackSpec.Builder builder = FeaturePackSpec
                .builder(ArtifactCoords.newGav("g", "a", "v"))
                .addDefaultPackage(
                        PackageSpec.builder("p1")
                        .addDependency("p2")
                        .build())
                .addPackage(
                        PackageSpec.builder("p2")
                        .addDependency("p3")
                        .build())
                .addPackage(
                        PackageSpec.builder("p3")
                        .addDependency("p4")
                        .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            // expected
        }
    }

    @Test
    public void testInvalidOptionalDependency() throws Exception {

        final FeaturePackSpec.Builder builder = FeaturePackSpec
                .builder(ArtifactCoords.newGav("g", "a", "v"))
                .addDefaultPackage(
                        PackageSpec.builder("p1")
                        .addDependency("p2", true)
                        .build())
                .addPackage(
                        PackageSpec.builder("p2")
                        .addDependency("p3", true)
                        .build())
                .addPackage(
                        PackageSpec.builder("p3")
                        .addDependency("p4", true)
                        .build());

        try {
            builder.build();
            Assert.fail("Cannot build feature-pack description with inconsistent package dependencies.");
        } catch (ProvisioningDescriptionException e) {
            // expected
        }
    }
}