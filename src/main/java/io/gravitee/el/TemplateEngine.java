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
package io.gravitee.el;

import io.gravitee.common.util.ServiceLoaderHelper;
import io.gravitee.el.exceptions.ExpressionEvaluationException;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TemplateEngine {
    TemplateEngineFactory factory = ServiceLoaderHelper.loadFactory(TemplateEngineFactory.class);

    /**
     * Creates a {@link TemplateEngine} instance that cannot be shared.
     *
     * @return a {@link TemplateEngine} instance for single use.
     */
    static TemplateEngine templateEngine() {
        return factory.templateEngine();
    }

    /**
     * Creates a {@link TemplateEngine} instance from an existing one. The goal is to share the same variables.
     *
     * @param templateEngine the template engine to copy variables from
     * @return a {@link TemplateEngine} instance for single use.
     */
    static TemplateEngine fromTemplateEngine(TemplateEngine templateEngine) {
        return factory.fromTemplateEngine(templateEngine);
    }

    /**
     * @deprecated this method is deprecated in favor of {@link #eval(String, Class)} that supports reactive.
     * Evaluate the el expression against the current template context.
     *
     * @param expression the el expression to evaluate.
     *
     * @return the result of the evaluation as a {@link String}.
     * @see #eval(String, Class)
     */
    @Deprecated(since = "1.2.0", forRemoval = true)
    default String convert(String expression) {
        return getValue(expression, String.class);
    }

    /**
     * Evaluate the el expression against the current template context.
     *
     * @param expression the el expression to evaluate.
     * @param clazz the class of the expected result .
     * @param <T> the expected result type.
     *
     * @return the result of the evaluation.
     * @see #eval(String, Class)
     * @deprecated this method is deprecated in favor of {@link #eval(String, Class)} or {@link #evalNow(String, Class)}.
     */
    @Deprecated(since = "1.10.0", forRemoval = true)
    <T> T getValue(String expression, Class<T> clazz);

    /**
     * Evaluate the el expression against the current template context.
     * <b>Warn</b>: <code>evalNow</code> does not support deferred variables and doesn't benefit from cache expression mechanism. Use {@link #eval(String, Class)} for reactive version that supports deferred variables.
     *
     * @param expression the el expression to evaluate.
     * @param clazz the class of the expected result .
     * @param <T> the expected result type.
     *
     * @return the result of the evaluation.
     * @see #eval(String, Class)
     */
    <T> T evalNow(String expression, Class<T> clazz);

    /**
     * Evaluate the el expression against the current template context in a reactive context.
     * This method supports deferred variables and benefits from a cache of parsed expression.
     *
     * @param expression the el expression to evaluate.
     * @param clazz the class of the expected result .
     * @param <T> the expected result type.
     *
     * @return a {@link Maybe} with the result of the evaluation or empty in case the evaluation returns <code>null</code>.
     */
    <T> Maybe<T> eval(String expression, Class<T> clazz);

    /**
     * Blocking evaluation of the el expression against the current template context in a reactive context.
     * This method supports deferred variables and benefits from a cache of parsed expression.
     * <b>Warn</b>: <code>evalBlocking</code> cannot be invoked on the eventloop. This is to avoid possible deadlock. In that case a {@link ExpressionEvaluationException} will be thrown.
     *
     *
     * @param expression the el expression to evaluate.
     * @param clazz the class of the expected result .
     * @param <T> the expected result type.
     *
     * @return the result of the evaluation.
     */
    default <T> T evalBlocking(String expression, Class<T> clazz) throws ExpressionEvaluationException {
        final Context currentContext = Vertx.currentContext();
        if (currentContext != null && currentContext.isEventLoopContext()) {
            throw new ExpressionEvaluationException("Cannot evaluate expression " + expression + " on a vertx event loop");
        }

        return eval(expression, clazz).blockingGet();
    }

    /**
     * The context containing all the variables that can be used to evaluate the expressions.
     *
     * @return the current template context.
     */
    TemplateContext getTemplateContext();
}
