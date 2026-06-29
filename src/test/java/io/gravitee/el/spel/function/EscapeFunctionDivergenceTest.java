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

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.el.spel.function.json.escape.JsonEscapeFunction;
import io.gravitee.el.spel.function.xml.escape.XmlEscapeFunction;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EscapeFunctionDivergenceTest {

    @ParameterizedTest
    @ValueSource(strings = { "hello", "abc123" })
    void should_produce_identical_output_for_plain_ascii(String input) {
        assertThat(JsonEscapeFunction.evaluate(input)).isEqualTo(XmlEscapeFunction.evaluate(input));
    }

    @Test
    void should_diverge_on_double_quote() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate("\"")).isEqualTo("\\\"");
            softly.assertThat(XmlEscapeFunction.evaluate("\"")).isEqualTo("&quot;");
        });
    }

    @Test
    void should_diverge_on_less_than() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate("<")).isEqualTo("<");
            softly.assertThat(XmlEscapeFunction.evaluate("<")).isEqualTo("&lt;");
        });
    }

    @Test
    void should_diverge_on_greater_than() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate(">")).isEqualTo(">");
            softly.assertThat(XmlEscapeFunction.evaluate(">")).isEqualTo("&gt;");
        });
    }

    @Test
    void should_diverge_on_ampersand() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate("&")).isEqualTo("&");
            softly.assertThat(XmlEscapeFunction.evaluate("&")).isEqualTo("&amp;");
        });
    }

    @Test
    void should_diverge_on_backslash() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate("\\")).isEqualTo("\\\\");
            softly.assertThat(XmlEscapeFunction.evaluate("\\")).isEqualTo("\\");
        });
    }

    @Test
    void should_diverge_on_newline() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate("\n")).isEqualTo("\\n");
            softly.assertThat(XmlEscapeFunction.evaluate("\n")).isEqualTo("\n");
        });
    }

    @Test
    void should_both_return_null_for_null_input() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate(null)).isNull();
            softly.assertThat(XmlEscapeFunction.evaluate(null)).isNull();
        });
    }

    @Test
    void should_both_return_empty_string_for_empty_input() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(JsonEscapeFunction.evaluate("")).isEmpty();
            softly.assertThat(XmlEscapeFunction.evaluate("")).isEmpty();
        });
    }
}
