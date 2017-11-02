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

import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import org.jboss.provisioning.spec.FeatureGroupSpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class FeatureGroupXmlParser implements XmlParser<FeatureGroupSpec> {

    private static final FeatureGroupXmlParser INSTANCE = new FeatureGroupXmlParser();

    public static FeatureGroupXmlParser getInstance() {
        return INSTANCE;
    }

    private FeatureGroupXmlParser() {
    }

    @Override
    public FeatureGroupSpec parse(final Reader input) throws XMLStreamException {
        final FeatureGroupSpec.Builder configBuilder = FeatureGroupSpec.builder();
        XmlParsers.parse(input, configBuilder);
        return configBuilder.build();
    }
}
