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
    void shouldExtractVariables(String expression, Set<String> expectedVariables) {
        final Expression parseExpression = spelExpressionParser.parseExpression(expression);
        final CachedExpression cachedExpression = new CachedExpression(parseExpression);

        assertNotNull(cachedExpression.getExpression());
        assertEquals(expectedVariables, cachedExpression.getVariables());
    }
}
