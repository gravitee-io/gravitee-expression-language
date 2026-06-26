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
package io.gravitee.el.spel.function;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class EscapeFunctionUtils {

    private EscapeFunctionUtils() {}

    public static String normalizeToText(Object input) {
        if (input instanceof String stringInput) {
            return stringInput;
        }
        if (input instanceof Collection<?> collection) {
            return collection.stream().map(EscapeFunctionUtils::elementToText).collect(Collectors.joining(" "));
        }
        if (input.getClass().isArray()) {
            return IntStream
                .range(0, Array.getLength(input))
                .mapToObj(index -> elementToText(Array.get(input, index)))
                .collect(Collectors.joining(" "));
        }
        return input.toString();
    }

    private static String elementToText(Object element) {
        return element != null ? element.toString() : "";
    }
}
