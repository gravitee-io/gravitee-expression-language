/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.el.spel.function.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DocumentBuilderFactoryUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentBuilderFactoryUtils.class);

    public DocumentBuilderFactoryUtils() {
    }

    public static DocumentBuilderFactory newInstance() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
        } catch (IllegalArgumentException var8) {
            LOGGER.warn("http://javax.xml.XMLConstants/property/accessExternalDTD property not supported by " + factory.getClass().getCanonicalName());
        }

        try {
            factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema", "");
        } catch (IllegalArgumentException var7) {
            LOGGER.warn("http://javax.xml.XMLConstants/property/accessExternalSchema property not supported by " + factory.getClass().getCanonicalName());
        }

        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException var6) {
            LOGGER.warn("FEATURE 'http://apache.org/xml/features/disallow-doctype-decl' is probably not supported by " + factory.getClass().getCanonicalName());
        }

        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        } catch (ParserConfigurationException var5) {
            LOGGER.warn("FEATURE 'http://xml.org/sax/features/external-general-entities' is probably not supported by " + factory.getClass().getCanonicalName());
        }

        try {
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException var4) {
            LOGGER.warn("FEATURE 'http://xml.org/sax/features/external-parameter-entities' is probably not supported by " + factory.getClass().getCanonicalName());
        }

        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException var3) {
            LOGGER.warn("FEATURE 'http://apache.org/xml/features/nonvalidating/load-external-dtd' is probably not supported by " + factory.getClass().getCanonicalName());
        }

        try {
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (Exception var2) {
            LOGGER.warn("Caught " + var2.getMessage() + " attempting to configure your XML parser.");
        }

        return factory;
    }
}
