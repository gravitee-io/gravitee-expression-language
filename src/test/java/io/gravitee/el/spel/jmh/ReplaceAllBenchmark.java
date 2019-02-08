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

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 2, time = 3)
public class ReplaceAllBenchmark {

    private static final String EXPRESSION_REGEX = "\\{([^#|T|(])";
    private static final Pattern EXPRESSION_REGEX_PATTERN = Pattern.compile(EXPRESSION_REGEX);
    private static final String EXPRESSION_REGEX_SUBSTITUTE = "{'{'}$1";

    @Benchmark
    public void benchmarkPatternCompile() {
        EXPRESSION_REGEX_PATTERN.matcher("{#request.headers['X-Gravitee-Endpoint']}").replaceAll(EXPRESSION_REGEX_SUBSTITUTE);
    }

    @Benchmark
    public void benchmarkStringReplaceAll() {
        "{#request.headers['X-Gravitee-Endpoint']}".replaceAll(EXPRESSION_REGEX, EXPRESSION_REGEX_SUBSTITUTE);
    }
}
