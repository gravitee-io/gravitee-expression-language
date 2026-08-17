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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

/**
 * A compiled json-path is not immutable: evaluating a function that takes a path argument makes
 * json-path store the value extracted from the current document inside the compiled object. Sharing
 * one instance — which the JVM-wide compilation cache of {@code CacheProvider} does — therefore hands
 * one caller the value of another document, silently. The Gateway evaluates the same expression on
 * every event loop at once, so this is the scenario that matters.
 *
 * @author GraviteeSource Team
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JsonPathFunctionConcurrencyTest {

    private static final String PATH_WITH_FUNCTION = "concat(\"/\", $.key)";
    private static final int THREADS = 16;
    private static final int EVALUATIONS_PER_THREAD = 500;

    @Test
    void should_never_return_the_value_of_another_document() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        final CountDownLatch ready = new CountDownLatch(THREADS);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(THREADS);
        final AtomicInteger wrong = new AtomicInteger();
        final List<Throwable> failures = new ArrayList<>();

        try {
            for (int thread = 0; thread < THREADS; thread++) {
                final String value = "value-" + thread;
                executor.execute(() -> {
                    final Map<String, String> document = new HashMap<>();
                    document.put("key", value);
                    final String expected = "/" + value;
                    try {
                        ready.countDown();
                        start.await();
                        for (int i = 0; i < EVALUATIONS_PER_THREAD; i++) {
                            if (!expected.equals(JsonPathFunction.evaluate(document, PATH_WITH_FUNCTION))) {
                                wrong.incrementAndGet();
                            }
                        }
                    } catch (Throwable t) {
                        synchronized (failures) {
                            failures.add(t);
                        }
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertThat(ready.await(10, TimeUnit.SECONDS)).as("every thread reached the starting line").isTrue();
            start.countDown();
            assertThat(done.await(60, TimeUnit.SECONDS)).as("every thread finished").isTrue();
        } finally {
            executor.shutdownNow();
        }

        assertThat(failures).isEmpty();
        assertThat(wrong).as("evaluations that returned another document's value").hasValue(0);
    }
}
