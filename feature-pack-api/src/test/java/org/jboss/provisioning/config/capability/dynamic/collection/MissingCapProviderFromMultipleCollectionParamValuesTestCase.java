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

package org.jboss.provisioning.config.capability.dynamic.collection;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.Errors;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.spec.ConfigSpec;
import org.jboss.provisioning.spec.FeatureParameterSpec;
import org.jboss.provisioning.spec.FeatureSpec;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.test.PmInstallFeaturePackTestBase;

/**
 *
 * @author Alexey Loubyansky
 */
public class MissingCapProviderFromMultipleCollectionParamValuesTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");


    @Override
    protected void setupRepo(FeaturePackRepositoryManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .providesCapability("$a.$p")
                    .addParam(FeatureParameterSpec.createId("a"))
                    .addParam(FeatureParameterSpec.createId("p"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .requiresCapability("$p1.$p2")
                    .addParam(FeatureParameterSpec.createId("b"))
                    .addParam(FeatureParameterSpec.builder("p1").setType("List<String>").build())
                    .addParam(FeatureParameterSpec.builder("p2").setType("List<String>").build())
                    .build())
            .addConfig(ConfigSpec.builder()
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("b", "b1")
                            .setParam("p1", "[ 1 , 2 ]")
                            .setParam("p2", "[a,b]"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "1")
                            .setParam("p", "a"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "1")
                            .setParam("p", "b"))
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("a", "2")
                            .setParam("p", "a"))
                    .build())
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected String[] pmErrors() {
        return new String[] {
                Errors.failedToBuildConfigSpec(null, null),
                "No provider found for capability 2.b required by org.jboss.pm.test:fp1:1.0.0.Final#specB:b=b1 as $p1.$p2"
        };
    }
    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return null;
    }
}
