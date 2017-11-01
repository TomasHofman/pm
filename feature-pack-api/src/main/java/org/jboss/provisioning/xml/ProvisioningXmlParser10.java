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
package org.jboss.provisioning.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.config.IncludedConfig;
import org.jboss.provisioning.config.FeaturePackConfig.Builder;
import org.jboss.provisioning.config.ProvisioningConfig;
import org.jboss.provisioning.spec.ConfigSpec;
import org.jboss.provisioning.util.ParsingUtils;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisioningXmlParser10 implements PlugableXmlParser<ProvisioningConfig.Builder> {

    public static final String NAMESPACE_1_0 = "urn:wildfly:pm-provisioning:1.0";
    public static final QName ROOT_1_0 = new QName(NAMESPACE_1_0, Element.INSTALLATION.getLocalName());

    enum Element implements XmlNameProvider {

        CONFIG("config"),
        DEFAULT_CONFIGS("default-configs"),
        EXCLUDE("exclude"),
        FEATURE_PACK("feature-pack"),
        INCLUDE("include"),
        INSTALLATION("installation"),
        PACKAGES("packages"),

        // default unknown element
        UNKNOWN(null);


        private static final Map<String, Element> elementsByLocal;

        static {
            elementsByLocal = Arrays.stream(values()).filter(val -> val.name != null)
                    .collect(Collectors.toMap(val -> val.getLocalName(), val -> val));
        }

        static Element of(String localName) {
            final Element element = elementsByLocal.get(localName);
            return element == null ? UNKNOWN : element;
        }

        private final String name;
        private final String namespace = NAMESPACE_1_0;

        Element(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        @Override
        public String getLocalName() {
            return name;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum Attribute implements XmlNameProvider {

        ARTIFACT_ID("artifactId"),
        GROUP_ID("groupId"),
        INHERIT("inherit"),
        INHERIT_FEATURES("inherit-features"),
        MODEL("model"),
        NAME("name"),
        NAMED_CONFIGS_ONLY("named-configs-only"),
        VERSION("version"),

        // default unknown attribute
        UNKNOWN(null);

        private static final Map<QName, Attribute> attributes;

        static {
            attributes = Arrays.stream(values()).filter(val -> val.name != null).collect(Collectors.toMap(val -> new QName(val.getLocalName()), val -> val));
        }

        static Attribute of(QName qName) {
            final Attribute attribute = attributes.get(qName);
            return attribute == null ? UNKNOWN : attribute;
        }

        private final String name;

        Attribute(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        @Override
        public String getLocalName() {
            return name;
        }

        @Override
        public String getNamespace() {
            return null;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    @Override
    public QName getRoot() {
        return ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, ProvisioningConfig.Builder builder) throws XMLStreamException {
        ParsingUtils.parseNoAttributes(reader);
        boolean hasFp = false;
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    if (!hasFp) {
                        throw ParsingUtils.expectedAtLeastOneChild(reader, Element.INSTALLATION, Element.FEATURE_PACK);
                    }
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case FEATURE_PACK:
                            hasFp = true;
                            try {
                                builder.addFeaturePack(readFeaturePack(reader));
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException("Failed to add feature-pack", e);
                            }
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private FeaturePackConfig readFeaturePack(XMLExtendedStreamReader reader) throws XMLStreamException {
        final int count = reader.getAttributeCount();
        String groupId = null;
        String artifactId = null;
        String version = "LATEST";
        for (int i = 0; i < count; i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case GROUP_ID:
                    groupId = reader.getAttributeValue(i);
                    break;
                case ARTIFACT_ID:
                    artifactId = reader.getAttributeValue(i);
                    break;
                case VERSION:
                    version = reader.getAttributeValue(i);
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        if (groupId == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.GROUP_ID));
        }
        if (artifactId == null) {
            throw ParsingUtils.missingAttributes(reader.getLocation(), Collections.singleton(Attribute.ARTIFACT_ID));
        }

        final FeaturePackConfig.Builder fpBuilder = FeaturePackConfig.builder(ArtifactCoords.newGav(groupId, artifactId, version));

        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return fpBuilder.build();
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case DEFAULT_CONFIGS:
                            parseDefaultConfigs(reader, fpBuilder);
                            break;
                        case CONFIG:
                            final ConfigSpec.Builder configBuilder = ConfigSpec.builder().setResetFeaturePackOrigin(true);
                            ConfigXml.readConfig(reader, configBuilder);
                            try {
                                fpBuilder.addConfig(configBuilder.build());
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException(e);
                            }
                            break;
                        case PACKAGES:
                            try {
                                FeaturePackPackagesConfigParser10.readPackages(reader, fpBuilder);
                            } catch (ProvisioningDescriptionException e) {
                                throw new XMLStreamException(e);
                            }
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    public static void parseDefaultConfigs(XMLExtendedStreamReader reader, Builder fpBuilder) throws XMLStreamException {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case INHERIT:
                    fpBuilder.setInheritConfigs(Boolean.parseBoolean(reader.getAttributeValue(i)));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getLocalName());
                    switch (element) {
                        case INCLUDE:
                            parseConfigModelRef(reader, fpBuilder, true);
                            break;
                        case EXCLUDE:
                            parseConfigModelRef(reader, fpBuilder, false);
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private static void parseConfigModelRef(XMLExtendedStreamReader reader, Builder fpBuilder, boolean include) throws XMLStreamException {
        String name = null;
        String model = null;
        Boolean inheritFeatures = null;
        Boolean namedConfigsOnly = null;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final Attribute attribute = Attribute.of(reader.getAttributeName(i));
            switch (attribute) {
                case NAME:
                    name = reader.getAttributeValue(i);
                    break;
                case MODEL:
                    model = reader.getAttributeValue(i);
                    break;
                case INHERIT_FEATURES:
                    inheritFeatures = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                case NAMED_CONFIGS_ONLY:
                    namedConfigsOnly = Boolean.parseBoolean(reader.getAttributeValue(i));
                    break;
                default:
                    throw ParsingUtils.unexpectedContent(reader);
            }
        }

        try {
            if (include) {
                if (name == null) {
                    fpBuilder.includeModel(model);
                } else {
                    final IncludedConfig.Builder configBuilder = IncludedConfig.builder(model, name);
                    if(inheritFeatures != null) {
                        configBuilder.setInheritFeatures(inheritFeatures);
                    }
                    FeatureGroupXml.readFeatureGroupConfigBody(reader, configBuilder);
                    fpBuilder.includeDefaultConfig(configBuilder.build());
                    return;
                }
            } else if (name == null) {
                if(namedConfigsOnly != null) {
                    fpBuilder.excludeModel(model, namedConfigsOnly);
                } else {
                    fpBuilder.excludeModel(model);
                }
            } else {
                fpBuilder.excludeDefaultConfig(model, name);
            }
        } catch(ProvisioningDescriptionException e) {
            throw new XMLStreamException(e);
        }
        ParsingUtils.parseNoContent(reader);
    }
}
