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

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.el.TemplateEngine;
import io.gravitee.el.exceptions.ExpressionEvaluationException;
import io.gravitee.el.spel.context.SecuredResolverTestInitializer;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JsonEscapeFunctionTest {

    @BeforeEach
    void setUp() {
        SecuredResolverTestInitializer.reinit();
    }

    @ParameterizedTest
    @MethodSource("elStringInputCases")
    void should_handle_string_inputs_in_el(String expression, String expected) {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval(expression, String.class).test().assertResult(expected);
    }

    static Stream<Arguments> elStringInputCases() {
        return Stream.of(
            Arguments.of("{#jsonEscape('say \"hi\"')}", "say \\\"hi\\\""),
            Arguments.of("{#jsonEscape(\"\")}", ""),
            Arguments.of("{#jsonEscape('   ')}", "   "),
            Arguments.of("{#jsonEscape('/api/v1')}", "\\/api\\/v1")
        );
    }

    @Test
    void should_handle_null_input_in_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        TestObserver<String> obs = engine.eval("{#jsonEscape(null)}", String.class).test();
        obs.assertComplete();
        assertThat(obs.values()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "123, 123", "true, true" })
    void should_handle_scalar_non_string_types(String value, String expected) {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval("{#jsonEscape(" + value + ")}", String.class).test().assertResult(expected);
    }

    @ParameterizedTest
    @MethodSource("collectionInputCases")
    void should_handle_collection_inputs(String elExpression, String expected) {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval(elExpression, String.class).test().assertResult(expected);
    }

    static Stream<Arguments> collectionInputCases() {
        return Stream.of(
            Arguments.of("{#jsonEscape({})}", ""),
            Arguments.of("{#jsonEscape({'hello'})}", "hello"),
            Arguments.of("{#jsonEscape({'hello', 'world'})}", "hello world")
        );
    }

    @ParameterizedTest
    @MethodSource("escapeCharacterCases")
    void should_escape_special_characters(String input, String expected) {
        assertThat(JsonEscapeFunction.evaluate(input)).isEqualTo(expected);
    }

    static Stream<Arguments> escapeCharacterCases() {
        return Stream.of(
            Arguments.of("hello \"world\"", "hello \\\"world\\\""),
            Arguments.of("a\\b", "a\\\\b"),
            Arguments.of("a\nb", "a\\nb"),
            Arguments.of("a\tb", "a\\tb"),
            Arguments.of("a\rb", "a\\rb"),
            Arguments.of("a\bb", "a\\bb"),
            Arguments.of("a\fb", "a\\fb"),
            Arguments.of("/", "\\/"),
            Arguments.of("   ", "   "),
            Arguments.of("hello\u0001world", "hello\\u0001world")
        );
    }

    @ParameterizedTest
    @MethodSource("nonAsciiInputCases")
    void should_escape_non_ascii_characters(String input, String expected) {
        assertThat(JsonEscapeFunction.evaluate(input)).isEqualTo(expected);
    }

    static Stream<Arguments> nonAsciiInputCases() {
        return Stream.of(Arguments.of("café", "caf\\u00E9"), Arguments.of("世界", "\\u4E16\\u754C"), Arguments.of("🚀", "\\uD83D\\uDE80"));
    }

    @ParameterizedTest
    @MethodSource("templateVariableCases")
    void should_escape_control_characters_from_template_variable(String value, String expected) {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("payload", value);
        engine.eval("{#jsonEscape(#payload)}", String.class).test().assertResult(expected);
    }

    static Stream<Arguments> templateVariableCases() {
        return Stream.of(Arguments.of("line1\nline2", "line1\\nline2"), Arguments.of("col1\tcol2", "col1\\tcol2"));
    }

    @Test
    void should_return_null_for_null_input() {
        assertThat(JsonEscapeFunction.evaluate(null)).isNull();
    }

    @ParameterizedTest
    @MethodSource("arrayInputCases")
    void should_handle_array_inputs(Object input, String expected) {
        assertThat(JsonEscapeFunction.evaluate(input)).isEqualTo(expected);
    }

    static Stream<Arguments> arrayInputCases() {
        return Stream.of(
            Arguments.of(new int[] { 1, 2, 3 }, "1 2 3"),
            Arguments.of((Object) new Object[] { "hello" }, "hello"),
            Arguments.of((Object) new String[] { "hello", "world" }, "hello world")
        );
    }

    @ParameterizedTest
    @MethodSource("nullSubstitutionCases")
    void should_substitute_empty_string_for_null(Object input, String expected) {
        assertThat(JsonEscapeFunction.evaluate(input)).isEqualTo(expected);
    }

    static Stream<Arguments> nullSubstitutionCases() {
        return Stream.of(
            Arguments.of(Arrays.asList((Object) null), ""),
            Arguments.of(Arrays.asList("a", null, "b"), "a  b"),
            Arguments.of((Object) new Object[] { null }, ""),
            Arguments.of((Object) new Object[] { "a", null, "b" }, "a  b")
        );
    }

    @ParameterizedTest
    @MethodSource("sandboxBlockedArguments")
    void should_reject_non_whitelisted_argument(String expression) {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval(expression, String.class).test().assertError(ExpressionEvaluationException.class);
    }

    static Stream<Arguments> sandboxBlockedArguments() {
        return Stream.of(
            Arguments.of("{#jsonEscape(T(java.lang.Runtime).getRuntime())}"),
            Arguments.of("{#jsonEscape(new java.lang.Thread())}")
        );
    }

    @Test
    void should_allow_whitelisted_argument() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval("{#jsonEscape(T(java.lang.Math).abs(-42))}", String.class).test().assertResult("42");
    }

    @ParameterizedTest
    @MethodSource("wrongArityExpressions")
    void should_error_on_wrong_arity(String expression) {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval(expression, String.class).test().assertError(Exception.class);
    }

    static Stream<Arguments> wrongArityExpressions() {
        return Stream.of(Arguments.of("{#jsonEscape()}"), Arguments.of("{#jsonEscape('a','b')}"));
    }
}
