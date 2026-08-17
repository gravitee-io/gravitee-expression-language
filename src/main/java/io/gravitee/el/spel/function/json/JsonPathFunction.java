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
package io.gravitee.el.spel.function.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.Predicate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to {@link #evaluate} a jsonPath on the provided object.
 * Delegates evaluation to <a href="https://github.com/jayway/JsonPath">JsonPath</a>.
 * Note {@link #evaluate} is used as {@code #jsonPath()} SpEL function.
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public final class JsonPathFunction {

    private static final Configuration CONFIGURATION = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    private JsonPathFunction() {}

    public static <T> T evaluate(Object json, String jsonPath, Predicate... predicates) throws IOException {
        final ParseContext parseContext = JsonPath.using(CONFIGURATION);
        final DocumentContext document;

        if (json instanceof String) {
            document = parseContext.parse((String) json);
        } else if (json instanceof File) {
            document = parseContext.parse((File) json);
        } else if (json instanceof InputStream) {
            document = parseContext.parse((InputStream) json);
        } else {
            document = parseContext.parse(json);
        }

        if (jsonPath == null || jsonPath.isEmpty()) {
            // Checked after parsing, where reading from a path string used to check it, so the two
            // implementations report the same failure first. Compiling would blame the json argument.
            throw new IllegalArgumentException("path can not be null or empty");
        }

        // Compile the path here rather than letting json-path read it from a string. A compiled path
        // is not immutable: evaluating a function that takes a path argument — concat($.key, …) and
        // friends — makes json-path store the value extracted from the current document inside the
        // compiled object. Reading from a string goes through the compilation cache of CacheProvider,
        // which is JVM-wide, so a single instance would carry that value across event loops and hand
        // one request the payload of another. Compiling per call keeps it local to this evaluation.
        return document.read(JsonPath.compile(jsonPath, predicates));
    }
}
