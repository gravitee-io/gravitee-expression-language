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
package io.gravitee.el.spel.context;

import io.gravitee.el.TemplateContext;
import io.gravitee.el.spel.CachedExpression;
import io.gravitee.el.spel.function.json.JsonPathFunction;
import io.gravitee.el.spel.function.xml.XPathFunction;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.expression.EvaluationContext;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SpelTemplateContext implements TemplateContext {

    protected static final Method JSON_PATH_EVAL_METHOD = BeanUtils.resolveSignature("evaluate", JsonPathFunction.class);
    protected static final Method XPATH_EVAL_METHOD = BeanUtils.resolveSignature("evaluate", XPathFunction.class);
    private final EvaluationContext context;
    private Map<String, Object> deferredVariables;

    public SpelTemplateContext() {
        context = new SecuredEvaluationContext();
        context.setVariable("jsonPath", JSON_PATH_EVAL_METHOD);
        context.setVariable("xpath", XPATH_EVAL_METHOD);
    }

    @Override
    public void setVariable(String name, Object value) {
        context.setVariable(name, value);
    }

    @Override
    public void setDeferredVariable(String name, Completable deferred) {
        addDeferredVariable(name, deferred);
    }

    @Override
    public void setDeferredVariable(String name, Maybe<?> deferred) {
        addDeferredVariable(name, deferred);
    }

    @Override
    public void setDeferredVariable(String name, Single<?> deferred) {
        addDeferredVariable(name, deferred);
    }

    @Override
    public Object lookupVariable(String name) {
        return context.lookupVariable(name);
    }

    public EvaluationContext getContext() {
        return context;
    }

    public Single<EvaluationContext> evaluationContext(CachedExpression expression) {
        if (deferredVariables != null) {
            return Flowable
                .fromIterable(deferredVariables.entrySet())
                .filter(deferredEntry -> requiresDeferredVariable(expression, deferredEntry))
                .flatMapCompletable(e -> resolveDeferred(e.getKey(), e.getValue()))
                .andThen(Single.just(context));
        }

        return Single.just(context);
    }

    private boolean requiresDeferredVariable(CachedExpression expression, Map.Entry<String, Object> deferredEntry) {
        // Ex: expression: 'request.contentJson.test', variable 'request.contentJson'
        return expression
            .getVariables()
            .stream()
            .anyMatch(variable -> variable.equals(deferredEntry.getKey()) || variable.startsWith(deferredEntry.getKey() + "."));
    }

    private Completable resolveDeferred(String key, Object deferred) {
        if (deferred instanceof Completable) {
            return (Completable) deferred;
        } else if (deferred instanceof Maybe) {
            return ((Maybe<?>) deferred).doOnSuccess(o -> context.setVariable(key, o)).ignoreElement();
        } else if (deferred instanceof Single) {
            return ((Single<?>) deferred).doOnSuccess(o -> context.setVariable(key, o)).ignoreElement();
        }

        return Completable.error(new RuntimeException("Deferred EL variable unsupported" + deferred.getClass().getSimpleName()));
    }

    private void addDeferredVariable(String name, Object deferred) {
        Objects.requireNonNull(deferred, "Deferred EL variable cannot be null");
        if (deferredVariables == null) {
            deferredVariables = new HashMap<>();
        }

        deferredVariables.put(name, deferred);
    }
}
