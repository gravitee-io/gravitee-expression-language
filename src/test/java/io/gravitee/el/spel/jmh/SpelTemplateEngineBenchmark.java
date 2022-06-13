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
package io.gravitee.el.spel.jmh;

import io.gravitee.el.TemplateEngine;
import io.gravitee.el.spel.SpelExpressionParser;
import io.gravitee.el.spel.SpelTemplateEngine;
import io.gravitee.gateway.api.http.HttpHeaders;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/**
 * Benchmark test to compare performance between spel evaluations with and without caching parsed expression.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 2, time = 3)
public class SpelTemplateEngineBenchmark {

    private static final String EXPRESSION = "{#headers['X-Gravitee-Endpoint'] != null}";
    private final TemplateEngine engine = new SpelTemplateEngine(new SpelExpressionParser());

    @Setup
    public void setup() {
        final HttpHeaders headers = HttpHeaders.create();
        headers.add("X-Gravitee-Endpoint", "value");
        engine.getTemplateContext().setVariable("headers", headers);
    }

    @Benchmark
    public void engineNoCache() {
        engine.getValue(EXPRESSION, Boolean.class);
    }

    @Benchmark
    public void engineWithCacheEval() {
        engine.eval(EXPRESSION, Boolean.class).subscribe();
    }
}
