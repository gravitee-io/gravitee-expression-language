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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class XPathExpression {

    private final javax.xml.xpath.XPathExpression xpathExpression;
    private final String expression;

    public XPathExpression(javax.xml.xpath.XPathExpression xpathExpression, String expression) {
        this.xpathExpression = xpathExpression;
        this.expression = expression;
    }

    public String toString() {
        return this.expression;
    }

    public String evaluateAsString(Node node) {
        return (String) this.evaluate(node, XPathConstants.STRING);
    }

    public List<Node> evaluateAsNodeList(Node node) {
        NodeList nodeList = (NodeList) this.evaluate(node, XPathConstants.NODESET);
        return this.toNodeList(nodeList);
    }

    private Object evaluate(Node node, QName returnType) {
        try {
            synchronized (this.xpathExpression) {
                return this.xpathExpression.evaluate(node, returnType);
            }
        } catch (XPathExpressionException var6) {
            throw new XPathParseException("Could not evaluate XPath expression:" + var6.getMessage(), var6);
        }
    }

    private List<Node> toNodeList(NodeList nodeList) {
        List<Node> result = new ArrayList<>(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); ++i) {
            result.add(nodeList.item(i));
        }

        return result;
    }

    public double evaluateAsNumber(Node node) {
        return (Double) this.evaluate(node, XPathConstants.NUMBER);
    }

    public boolean evaluateAsBoolean(Node node) {
        return (Boolean) this.evaluate(node, XPathConstants.BOOLEAN);
    }

    public Node evaluateAsNode(Node node) {
        return (Node) this.evaluate(node, XPathConstants.NODE);
    }
}
