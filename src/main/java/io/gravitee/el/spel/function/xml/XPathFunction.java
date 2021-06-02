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

import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public final class XPathFunction {

    public static final String STRING = "string";

    public static final String BOOLEAN = "boolean";

    public static final String NUMBER = "number";

    public static final String NODE = "node";

    public static final String NODE_LIST = "node_list";

    public static final String DOCUMENT_LIST = "document_list";

    private static final List<String> RESULT_TYPES =
            Arrays.asList(STRING, BOOLEAN, NUMBER, NODE, NODE_LIST, DOCUMENT_LIST);

    private static final XmlPayloadConverter CONVERTER = new XmlPayloadConverter();

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactoryUtils.newInstance();

    static {
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    }

    private XPathFunction() {
    }

    public static <T> T evaluate(Object object, String xpath, Object... resultArg) {
        Object resultType = null;
        if (resultArg != null && resultArg.length > 0) {
            Assert.isTrue(resultArg.length == 1, "'resultArg' can contains only one element.");
            Assert.noNullElements(resultArg, "'resultArg' can't contains 'null' elements.");
            resultType = resultArg[0];
        }

        XPathExpression expression = XPathExpressionFactory.createXPathExpression(xpath, Collections.emptyMap());
        Node node = CONVERTER.convertToNode(object);

        if (resultType == null) {
            return (T) expression.evaluateAsString(node);
        } else if (resultType instanceof String && RESULT_TYPES.contains(resultType)) {
            String resType = (String) resultType;
            if (DOCUMENT_LIST.equals(resType)) {
                List<Node> nodeList = (List<Node>) XPathEvaluationType.NODE_LIST_RESULT.evaluateXPath(expression,
                        node);
                try {
                    DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
                    List<Node> documents = new ArrayList<>(nodeList.size());
                    for (Node n : nodeList) {
                        Document document = documentBuilder.newDocument();
                        document.appendChild(document.importNode(n, true));
                        documents.add(document);
                    }
                    return (T) documents;
                }
                catch (ParserConfigurationException e) {
                    throw new XPathException("Unable to create 'documentBuilder'.", e);
                }
            }
            else {
                XPathEvaluationType evaluationType = XPathEvaluationType.valueOf(resType.toUpperCase() + "_RESULT");
                return (T) evaluationType.evaluateXPath(expression, node);
            }
        }
        else {
            throw new IllegalArgumentException("'resultArg[0]' can be an instance of 'NodeMapper<?>' " +
                    "or one of supported String constants: " + RESULT_TYPES);
        }
    }
}
