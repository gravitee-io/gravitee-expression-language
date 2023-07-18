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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.expression.Expression;
import org.springframework.expression.common.CompositeStringExpression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.CompoundExpression;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.ast.VariableReference;
import org.springframework.expression.spel.standard.SpelExpression;

/**
 * Allows to cache a given {@link Expression} for later reuse.
 * It also gives access to the list of variables accessed by the {@link Expression}.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CachedExpression {

    private final Expression expression;
    private Set<String> variables;

    public CachedExpression(final Expression expression) {
        this.expression = expression;
        computeVariables(expression);
    }

    private void computeVariables(Expression expression) {
        if (expression instanceof SpelExpression) {
            // Ex: "{#request.headers['X-Gravitee-Endpoint']}"
            computeVariables(((SpelExpression) expression).getAST());
        } else if (expression instanceof CompositeStringExpression) {
            // Ex: "Header X-Gravitee-Endpoint: {#request.headers['X-Gravitee-Endpoint'][0]}"
            computeVariables((CompositeStringExpression) expression);
        }
        // Could be a LiteralExpression we don't really care, ex: "Hello Gravitee".
    }

    private void computeVariables(CompositeStringExpression expression) {
        // Ex: "Header X-Gravitee-Endpoint: {#request.headers['X-Gravitee-Endpoint'][0]}"
        for (Expression e : expression.getExpressions()) {
            computeVariables(e);
        }
    }

    private void computeVariables(CompoundExpression expression) {
        // Ex: "{#value.content.val1}"
        for (int i = 0; i < expression.getChildCount(); i++) {
            final SpelNode node = expression.getChild(i);
            if (node instanceof VariableReference) {
                i++;
                StringBuilder variableName = new StringBuilder(getVariableName((VariableReference) node));
                SpelNode attrNode;
                while (i < expression.getChildCount() && ((attrNode = expression.getChild(i)) instanceof PropertyOrFieldReference)) {
                    // Iterate over all children, ex: value -> content -> val1
                    variableName.append(".").append(((PropertyOrFieldReference) attrNode).getName());
                    i++;
                }

                i--;

                addVariable(variableName.toString());
            } else {
                // Could be a method, ex: {#value.content()}.
                computeVariables(node);
            }
        }
    }

    private void computeVariables(SpelNode spelNode) {
        if (spelNode instanceof CompoundExpression) {
            // Ex: "{#value.content.val1}"
            computeVariables((CompoundExpression) spelNode);
        } else if (spelNode instanceof VariableReference && spelNode.getChildCount() == 0) {
            // Ex: "{#value}
            addVariable(getVariableName((VariableReference) spelNode));
        } else {
            // Iterate and process all child nodes.
            for (int i = 0; i < spelNode.getChildCount(); i++) {
                computeVariables(spelNode.getChild(i));
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
        return expression;
    }

    public Set<String> getVariables() {
        if (variables == null) {
            return Collections.emptySet();
        }
        return variables;
    }
}
