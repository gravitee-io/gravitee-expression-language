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
package io.gravitee.el.spel.function.json.escape;

import io.gravitee.el.spel.function.EscapeFunctionUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * <p>Escaping prevents JSON injection when embedding user input in JSON output.</p>
 *
 * @author GraviteeSource Team
 * @since 4.4.0
 */
public final class JsonEscapeFunction {

    private JsonEscapeFunction() {}

    public static String evaluate(Object input) {
        if (input == null) {
            return null;
        }
        return StringEscapeUtils.escapeJson(EscapeFunctionUtils.normalizeToText(input));
    }
}
