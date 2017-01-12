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
package org.jboss.provisioning;

/**
 *
 * @author Alexey Loubyansky
 */
public interface Constants {

    String FEATURE_PACKS = "featurepacks";
    String PM_INSTALL_DIR = "pm.target.dir";
    String PM_INSTALL_WORK_DIR = "pm.install.workdir";
    String PM_TOOL_HOME_DIR = "pm.tool.home";

    String CONTENT = "content";
    String FEATURE_PACK_XML = "feature-pack.xml";
    String MODULES_XML = "module.xml";
    String PACKAGE_XML = "package.xml";
    String PACKAGES = "packages";
    String PROVISIONING_XML = "provisioning.xml";
    String RESOURCES = "resources";

    String PROVISIONED_STATE_DIR = ".pm";
    String PROVISIONED_STATE_XML = "provisioned.xml";
}