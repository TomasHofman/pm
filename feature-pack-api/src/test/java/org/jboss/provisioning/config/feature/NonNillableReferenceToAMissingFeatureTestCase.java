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

package org.jboss.provisioning.config.feature;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.ProvisioningManager;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.spec.ConfigSpec;
import org.jboss.provisioning.spec.FeatureParameterSpec;
import org.jboss.provisioning.spec.FeatureReferenceSpec;
import org.jboss.provisioning.spec.FeatureSpec;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.test.PmInstallFeaturePackTestBase;
import org.jboss.provisioning.test.util.fs.state.DirState;
import org.jboss.provisioning.test.util.fs.state.DirState.DirBuilder;
import org.jboss.provisioning.test.util.repomanager.FeaturePackRepoManager;
import org.junit.Assert;

/**
 *
 * @author Alexey Loubyansky
 */
public class NonNillableReferenceToAMissingFeatureTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepoManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("a", "aOne"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("b", false))
                    .addParam(FeatureParameterSpec.create("a", true))
                    .addRef(FeatureReferenceSpec.builder("specA")
                            .setName("specA")
                            .setNillable(false)
                            .mapParam("a", "name")
                            .build())
                    .build())
            .addConfig(ConfigSpec.builder().setName("config1")
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "b")
                            .setParam("a", "a"))
                    .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() throws ProvisioningDescriptionException {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected void testPmMethod(ProvisioningManager pm) throws ProvisioningException {
        try {
            super.testPmMethod(pm);
            Assert.fail("There should be an unsatisfied reference");
        } catch(ProvisioningException e) {
            Assert.assertEquals("Failed to build config named config1", e.getMessage());
            e = (ProvisioningException) e.getCause();
            Assert.assertNotNull(e);
            Assert.assertEquals("org.jboss.pm.test:fp1:1.0.0.Final#specB:name=b has unresolved dependency on org.jboss.pm.test:fp1:1.0.0.Final#specA:name=a", e.getMessage());
        }
    }

    @Override
    protected void testRecordedProvisioningConfig(final ProvisioningManager pm) throws ProvisioningException {
        assertProvisioningConfig(pm, null);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }

    @Override
    protected DirState provisionedHomeDir(DirBuilder builder) {
        return builder.clear().build();
    }
}
