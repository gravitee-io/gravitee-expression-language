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
package io.gravitee.el.spel;

import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.regex.Pattern;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SpelTemplateEngine implements TemplateEngine {

    private static final String EXPRESSION_REGEX = "\\{([^#|T|(])";
    private static final Pattern EXPRESSION_REGEX_PATTERN = Pattern.compile(EXPRESSION_REGEX);
    private static final String EXPRESSION_REGEX_SUBSTITUTE = "{'{'}$1";

    private static final ParserContext PARSER_CONTEXT = new TemplateParserContext();
    private final SpelTemplateContext templateContext = new SpelTemplateContext();

    @Override
    public String convert(String expression) {
        return getValue(expression, String.class);
    }

    private Expression parseExpression(String expression) {
        return new SpelExpressionParser().parseExpression(
                EXPRESSION_REGEX_PATTERN.matcher(expression).replaceAll(EXPRESSION_REGEX_SUBSTITUTE),
                PARSER_CONTEXT);
    }

    private <T> T getValue(Expression expression, Class<T> clazz) {
        return expression.getValue(templateContext.getContext(), clazz);
    }

    @Override
    public <T> T getValue(String expression, Class<T> clazz) {
        return getValue(parseExpression(expression), clazz);
    }

    @Override
    public TemplateContext getTemplateContext() {
        return templateContext;
    }
}
