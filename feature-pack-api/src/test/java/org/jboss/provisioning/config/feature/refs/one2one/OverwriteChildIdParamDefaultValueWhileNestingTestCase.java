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

package org.jboss.provisioning.config.feature.refs.one2one;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeatureGroupConfig;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.runtime.ResolvedFeatureId;
import org.jboss.provisioning.spec.ConfigSpec;
import org.jboss.provisioning.spec.FeatureGroupSpec;
import org.jboss.provisioning.spec.FeatureParameterSpec;
import org.jboss.provisioning.spec.FeatureReferenceSpec;
import org.jboss.provisioning.spec.FeatureSpec;
import org.jboss.provisioning.state.ProvisionedFeaturePack;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.test.PmInstallFeaturePackTestBase;
import org.jboss.provisioning.xml.ProvisionedConfigBuilder;
import org.jboss.provisioning.xml.ProvisionedFeatureBuilder;

/**
 * An id param may have a default value but it can be initialized to a different one.
 * It cannot be overwritten afterwards.
 *
 * @author Alexey Loubyansky
 */
public class OverwriteChildIdParamDefaultValueWhileNestingTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("p1", "spec"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addFeatureRef(FeatureReferenceSpec.builder("specA").mapParam("a", "id").build())
                    .addParam(FeatureParameterSpec.createId("id"))
                    .addParam(FeatureParameterSpec.create("a", true, false, "def"))
                    .build())
            .addFeatureGroup(FeatureGroupSpec.builder("groupC")
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("id", "c1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("id", "c2"))
                    .build())
            .addConfig(ConfigSpec.builder()
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("id", "a1")
                            .addFeatureGroup(FeatureGroupConfig.forGroup("groupC")))
                    .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.forGav(FP_GAV))
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "id", "a1"))
                                .setConfigParam("p1", "spec")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP_GAV, "specC").setParam("a", "a1").setParam("id", "c1").build())
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.builder(FP_GAV, "specC").setParam("a", "a1").setParam("id", "c2").build())
                                .build())
                        .build())
                .build();
    }
}
