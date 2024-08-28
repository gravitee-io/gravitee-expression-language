/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.el.spel.function.xml;

import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.util.xml.SimpleNamespaceContext;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class XPathExpressionFactory {

    private static final XPathFactory xpathFactory = XPathFactory.newInstance();

    public static XPathExpression createXPathExpression(String expression) {
        try {
            XPath xpath = createXPath();
            javax.xml.xpath.XPathExpression xpathExpression = xpath.compile(expression);
            return new XPathExpression(xpathExpression, expression);
        } catch (XPathExpressionException var5) {
            throw new XPathParseException("Could not compile [" + expression + "] to a XPathExpression: " + var5.getMessage(), var5);
        }
    }

    private static synchronized XPath createXPath() {
        return xpathFactory.newXPath();
    }
}
