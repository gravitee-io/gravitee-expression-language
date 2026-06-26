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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EscapeFunctionUtilsTest {

    @ParameterizedTest
    @MethodSource("stringInputCases")
    void should_pass_string_input_through_unchanged(String input) {
        assertThat(EscapeFunctionUtils.normalizeToText(input)).isEqualTo(input);
    }

    static Stream<Arguments> stringInputCases() {
        return Stream.of(Arguments.of("hello world"), Arguments.of(""), Arguments.of("   "));
    }

    @ParameterizedTest
    @MethodSource("collectionInputCases")
    void should_join_collection_elements_with_spaces(List<?> input, String expected) {
        assertThat(EscapeFunctionUtils.normalizeToText(input)).isEqualTo(expected);
    }

    static Stream<Arguments> collectionInputCases() {
        return Stream.of(
            Arguments.of(List.of("a", "b", "c"), "a b c"),
            Arguments.of(List.of("x"), "x"),
            Arguments.of(List.of(), ""),
            Arguments.of(Arrays.asList("a", null, "b"), "a  b")
        );
    }

    @ParameterizedTest
    @MethodSource("arrayInputCases")
    void should_join_array_elements_with_spaces(Object input, String expected) {
        assertThat(EscapeFunctionUtils.normalizeToText(input)).isEqualTo(expected);
    }

    static Stream<Arguments> arrayInputCases() {
        return Stream.of(
            Arguments.of((Object) new String[] { "x", "y" }, "x y"),
            Arguments.of(new int[] { 1, 2, 3 }, "1 2 3"),
            Arguments.of(new boolean[] { true, false }, "true false"),
            Arguments.of((Object) new String[] { "a", null }, "a ")
        );
    }

    @Test
    void should_convert_scalar_input_via_to_string() {
        assertThat(EscapeFunctionUtils.normalizeToText(42)).isEqualTo("42");
    }

    @Test
    void should_throw_npe_for_null_input() {
        assertThatThrownBy(() -> EscapeFunctionUtils.normalizeToText(null)).isInstanceOf(NullPointerException.class);
    }
}
