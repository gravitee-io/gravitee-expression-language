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
package io.gravitee.el.spel.function;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import com.jayway.jsonpath.spi.cache.NOOPCache;

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

    private static final Configuration CONFIGURATION;

    static {
        Configuration configuration = Configuration.defaultConfiguration();
        CONFIGURATION = configuration.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        CacheProvider.setCache(new NOOPCache());
    }

    private JsonPathFunction() {
    }

    public static <T> T evaluate(Object json, String jsonPath, Predicate... predicates) throws IOException {
        if (json instanceof String) {
            return JsonPath.using(CONFIGURATION).parse((String)json).read(jsonPath, predicates);
        }
        else if (json instanceof File) {
            return JsonPath.using(CONFIGURATION).parse((File)json).read(jsonPath, predicates);
        }
        else if (json instanceof InputStream) {
            return JsonPath.using(CONFIGURATION).parse((InputStream)json).read(jsonPath, predicates);
        }
        else {
            return JsonPath.using(CONFIGURATION).parse(json).read(jsonPath, predicates);
        }
    }
}
