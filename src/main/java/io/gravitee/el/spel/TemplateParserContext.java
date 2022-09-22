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

import io.reactivex.rxjava3.annotations.NonNull;
import org.springframework.expression.ParserContext;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TemplateParserContext implements ParserContext {

    private final String prefix;
    private final String suffix;

    public TemplateParserContext(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @NonNull
    @Override
    public String getExpressionPrefix() {
        return prefix;
    }

    @NonNull
    @Override
    public String getExpressionSuffix() {
        return suffix;
    }

    @Override
    public boolean isTemplate() {
        return true;
    }
}
