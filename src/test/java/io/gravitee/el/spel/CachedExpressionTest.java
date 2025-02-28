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
package io.gravitee.el.spel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.Expression;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CachedExpressionTest {

    private final io.gravitee.el.spel.SpelExpressionParser spelExpressionParser = new io.gravitee.el.spel.SpelExpressionParser();

    private static Stream<Arguments> provideExpressions() {
        return Stream.of(
            Arguments.of("{#request}", Set.of("request")),
            Arguments.of("{#request.headers}", Set.of("request.headers")),
            Arguments.of("{#request.headers['X-Gravitee-Endpoint']}", Set.of("request.headers")),
            Arguments.of("{#request.headers['X-Gravitee-Endpoint'][0]}", Set.of("request.headers")),
            Arguments.of("{#request.headers[#variable][0]}", Set.of("request.headers", "variable")),
            Arguments.of("{#request.headers} {#request.params['param'][1]}", Set.of("request.headers", "request.params")),
            Arguments.of(
                "Header X-Gravitee-Endpoint: {#request.headers['X-Gravitee-Endpoint'][0]} Param param:  {#request.params['param'][1]}",
                Set.of("request.headers", "request.params")
            ),
            Arguments.of(
                "<user><id>{#request.paths[2]}</id><name>{#properties['name_123']}</name><firstname>{#properties['firstname_' + #request.paths[2]]}</firstname></user>",
                Set.of("request.paths", "properties")
            ),
            Arguments.of("{#xpath(#request.content, './/age', 'number') + 20}", Set.of("request.content")),
            Arguments.of("{#jsonPath(#request.content, '$.lastname')}", Set.of("request.content")),
            Arguments.of("{#response.content.startsWith('pong')}", Set.of("response.content")),
            Arguments.of(
                "{#jsonPath(#profile.id, '$.nothingness')?.contains('Group4')?:#variable.false}",
                Set.of("profile.id", "variable.false")
            ),
            Arguments.of("{#request.params.add(#e.key, #e.value)}", Set.of("request.params", "e.key", "e.value")),
            Arguments.of(
                "{#list.contains(#value.content.val1) and #linkedList.contains(#value.content.val2)}",
                Set.of("list", "linkedList", "value.content.val1", "value.content.val2")
            ),
            Arguments.of("{(new java.lang.String(#request.content))}", Set.of("request.content")),
            Arguments.of("{ \"status\": \"OK\"  }", Set.of()),
            Arguments.of("{(#request.timestamp > 2) && (#request.timestamp < 2)}", Set.of("request.timestamp"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideExpressions")
    void should_extract_variables(String expression, Set<String> expectedVariables) {
        final Expression parseExpression = spelExpressionParser.parseExpression(expression);
        final CachedExpression cachedExpression = new CachedExpression(parseExpression, spelExpressionParser, Set.of());

        assertNotNull(cachedExpression.getExpression());
        assertEquals(expectedVariables, cachedExpression.getVariables());
    }

    private static Stream<Arguments> provideExpressionsWithDeferredFunctions() {
        return Stream.of(
            Arguments.of("{#deferHolder.get('val')}", Set.of("deferHolder"), "#deferHolder.get('val')"), // supports EL returning Rx object.
            Arguments.of("{#deferHolder.get('val')}", Set.of(), "#deferHolder.get('val')"), // no deferred functions holder
            Arguments.of("Hello {#deferHolder.get('val')}", Set.of("deferHolder"), "Hello {##_7fe56cf8d36e46bf4c034549c567ee78}"), // literal + deferred
            Arguments.of("Hello {#deferHolder.get('val')}", Set.of(), "Hello {##deferHolder.get('val')}"), // no deferred functions holder
            Arguments.of(
                "{#deferHolder.get('val').contains('val')}",
                Set.of("deferHolder"),
                "#_7fe56cf8d36e46bf4c034549c567ee78.contains('val')"
            ), // invoking method on a deferred functions holder
            Arguments.of("{#deferHolder.get('val').contains('val')}", Set.of(), "#deferHolder.get('val').contains('val')"), // no deferred functions holder
            Arguments.of(
                "{#something.get(#deferHolder.get('val'))}",
                Set.of("deferHolder"),
                "#something.get(#_7fe56cf8d36e46bf4c034549c567ee78)"
            ), // calling method with deferred as an argument
            Arguments.of("{#something.get(#deferHolder.get('val'))}", Set.of(), "#something.get(#deferHolder.get('val'))"), // no deferred functions holder
            Arguments.of(
                "{#deferHolder.get('val')['X-Gravitee-Endpoint']}",
                Set.of("deferHolder"),
                "#_7fe56cf8d36e46bf4c034549c567ee78['X-Gravitee-Endpoint']"
            ), // accessing index on deferred
            Arguments.of("{#deferHolder.get('val')['X-Gravitee-Endpoint']}", Set.of(), "#deferHolder.get('val')['X-Gravitee-Endpoint']"), // no deferred function holder
            Arguments.of(
                "{('Hello'.contains(#deferHolder.get('val')))}",
                Set.of("deferHolder"),
                "('Hello'.contains(#_7fe56cf8d36e46bf4c034549c567ee78))"
            ), // true -> literal + deferred
            Arguments.of("{('Hello'.contains(#deferHolder.get('val')))}", Set.of(), "('Hello'.contains(#deferHolder.get('val')))"), // no deferred function holder
            Arguments.of(
                "{#something.get('val')[#deferHolder.getInt()]}",
                Set.of("deferHolder"),
                "#something.get('val')[#_ed0a6c6261eb18246015ef82deb2b5fb]"
            ) // using deferred as an indexer.
        );
    }

    @ParameterizedTest
    @MethodSource("provideExpressionsWithDeferredFunctions")
    void should_detect_invocation_of_deferred_functions_holder(
        String expression,
        Set<String> deferredFunctionsHolderNames,
        String expectedExpression
    ) {
        final Expression parseExpression = spelExpressionParser.parseExpression(expression);
        final CachedExpression cachedExpression = new CachedExpression(parseExpression, spelExpressionParser, deferredFunctionsHolderNames);

        assertNotNull(cachedExpression.getExpression());
        assertThat(cachedExpression.getExpression().getExpressionString()).isEqualTo(expectedExpression);
    }
}
