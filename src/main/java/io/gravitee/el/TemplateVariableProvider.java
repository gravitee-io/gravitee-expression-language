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
package io.gravitee.el;

import io.gravitee.gateway.jupiter.api.context.RequestExecutionContext;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TemplateVariableProvider {
    /**
     * Method to call to provided variables to the {@link TemplateEngine} by adding variables to the {@link TemplateContext}.
     *
     * @param templateContext the template context where to add the variables.
     */
    void provide(TemplateContext templateContext);

    /**
     * Same as {@link #provide(TemplateContext)} but with a {@link RequestExecutionContext} allowing to have access to the complete request context
     * (including request and response) as well as the {@link TemplateEngine} and {@link TemplateContext}.
     * It offers more flexibility to the template variable provider when it comes to provide template variables that are coming from the current execution context.
     *
     * @param ctx the current request execution context.
     */
    default void provide(RequestExecutionContext ctx) {
        provide(ctx.getTemplateEngine().getTemplateContext());
    }
}
