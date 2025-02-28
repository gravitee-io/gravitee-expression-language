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
package io.gravitee.el.spel;

import static java.util.Collections.emptyMap;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.expression.Expression;
import org.springframework.expression.common.CompositeStringExpression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.*;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.util.DigestUtils;

/**
 * Allows to cache a given {@link Expression} for later reuse.
 * It also gives access to the list of variables accessed by the {@link Expression}.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CachedExpression {

    private final Expression expression;
    private final SpelExpressionParser parser;
    private final Set<String> knownDeferredFunctionHolders;

    private Set<String> variables;
    private LinkedHashMap<String, String> expressionsToDefer;
    private Expression rebuiltExpressionForDefer;

    public CachedExpression(final Expression expression, SpelExpressionParser parser, Set<String> knownDeferredFunctionHolders) {
        this.expression = expression;
        this.parser = parser;
        this.knownDeferredFunctionHolders = knownDeferredFunctionHolders;

        computeVariables(expression);
    }

    public Map<String, String> expressionsToDefer() {
        if (rebuiltExpressionForDefer == null) {
            return emptyMap();
        }
        return expressionsToDefer;
    }

    private void computeVariables(Expression expression) {
        final List<String> deferExpressionsCollector = new ArrayList<>();

        computeVariables(expression, deferExpressionsCollector);
        computeFinalExpression(expression, deferExpressionsCollector);
    }

    private void computeVariables(Expression expression, List<String> deferExpressionsCollector) {
        if (expression instanceof SpelExpression spelExpression) {
            // Ex: "{#request.headers['X-Gravitee-Endpoint']}"
            computeVariables(spelExpression.getAST(), deferExpressionsCollector);
        } else if (expression instanceof CompositeStringExpression compositeStringExpression) {
            // Ex: "Header X-Gravitee-Endpoint: {#request.headers['X-Gravitee-Endpoint'][0]}"
            computeVariables(compositeStringExpression, deferExpressionsCollector);
        }
        // Could be a LiteralExpression we don't really care, ex: "Hello Gravitee".
    }

    private void computeVariables(CompositeStringExpression expression, List<String> deferExpressionsCollector) {
        // Ex: "Header X-Gravitee-Endpoint: {#request.headers['X-Gravitee-Endpoint'][0]}"
        for (Expression e : expression.getExpressions()) {
            computeVariables(e, deferExpressionsCollector);
        }
    }

    private void computeVariables(CompoundExpression expression, List<String> deferExpressionsCollector) {
        // Ex: "{#value.content.val1.get('val')[0]}"
        final List<SpelNodeImpl> deferExpressionNodes = new ArrayList<>();

        for (int i = 0; i < expression.getChildCount(); i++) {
            final SpelNode node = expression.getChild(i);
            if (node instanceof VariableReference variableReference) {
                // Ex: "{#value.content.val1}"

                // nodes: [value]
                deferExpressionNodes.add(variableReference);

                i++;
                StringBuilder variableName = new StringBuilder(getVariableName(variableReference));

                while (
                    i < expression.getChildCount() && (expression.getChild(i) instanceof PropertyOrFieldReference propertyOrFieldReference)
                ) {
                    // Iterate over all children, ex: value -> content -> val1
                    variableName.append(".").append(propertyOrFieldReference.getName());

                    // nodes: [value, content, val1]
                    deferExpressionNodes.add(propertyOrFieldReference);
                    i++;
                }

                i--;

                addVariable(variableName.toString());
            } else if (node instanceof MethodReference || node instanceof Indexer) {
                // nodes: [value, content, val1, get('val')]
                deferExpressionNodes.add((SpelNodeImpl) node);

                // Compute the method, ex: {#value.content.val1.get('val')}.
                computeVariables(node, deferExpressionsCollector);

                if (deferExpressionNodes.size() > 1) {
                    // Rebuild defer expression from nodes and add it to the collector, ex: #value.content.val1.get('val').
                    addDeferExpression(deferExpressionNodes, deferExpressionsCollector);
                }
            } else {
                // Literal, FunctionReference, TypeReference, ex: {#value.content.val1.get()[0]}
                if (deferExpressionNodes.size() > 1) {
                    // Rebuild defer expression from nodes and add it to the collector, ex: #value.content.val1.get('val').
                    addDeferExpression(deferExpressionNodes, deferExpressionsCollector);
                }

                // Could be a method, ex: {#value.content()}.
                computeVariables(node, deferExpressionsCollector);
            }
        }
    }

    private void computeVariables(SpelNode spelNode, List<String> deferExpressionsCollector) {
        if (spelNode instanceof CompoundExpression compoundExpression) {
            // Ex: "{#value.content.val1}"
            computeVariables(compoundExpression, deferExpressionsCollector);
        } else if (spelNode instanceof VariableReference variableReference && spelNode.getChildCount() == 0) {
            // Ex: "{#value}
            addVariable(getVariableName(variableReference));
        } else {
            // Iterate and process all child nodes.
            for (int i = 0; i < spelNode.getChildCount(); i++) {
                computeVariables(spelNode.getChild(i), deferExpressionsCollector);
            }
        }
    }

    private String getVariableName(VariableReference variableReference) {
        return variableReference.toStringAST().replace("#", "");
    }

    private void addVariable(String name) {
        if (variables == null) {
            variables = new HashSet<>();
        }
        variables.add(name);
    }

    public Expression getExpression() {
        if (rebuiltExpressionForDefer != null) {
            return rebuiltExpressionForDefer;
        }
        return expression;
    }

    public Set<String> getVariables() {
        if (variables == null) {
            return Collections.emptySet();
        }
        return variables;
    }

    private void computeFinalExpression(Expression expression, List<String> deferExpressionsCollector) {
        if (makesUseOfDeferFunctions() && (expression instanceof CompositeStringExpression || !deferExpressionsCollector.isEmpty())) {
            expressionsToDefer = new LinkedHashMap<>();

            String lastDeferVariable = null;

            for (String exp : deferExpressionsCollector) {
                for (Map.Entry<String, String> e : expressionsToDefer.entrySet()) {
                    exp = exp.replaceAll(Pattern.quote(e.getValue()), "#" + e.getKey());
                }

                lastDeferVariable = "_" + DigestUtils.md5DigestAsHex(exp.getBytes(StandardCharsets.UTF_8));
                expressionsToDefer.put(lastDeferVariable, exp);
            }

            final StringBuilder finalExpressionBuilder = new StringBuilder();

            if (expression instanceof CompositeStringExpression compositeStringExpression) {
                for (Expression e : compositeStringExpression.getExpressions()) {
                    if (e instanceof SpelExpression spelExpression) {
                        finalExpressionBuilder.append("{").append(spelExpression.toStringAST()).append("}");
                    } else {
                        finalExpressionBuilder.append(e.getExpressionString());
                    }
                }
            } else if (expression instanceof SpelExpression spelExpression) {
                if (!(spelExpression.getAST().getChild(0) instanceof Literal)) {
                    // Last variable is unnecessary when the original expression isn't a Literal.
                    expressionsToDefer.remove(lastDeferVariable);
                }
                finalExpressionBuilder.append(spelExpression.toStringAST());
            }

            String finalExpression = finalExpressionBuilder.toString();

            for (Map.Entry<String, String> e : expressionsToDefer.entrySet()) {
                finalExpression = finalExpression.replaceAll(Pattern.quote(e.getValue()), "#" + e.getKey());

                if (!finalExpression.equals(expression.getExpressionString())) {
                    this.variables.add(e.getKey());
                }
            }

            if (!finalExpression.equals(expression.getExpressionString())) {
                if (expression instanceof SpelExpression spelExpression) {
                    if (spelExpression.getAST().getChild(0) instanceof Literal) {
                        // Ex: 'Hello'.contains(#deferred.get('val')
                        finalExpression = "{(" + finalExpression + ")}";
                    } else {
                        finalExpression = "{" + finalExpression + "}";
                    }
                }

                this.rebuiltExpressionForDefer = parser.parseExpression(finalExpression);
            }
        }
    }

    private void addDeferExpression(List<SpelNodeImpl> nodes, List<String> deferExpressionsCollector) {
        final String deferExpression = buildCompoundExpression(nodes).toStringAST();

        if (knownDeferredFunctionHolders.stream().anyMatch(deferExpression::contains)) {
            deferExpressionsCollector.add(deferExpression);
        }
    }

    private boolean makesUseOfDeferFunctions() {
        return (
            variables != null && knownDeferredFunctionHolders != null && variables.stream().anyMatch(knownDeferredFunctionHolders::contains)
        );
    }

    private CompoundExpression buildCompoundExpression(List<SpelNodeImpl> sub) {
        return new CompoundExpression(
            sub.get(0).getStartPosition(),
            sub.get(sub.size() - 1).getEndPosition(),
            sub.toArray(new SpelNodeImpl[0])
        );
    }
}
