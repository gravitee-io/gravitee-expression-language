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

import io.gravitee.el.spel.context.SpelTemplateContext;
import io.gravitee.node.api.cache.Cache;
import io.gravitee.node.api.cache.CacheConfiguration;
import io.gravitee.node.plugin.cache.common.InMemoryCache;
import java.util.regex.Pattern;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SpelExpressionParser {

    public static final String EXPRESSION_PREFIX = "{#";
    public static final String EXPRESSION_SUFFIX = "}";
    // This transforms expressions prefixes from user input : {#, {(, or {T
    // By prefixes that will be well interpreted by our TemplateParserContext : {##, {#(, or {#T
    // regular '{' characters won't be interpreted as expression prefixes by EL SpelExpressionParser
    private static final String EXPRESSION_REGEX = "\\{ *([#T(])((?>[^{}]+|\\{(?>[^{}]+)*\\})*\\})";
    private static final Pattern EXPRESSION_REGEX_PATTERN = Pattern.compile(EXPRESSION_REGEX);
    private static final String EXPRESSION_REGEX_SUBSTITUTE = EXPRESSION_PREFIX + "$1$2";
    private static final ParserContext PARSER_CONTEXT = new TemplateParserContext(EXPRESSION_PREFIX, EXPRESSION_SUFFIX);
    private org.springframework.expression.spel.standard.SpelExpressionParser expressionParser;

    protected static final Cache<String, CachedExpression> expressions;

    private static final int CACHE_EXPRESSION_MAX_SIZE = 20000;
    private static final int CACHE_EXPRESSION_IDLE_MILLIS = 3600000;

    static {
        final CacheConfiguration cacheConfiguration = CacheConfiguration
            .builder()
            .maxSize(CACHE_EXPRESSION_MAX_SIZE)
            .timeToIdleInMs(CACHE_EXPRESSION_IDLE_MILLIS)
            .build();

        expressions = new InMemoryCache<>("el", cacheConfiguration);
    }

    public CachedExpression parseAndCacheExpression(String expression, SpelTemplateContext templateContext) {
        CachedExpression exp = expressions.get(expression);
        if (exp != null) {
            return exp;
        }

        exp = new CachedExpression(parseExpression(expression), this, templateContext.knownDeferredVariablesName());
        expressions.put(expression, exp);

        return exp;
    }

    public Expression parseExpression(String expression) {
        return getParser()
            .parseExpression(EXPRESSION_REGEX_PATTERN.matcher(expression).replaceAll(EXPRESSION_REGEX_SUBSTITUTE), getParserContext());
    }

    protected ParserContext getParserContext() {
        return PARSER_CONTEXT;
    }

    org.springframework.expression.spel.standard.SpelExpressionParser getParser() {
        if (expressionParser == null) {
            expressionParser =
                new org.springframework.expression.spel.standard.SpelExpressionParser(
                    new SpelParserConfiguration(SpelCompilerMode.MIXED, this.getClass().getClassLoader())
                );
        }

        return expressionParser;
    }
}
