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

import io.gravitee.el.spel.context.DeferredFunctionHolder;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TemplateContext {
    /**
     * Set a named variable within this evaluation context to a specified value.
     * @param name variable to set
     * @param value value to be placed in the variable
     */
    void setVariable(String name, Object value);

    /**
     * Set a deferred variable that will be triggered on demand before evaluating any el expression and if the expression requires access to the specified variable.
     * If the expression evaluated against this template context does not access the <code>name</code> variable, then the deferred variable will not be invoked.
     * Specifying a {@link Completable} allows to define any custom action like populating attributes of an object added to the context (ex: request.content).
     *
     * @param name the name of the variable.
     * @param deferred a {@link Completable} that will be called if and only if the variable <code>name</code> is accessed by the evaluated expression.
     */
    void setDeferredVariable(String name, Completable deferred);

    /**
     * Same as {@link #setDeferredVariable(String, Completable)} but with a {@link Maybe}.
     * The value of the {@link Maybe} will be added to the context variable under the variable <code>name</code>.
     * @param name the name of the variable.
     * @param deferred a {@link Maybe} that will be called if and only if the variable <code>name</code> is accessed by the evaluated expression.
     */
    void setDeferredVariable(String name, Maybe<?> deferred);

    /**
     * Same as {@link #setDeferredVariable(String, Maybe)} but with a {@link Single}.
     * @param name the name of the variable.
     * @param deferred a {@link Single} that will be called if and only if the variable <code>name</code> is accessed by the evaluated expression.
     */
    void setDeferredVariable(String name, Single<?> deferred);

    /**
     * Set a deferred function holder variable. A {@link DeferredFunctionHolder} is an object that is known to expose one or more reactive methods.
     * Such an object is simply an holder allowing access to a set of function returning a {@link Maybe} or a {@link Single} that needs to be resolved when evaluating the EL.
     *
     * @param name the name of the variable.
     * @param deferredFunctionHolder an object exposing reactive function (e.g. returning {@link Maybe} or {@link Single}).
     */
    default void setDeferredFunctionHolderVariable(String name, DeferredFunctionHolder deferredFunctionHolder) {}

    /**
     * Look up a named variable within this evaluation context.
     *
     * @param name variable to lookup
     * @return the value of the variable
     */
    Object lookupVariable(String name);
}
