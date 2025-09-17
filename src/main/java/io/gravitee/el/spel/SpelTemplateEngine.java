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

import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.el.exceptions.ExpressionEvaluationException;
import io.gravitee.el.spel.context.SpelTemplateContext;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SpelTemplateEngine implements TemplateEngine {

    protected final SpelTemplateContext templateContext;
    private final io.gravitee.el.spel.SpelExpressionParser spelExpressionParser;

    public SpelTemplateEngine(io.gravitee.el.spel.SpelExpressionParser spelExpressionParser) {
        this.spelExpressionParser = spelExpressionParser;
        this.templateContext = new SpelTemplateContext();
    }

    @Override
    public <T> T getValue(String expression, Class<T> clazz) {
        return evalNow(expression, clazz);
    }

    @Override
    public <T> T evalNow(String expression, Class<T> clazz) {
        return eval(spelExpressionParser.parseExpression(expression), templateContext.getContext(), clazz);
    }

    @Override
    public <T> Maybe<T> eval(String expression, Class<T> clazz) {
        try {
            return eval(spelExpressionParser.parseAndCacheExpression(expression, templateContext), templateContext, clazz);
        } catch (Exception e) {
            return Maybe.error(e);
        }
    }

    @Override
    public TemplateContext getTemplateContext() {
        return templateContext;
    }

    @SuppressWarnings("unchecked")
    protected <T> Maybe<T> eval(CachedExpression cachedExpression, SpelTemplateContext templateContext, Class<T> clazz) {
        Expression expression;

        cachedExpression
            .expressionsToDefer()
            .forEach((key, exp) ->
                templateContext.setDeferredVariable(
                    key,
                    Maybe.defer(() ->
                        eval(spelExpressionParser.parseAndCacheExpression("{" + exp + "}", templateContext), templateContext, Object.class)
                    )
                )
            );

        expression = cachedExpression.getExpression();

        return templateContext
            .evaluationContext(cachedExpression)
            .flatMapMaybe(evaluationContext -> Maybe.fromCallable(() -> eval(expression, evaluationContext, clazz)))
            .flatMap(result -> {
                if (result instanceof Maybe maybeValue) {
                    // If we end here, the deferred value isn't resolved yet.
                    return maybeValue;
                }

                return Maybe.just(result);
            });
    }

    protected <T> T eval(Expression expression, EvaluationContext evaluationContext, Class<T> clazz) {
        try {
            return expression.getValue(evaluationContext, clazz);
        } catch (EvaluationException spelEvaluationException) {
            throw new ExpressionEvaluationException(expression.getExpressionString(), spelEvaluationException);
        }
    }
}
