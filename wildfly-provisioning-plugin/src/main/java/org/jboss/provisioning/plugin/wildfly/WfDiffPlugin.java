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
package org.jboss.provisioning.plugin.wildfly;


import java.io.File;
import java.nio.file.Path;

import org.jboss.provisioning.MessageWriter;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.diff.FileSystemDiff;
import org.jboss.provisioning.plugin.DiffPlugin;
import org.jboss.provisioning.runtime.ProvisioningRuntime;
import org.jboss.provisioning.util.PathFilter;

/**
 * WildFly plugin to compute the model difference between an instance and a clean provisioned instance.
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class WfDiffPlugin implements DiffPlugin {

    private static final String CONFIGURE_SYNC = "/synchronization=simple:add(host=%s, port=%s, protocol=%s, username=%s, password=%s)";
    private static final String EXPORT_DIFF = "attachment save --overwrite --operation=/synchronization=simple:export-diff --file=%s";

    private static final PathFilter FILTER_FP = PathFilter.Builder.instance()
            .addDirectories("*" + File.separatorChar + "tmp", "*" + File.separatorChar + "log","*_xml_history", "model_diff")
            .addFiles("standalone.xml", "process-uuid", "logging.properties")
            .build();

    private static final PathFilter FILTER = PathFilter.Builder.instance()
            .addDirectories("*" + File.separatorChar + "tmp", "model_diff")
            .addFiles("standalone.xml", "logging.properties")
            .build();

    @Override
    public void computeDiff(ProvisioningRuntime runtime, Path customizedInstallation, Path target) throws ProvisioningException {
        final MessageWriter messageWriter = runtime.getMessageWriter();
        messageWriter.verbose("WildFly diff plug-in");
        FileSystemDiff diff = new FileSystemDiff(messageWriter, runtime.getInstallDir(), customizedInstallation);
        runtime.setDiff(diff.diff(getFilter(runtime)));
    }

    private PathFilter getFilter(ProvisioningRuntime runtime) {
        if("diff-to-feature-pack".equals(runtime.getOperation())) {
            return FILTER_FP;
        }
       return FILTER;
    }
}
