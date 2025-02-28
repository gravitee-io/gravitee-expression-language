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
package io.gravitee.el.spel.jmh;

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.el.TemplateEngine;
import io.gravitee.el.spel.SpelExpressionParser;
import io.gravitee.el.spel.SpelTemplateEngine;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openjdk.jmh.annotations.*;

/**
 * Benchmark test to compare performance between spel evaluations with and without caching parsed expression.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 2, time = 3)
public class SpelTemplateEngineBenchmark {

    private static String expression;

    private final TemplateEngine engine = new SpelTemplateEngine(new SpelExpressionParser());

    @Setup
    public void setup() {
        int maxConditions = 10;
        engine.getTemplateContext().setVariable("context", new Context(Map.of("application", "" + maxConditions)));

        StringBuilder expressionBuilder = new StringBuilder("{");

        for (int i = 0; i <= maxConditions; i++) {
            if (i != 0) {
                expressionBuilder.append(" || ");
            }
            expressionBuilder.append("#context.attributes['application'] == '").append(i).append("'");
        }

        expressionBuilder.append("}");

        expression = expressionBuilder.toString();

        // Sanity check.
        assertThat(engine.evalNow(expression, Boolean.class)).isEqualTo(true);
    }

    @Getter
    @AllArgsConstructor
    private static class Context {

        private final Map<String, String> attributes;
    }

    @Benchmark
    public void engineEvalNow() {
        engine.evalNow(expression, Boolean.class);
    }

    @Benchmark
    public void engineEvalBlocking() {
        engine.evalBlocking(expression, Boolean.class);
    }

    @Benchmark
    public void engineEval() {
        engine.eval(expression, Boolean.class).subscribe();
    }
}
