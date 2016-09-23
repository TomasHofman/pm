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
package org.jboss.provisioning.plugin.wildfly.featurepack.model;


import java.util.Map;

import org.jboss.provisioning.xml.util.AttributeValue;
import org.jboss.provisioning.xml.util.ElementNode;

import static org.jboss.provisioning.plugin.wildfly.featurepack.model.ConfigModelParser20.Attribute;
import static org.jboss.provisioning.plugin.wildfly.featurepack.model.ConfigModelParser20.Element;

/**
 * Writes a {@link org.jboss.provisioning.plugin.wildfly.featurepack.model.ConfigDescription} as XML.
 *
 * @author Eduardo Martins
 * @author Alexey Loubyansky
 */
public class ConfigXmlWriter20 {

    public static final ConfigXmlWriter20 INSTANCE = new ConfigXmlWriter20();

    private ConfigXmlWriter20() {
    }

    public void write(ConfigDescription config, ElementNode parentElementNode) {
        if (!config.getStandaloneConfigFiles().isEmpty() || !config.getDomainConfigFiles().isEmpty()) {
            final ElementNode configElementNode = new ElementNode(parentElementNode, ConfigModelParser20.ELEMENT_LOCAL_NAME);
            for (ConfigFileDescription configFile : config.getStandaloneConfigFiles()) {
                final ElementNode standaloneElementNode = new ElementNode(parentElementNode, Element.STANDALONE.getLocalName());
                writeConfigFile(configFile, standaloneElementNode);
                configElementNode.addChild(standaloneElementNode);
            }
            for (ConfigFileDescription configFile : config.getDomainConfigFiles()) {
                final ElementNode domainElementNode = new ElementNode(parentElementNode, Element.DOMAIN.getLocalName());
                writeConfigFile(configFile, domainElementNode);
                configElementNode.addChild(domainElementNode);
            }
            for (ConfigFileDescription configFile : config.getHostConfigFiles()) {
                final ElementNode hostElementNode = new ElementNode(parentElementNode, Element.HOST.getLocalName());
                writeConfigFile(configFile, hostElementNode);
                configElementNode.addChild(hostElementNode);
            }
            parentElementNode.addChild(configElementNode);
        }
    }

    protected void writeConfigFile(ConfigFileDescription configFile, ElementNode configElementNode) {
        for (Map.Entry<String, String> property : configFile.getProperties().entrySet()) {
            ElementNode propertyElementNode = new ElementNode(configElementNode, Element.PROPERTY.getLocalName());
            propertyElementNode.addAttribute(Attribute.NAME.getLocalName(), new AttributeValue(property.getKey()));
            propertyElementNode.addAttribute(Attribute.VALUE.getLocalName(), new AttributeValue(property.getValue()));
            configElementNode.addChild(propertyElementNode);
        }
        configElementNode.addAttribute(Attribute.TEMPLATE.getLocalName(), new AttributeValue(configFile.getTemplate()));
        configElementNode.addAttribute(Attribute.SUBSYSTEMS.getLocalName(), new AttributeValue(configFile.getSubsystems()));
        configElementNode.addAttribute(Attribute.OUTPUT_FILE.getLocalName(), new AttributeValue(configFile.getOutputFile()));
    }

}