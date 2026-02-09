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
package io.gravitee.el.spel.context;

import static io.gravitee.el.spel.context.SecuredMethodResolver.EL_WHITELIST_LIST_KEY;
import static io.gravitee.el.spel.context.SecuredMethodResolver.EL_WHITELIST_MODE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.el.exceptions.ExpressionEvaluationException;
import io.gravitee.el.spel.EvaluableRequest;
import io.gravitee.el.spel.Request;
import io.gravitee.el.spel.TestDeferredFunctionHolder;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.context.SimpleExecutionContext;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.Vertx;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.expression.ParseException;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SpelTemplateEngineTest {

    @Mock
    protected Request request;

    @BeforeEach
    void init() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "class io.gravitee.el.spel.TestDeferredFunctionHolder");
        reinitSecuredResolver(environment);
    }

    @ParameterizedTest
    @CsvSource(
        quoteCharacter = '"',
        value = {
            "{#request.headers['X-Gravitee-Endpoint']}, my_api_host",
            "{#request.headers['X-Gravitee-Endpoint']+'-custom'}, my_api_host-custom",
            "custom-{#request.headers['X-Gravitee-Endpoint']}, custom-my_api_host",
        }
    )
    void should_transform_with_request_header(String given, String expected) {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertThat(engine.evalNow(given, String.class)).isEqualTo(expected);
    }

    @Test
    void should_transform_with_request_header_empty_header_list() {
        HttpHeaders headers = HttpHeaders.create();

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Boolean> obs = engine.eval("{#request.headers['X-Gravitee-Endpoint'] == null}", Boolean.class).test();
        obs.assertResult(true);
    }

    @Test
    void should_transform_with_request_header_multiple_values() {
        final HttpHeaders headers = HttpHeaders
            .create()
            .add("X-Gravitee-Endpoint", "my_api_host")
            .add("X-Gravitee-Endpoint", "my_api_host2");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.headers['X-Gravitee-Endpoint']}", String.class).test();
        obs.assertResult("my_api_host,my_api_host2");
    }

    @Test
    void should_transform_with_request_header_multiple_values_with_index() {
        final HttpHeaders headers = HttpHeaders
            .create()
            .add("X-Gravitee-Endpoint", "my_api_host")
            .add("X-Gravitee-Endpoint", "my_api_host2");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.headers['X-Gravitee-Endpoint'][0]}", String.class).test();
        obs.assertResult("my_api_host");
    }

    @Test
    void should_transform_with_request_header_with_spaces() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{ #request.headers['X-Gravitee-Endpoint'] }", String.class).test();
        obs.assertResult("my_api_host");
    }

    @Test
    void should_transform_with_request_header_getValue() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");
        final EvaluableRequest evaluableRequest = new EvaluableRequest(this.request);

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", evaluableRequest);

        final TestObserver<EvaluableRequest> requestObs = engine.eval("{#request}", EvaluableRequest.class).test();
        requestObs.assertResult(evaluableRequest);

        final TestObserver<HttpHeaders> headersObs = engine.eval("{#request.headers}", HttpHeaders.class).test();
        headersObs.assertResult(headers);
    }

    @Test
    void should_transform_with_request_query_parameter() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Collections.singletonList("myparam"));

        when(request.parameters()).thenReturn(parameters);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.params['param']}", String.class).test();
        obs.assertResult("myparam");
    }

    @Test
    void should_transform_with_request_query_parameter_getValue() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        final EvaluableRequest evaluableRequest = new EvaluableRequest(this.request);

        parameters.put("param", Collections.singletonList("myparam"));

        when(request.parameters()).thenReturn(parameters);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", evaluableRequest);

        final TestObserver<EvaluableRequest> requestObs = engine.eval("{#request}", EvaluableRequest.class).test();
        requestObs.assertResult(evaluableRequest);

        final TestObserver<MultiValueMap> headersObs = engine.eval("{#request.params}", MultiValueMap.class).test();
        headersObs.assertResult(parameters);
    }

    @Test
    void should_transform_with_request_query_parameter_multiple_values() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Arrays.asList("myparam", "myparam2"));

        when(request.parameters()).thenReturn(parameters);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.params['param'][1]}", String.class).test();
        obs.assertResult("myparam2");
    }

    @Test
    void should_transform_with_properties() {
        when(request.path()).thenReturn("/stores/123");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("name_123", "Doe");
        properties.put("firstname_123", "John");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));
        engine.getTemplateContext().setVariable("properties", properties);

        final TestObserver<String> obs = engine
            .eval(
                "<user><id>{#request.paths[2]}</id><name>{#properties['name_123']}</name><firstname>{#properties['firstname_' + #request.paths[2]]}</firstname></user>",
                String.class
            )
            .test();
        obs.assertResult("<user><id>123</id><name>Doe</name><firstname>John</firstname></user>");
    }

    @Test
    void should_transform_json_content() {
        when(request.path()).thenReturn("/stores/123");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("name_123", "Doe");
        properties.put("firstname_123", "John");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));
        engine.getTemplateContext().setVariable("properties", properties);

        final TestObserver<String> obs = engine
            .eval(
                "[{id: {#request.paths[2]}, firstname: {#properties['firstname_123']}, name: {#properties['name_123']}, age: 0}]",
                String.class
            )
            .test();
        obs.assertResult("[{id: 123, firstname: John, name: Doe, age: 0}]");
    }

    @Test
    void should_call_random_function() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval("age: {(T(java.lang.Math).random() * 60).intValue()}", String.class).test();
        obs.assertValue(v -> v.matches("age: \\d+"));
        obs.assertComplete();
    }

    @Test
    void should_xpath_function() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent())
            .thenReturn(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<root>" +
                "<lastname>DOE</lastname>" +
                "<firstname>JOHN</firstname>" +
                "<age>35</age>" +
                "</root>"
            );

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        String content = "{#xpath(#request.content, './/lastname')}";
        TestObserver<String> obs = engine.eval(content, String.class).test();
        obs.assertResult("DOE");

        content = "{#xpath(#request.content, './/age', 'number') + 20}";
        obs = engine.eval(content, String.class).test();
        obs.assertResult("55.0");

        content = "{#xpath(#request.content, './/something')}";
        obs = engine.eval(content, String.class).test();
        obs.assertResult("");
    }

    @Test
    void should_json_path_function() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35 }");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        String content = "{#jsonPath(#request.content, '$.lastname')}";
        TestObserver<String> obs = engine.eval(content, String.class).test();
        obs.assertResult("DOE");

        content = "{#jsonPath(#request.content, '$.age') + 20}";
        obs = engine.eval(content, String.class).test();
        obs.assertResult("55");

        content = "{#jsonPath(#request.content, '$.something')}";
        obs = engine.eval(content, String.class).test();
        obs.assertResult();
    }

    @Test
    @SneakyThrows
    void should_avoid_calling_eval_blocking_on_eventloop() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("name", "gravitee");

        String content = "Hello {#name}";

        CountDownLatch latch = new CountDownLatch(1);
        Vertx
            .vertx()
            .runOnContext(v -> {
                assertThrows(ExpressionEvaluationException.class, () -> engine.evalBlocking(content, String.class));
                latch.countDown();
            });

        assertThat(latch.await(10, TimeUnit.SECONDS)).isEqualTo(true);
    }

    @Test
    @SneakyThrows
    void should_call_eval_blocking_when_not_on_eventloop() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("name", "gravitee");

        String content = "Hello {#name}";

        assertThat(engine.evalBlocking(content, String.class)).isEqualTo("Hello gravitee");
    }

    @Test
    void should_call_literal() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("name", "gravitee");

        String content = "Hello {#name}";

        engine.eval(content, String.class).test().assertResult("Hello gravitee");
    }

    @Test
    void should_evaluate_deferred_functions() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.get('val1', 'val2')}";

        engine.eval(content, String.class).test().assertResult("resolved('val1', 'val2')");
    }

    @Test
    void should_evaluate_deferred_functions_several_times() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.get('val1', 'val2')}";

        for (int i = 0; i < 10; i++) {
            engine.eval(content, String.class).test().assertResult("resolved('val1', 'val2')");
        }
    }

    @Test
    void should_evaluate_deferred_functions_with_delay() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder(200));

        String content = "{#custom.get('val1', 'val2')}";

        engine.eval(content, String.class).test().awaitDone(1, TimeUnit.SECONDS).assertResult("resolved('val1', 'val2')");
    }

    @Test
    void should_evaluate_deferred_functions_with_list_index() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.getList('val1', 'val2')[0]}";

        engine.eval(content, String.class).test().assertResult("resolved('val1')");
    }

    @Test
    void should_evaluate_deferred_functions_with_list_and_el_as_index() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());
        engine.getTemplateContext().setVariable("index", 0);

        String content = "{#custom.getList('val1', 'val2')[#index]}";

        engine.eval(content, String.class).test().assertResult("resolved('val1')");
    }

    @Test
    void should_evaluate_deferred_functions_with_list_and_deferred_as_index() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.getList('val1', 'val2')[#custom.getIndex(0)]}";

        engine.eval(content, String.class).test().assertResult("resolved('val1')");
    }

    @Test
    void should_evaluate_list_with_deferred_as_index() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        engine.getTemplateContext().setVariable("something", "resolved");

        String content = "{#something.split(',')[#custom.getIndex(0)]}";

        engine.eval(content, String.class).test().assertResult("resolved");
    }

    @Test
    void should_evaluate_deferred_functions_with_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());
        engine.getTemplateContext().setVariable("properties", Map.of("prop1", "val1", "prop2", "val2"));

        String content = "{#custom.get(#properties['prop1'], #properties['prop2'])}";

        engine.eval(content, String.class).test().assertResult("resolved('val1', 'val2')");
    }

    @Test
    void should_evaluate_deferred_functions_in_a_literal() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "Hello {#custom.get('val1', 'val2')}";

        engine.eval(content, String.class).test().assertResult("Hello resolved('val1', 'val2')");
    }

    @Test
    void should_evaluate_deferred_functions_in_a_literal_with_multi_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.get('val1','val2')} is not equals to {#custom.get('val3', 'val4')}";

        engine.eval(content, String.class).test().assertResult("resolved('val1', 'val2') is not equals to resolved('val3', 'val4')");
    }

    @Test
    void should_evaluate_deferred_functions_and_contains() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.get('val1', 'v2').contains('resolved')}";

        engine.eval(content, Boolean.class).test().assertResult(true);
    }

    @Test
    void should_evaluate_deferred_functions_with_contains_and_deferred_functions_argument() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.get('val1', 'val2').contains(#custom.get('val3', 'val4'))}";

        engine.eval(content, Boolean.class).test().assertResult(false);
    }

    @Test
    void should_evaluate_string_contains() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{('Hello'.contains('Hello'))}";

        engine.eval(content, Boolean.class).test().assertResult(true);
    }

    @Test
    void should_evaluate_string_contains_with_deferred_functions() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{('Hello'.contains(#custom.get('val1', 'val2')))}";

        engine.eval(content, String.class).test().assertResult("false");
    }

    @Test
    void should_evaluate_deferred_functions_with_complex_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());
        engine
            .getTemplateContext()
            .setVariable(
                "properties",
                Map.of(
                    "prop1",
                    "val1",
                    "prop2",
                    "val2",
                    "prop3",
                    "val3",
                    "prop4",
                    "val4",
                    "prop5",
                    "val5",
                    "prop6",
                    "val6",
                    "prop7",
                    "val7",
                    "prop8",
                    "val8"
                )
            );

        String content =
            "{#custom.get(#custom.get(#properties['prop1'], #properties['prop2']), #custom.get(#properties['prop3'], #properties['prop4'])).concat(' ').concat(#custom.get(#custom.get(#properties['prop5'], #properties['prop6']), #custom.get(#properties['prop7'], #properties['prop8'])))}";

        engine
            .eval(content, String.class)
            .test()
            .assertResult(
                "resolved('resolved('val1', 'val2')', 'resolved('val3', 'val4')') resolved('resolved('val5', 'val6')', 'resolved('val7', 'val8')')"
            );
    }

    @Test
    void should_evaluate_deferred_functions_with_type() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{ T(java.util.Base64).getEncoder().encodeToString(#custom.get('val1', 'val2').getBytes()) }";

        engine.eval(content, String.class).test().assertResult("cmVzb2x2ZWQoJ3ZhbDEnLCAndmFsMicp");
    }

    @Test
    void should_evaluate_deferred_function_returning_single() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setDeferredFunctionHolderVariable("custom", new TestDeferredFunctionHolder());

        String content = "{#custom.getIndex(0)}";

        engine.eval(content, Integer.class).test().assertResult(0);
    }

    @Test
    void should_check_request_content_function() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("pong\n");

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        final TestObserver<Boolean> obs = engine.eval("{#request.content.startsWith('pong')}", Boolean.class).test();
        obs.assertResult(true);
    }

    @Test
    void should_json_path_function_for_boolean() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("profile", "{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35 }");

        String content = "{#jsonPath(#profile, '$.identity_provider_id') == 'idp_6'}";
        TestObserver<Boolean> obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(false);

        content = "{#jsonPath(#profile, '$.lastname') == 'DOE'}";
        obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(true);

        content = "{#jsonPath(#profile, '$.lastname') == 'DONE'}";
        obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(false);
    }

    @Test
    void should_json_path_function_groups_for_boolean() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine
            .getTemplateContext()
            .setVariable(
                "profile",
                "{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35" +
                ", \"groups\" : [\"Group1\", \"Group2\", \"Group3\"]" +
                ", \"emptiness\" : []" +
                ", \"nothingness\" : null" +
                " }"
            );

        String content = "{#jsonPath(#profile, '$.groups').contains('Group2')}";
        TestObserver<Boolean> obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(true);

        content = "{#jsonPath(#profile, '$.groups').contains('Group4')}";
        obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(false);

        content = "{#jsonPath(#profile, '$.emptiness').contains('Group4')}";
        obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(false);

        content = "{#jsonPath(#profile, '$.nothingness')?.contains('Group4')?:false}";
        obs = engine.eval(content, Boolean.class).test();
        obs.assertResult(false);
    }

    @Test
    void should_not_allowed_class_method() {
        String expression = "{T(java.lang.Class).forName('java.lang.Math')}";
        final TemplateEngine engine = TemplateEngine.templateEngine();

        final TestObserver<Object> obs = engine.eval(expression, Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_allow_math_method() {
        String expression = "{T(java.lang.Math).abs(60)}";
        final TemplateEngine engine = TemplateEngine.templateEngine();

        final TestObserver<Integer> obs = engine.eval(expression, Integer.class).test();
        obs.assertResult(60);
    }

    @Test
    void should_not_allow_add_param() {
        when(request.parameters()).thenReturn(new LinkedMultiValueMap<>());

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.params.add('test', 'test')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_not_allow_remove_param() {
        when(request.parameters()).thenReturn(new LinkedMultiValueMap<>());

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.params.remove('test')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_not_allow_to_add_header() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.headers.add('X-Gravitee-Endpoint', 'test')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_not_allow_to_remove_header() {
        when(request.headers()).thenReturn(HttpHeaders.create());

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.headers.remove('X-Gravitee-Endpoint')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_allow_method_from_super_type() {
        ArrayList<String> list = new ArrayList<>();
        list.add("test");

        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.add("test");

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("list", list);
        engine.getTemplateContext().setVariable("linkedList", linkedList);

        final TestObserver<Boolean> obs = engine.eval("{# list.contains('test') and #linkedList.contains('test')}", Boolean.class).test();
        obs.assertResult(true);
    }

    @Test
    void should_allow_method_from_configuration() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "method java.lang.System getenv");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Map> obs = engine.eval("{(T(java.lang.System)).getenv()}", Map.class).test();
        obs.assertResult(System.getenv());
    }

    @Test
    void should_not_allow_method_when_built_in_whitelist_not_loaded() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "replace") // The configured whitelist replaces the built-in (doesn't contains Math.abs(int) method).
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "method java.lang.System getenv");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{T(java.lang.Math).abs(30)}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_ignore_unknown_whitelisted_classes_or_methods() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "class io.gravitee.Unknown")
            .withProperty(EL_WHITELIST_LIST_KEY + "[1]", "method java.lang.Math unknown");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();

        final TestObserver<Integer> obs = engine.eval("{T(java.lang.Math).abs(60)}", Integer.class).test();
        obs.assertResult(60);
    }

    @Test
    void should_allow_constructors_from_whitelisted_classes() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval("{(new java.lang.String(\"Gravitee\"))}", String.class).test();
        obs.assertResult("Gravitee");
    }

    @Test
    void should_not_allow_constructors_from_unknown_classes() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Object> obs = engine.eval("{(new java.lang.Thread())}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_allow_constructors_from_whitelisted_constructors() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new java.lang.Exception java.lang.String");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Exception> obs = engine.eval("{(new java.lang.Exception(\"Gravitee\"))}", Exception.class).test();
        obs.assertValue(e -> "Gravitee".equals(e.getMessage()));
    }

    @Test
    void should_not_allow_constructors_others_than_whitelisted() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new java.lang.Exception java.lang.String");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Exception> obs = engine.eval("{(new java.lang.Exception())}", Exception.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    void should_evaluate_simple_content_with_newline() {
        final String expression = "{\n  \"status\": \"OK\"\n}";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval(expression, String.class).test();
        obs.assertResult(expression);
    }

    @Test
    void should_evaluate_simple_content_with_tab() {
        String expression = "{\t\"status\": \"OK\"  }";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval(expression, String.class).test();
        obs.assertResult(expression);
    }

    @Test
    void should_evaluate_simple_content_with_space() {
        String expression = "{ \"status\": \"OK\"  }";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval(expression, String.class).test();
        obs.assertResult(expression);
    }

    @Test
    void should_read_map_value_with_dot() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Collections.singletonList("myparam"));

        when(request.parameters()).thenReturn(parameters);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        TestObserver<MultiValueMap> paramsObs = engine.eval("{#request.params}", MultiValueMap.class).test();

        paramsObs.assertResult(parameters);

        engine.eval("{#request.params['param']}", String.class).test().assertResult("myparam");
        engine.eval("{#request.params.param}", String.class).test().assertResult("myparam");

        io.gravitee.gateway.api.Request req = Mockito.mock(io.gravitee.gateway.api.Request.class);
        Response res = Mockito.mock(Response.class);
        SimpleExecutionContext simpleExecutionContext = new SimpleExecutionContext(req, res);
        simpleExecutionContext.setAttribute("gravitee.attribute.api", "my-api-id");
        engine.getTemplateContext().setVariable("context", simpleExecutionContext);

        engine.eval("{#context.attributes['api']}", String.class).test().assertResult("my-api-id");
        engine.eval("{#context.attributes.api}", String.class).test().assertResult("my-api-id");
        engine.eval("{#context.attributes['application']}", String.class).test().assertNoValues().assertComplete();
        engine.eval("{#context.attributes.application}", String.class).test().assertNoValues().assertComplete();
    }

    @Test
    void should_evaluate_with_brackets() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Boolean> obs = engine.eval("{ (#timestamp > 2) && (#timestamp < 2) }", Boolean.class).test();
        obs.assertResult(false);
    }

    @Test
    void should_evaluate_base64_encoding() {
        String original = "original";
        final String expected = Base64.getEncoder().encodeToString(original.getBytes());
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine
            .eval("{ T(java.util.Base64).getEncoder().encodeToString('original'.getBytes()) }", String.class)
            .test()
            .assertResult(expected);
    }

    @Test
    void should_evaluate_base64_decoding() {
        String original = "original";
        final String encoded = Base64.getEncoder().encodeToString(original.getBytes());
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine
            .eval("{(new java.lang.String(T(java.util.Base64).getDecoder().decode('" + encoded + "'))) }", String.class)
            .test()
            .assertResult(original);
    }

    @Test
    void should_evaluate_boolean_without_braces() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Boolean> obs = engine.eval("true", Boolean.class).test();
        obs.assertResult(true);
    }

    private void reinitSecuredResolver(Environment environment) {
        ReflectionTestUtils.setField(SecuredResolver.class, "instance", null);
        SecuredResolver.initialize(environment);

        ReflectionTestUtils.setField(SecuredMethodResolver.class, "securedResolver", SecuredResolver.getInstance());
        ReflectionTestUtils.setField(SecuredContructorResolver.class, "securedResolver", SecuredResolver.getInstance());
    }

    @Test
    void should_throw_parsing_exception_with_wrong_expression() {
        String wrongExpression = "{#";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval(wrongExpression, Boolean.class).test().assertFailure(IllegalArgumentException.class);
    }

    @Test
    void should_get_first_header() {
        final List<CharSequence> values = new ArrayList<>();
        values.add("my_api_host");
        values.add("value2");
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", values);

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{ #request.headers.getFirst('X-Gravitee-Endpoint') }", String.class).test();
        obs.assertValue("my_api_host");
    }

    @Test
    void should_convert_to_single_value_map() {
        final List<CharSequence> values = new ArrayList<>();
        values.add("my_api_host");
        values.add("value2");
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", values).add("X-Gravitee-Other", "value");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        TestObserver<LinkedHashMap> result = engine.eval("{ #request.headers.toSingleValueMap() }", LinkedHashMap.class).test();
        result.assertValue(v ->
            v.containsKey("X-Gravitee-Endpoint") &&
            v.get("X-Gravitee-Endpoint").equals("my_api_host") &&
            v.containsKey("X-Gravitee-Other") &&
            v.get("X-Gravitee-Other").equals("value")
        );
    }

    @Test
    void should_headers_contains_all_keys() {
        final HttpHeaders headers = HttpHeaders.create().add("Header1", "value1").add("Header2", "value2");
        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertTrue(engine.evalNow("{ #request.headers.containsAllKeys({'Header1', 'Header2'}) }", Boolean.class));
        assertFalse(engine.evalNow("{ #request.headers.containsAllKeys({'Header1', 'Header2', 'Header3'}) }", Boolean.class));
    }

    @Test
    void should_get_value_as_boolean() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "true");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        engine.eval("{#request.headers['X-Gravitee-Endpoint'] != null}", Boolean.class).test().assertValue(true);
        engine
            .eval(
                "{#request.headers['X-Gravitee-Endpoint'] != null && #request.headers['X-Gravitee-Endpoint'][0] == \"true\"}",
                Boolean.class
            )
            .test()
            .assertValue(true);
        engine
            .eval(
                "{#request.headers['X-Gravitee-Endpoint'] != null && #request.headers['X-Gravitee-Endpoint'][0] == \"false\"}",
                Boolean.class
            )
            .test()
            .assertValue(false);
        engine.eval("{#request.headers['X-Gravitee-No-Present'] != null}", Boolean.class).test().assertValue(false);
    }

    @Test
    void should_evaluate_simple_regex() {
        String pathInfo = "/user";
        String regex = "^\\/user";

        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathInfo);
        assertTrue(matcher.matches());

        when(request.pathInfo()).thenReturn(pathInfo);
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertTrue(engine.evalNow("{#request.pathInfo.matches('" + regex + "')}", Boolean.class));
    }

    @Test
    void should_evaluate_regex_with_quantifiers() {
        String pathInfo = "/user/01234567-abcd-abcd-abcd-012345678912/file";
        String regex = "^\\/user\\/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(\\/file)?$";

        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathInfo);
        assertTrue(matcher.matches());

        when(request.pathInfo()).thenReturn(pathInfo);
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertTrue(engine.evalNow("{#request.pathInfo.matches('" + regex + "')}", Boolean.class));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "/api/[0-9]{2}/(.*)",
            "/api/[0-9]{217}/(.*)",
            "{",
            "{\n  \"status\": \"OK\"\n}",
            "{\t\"status\": \"OK\"  }",
            "{ \"status\": \"OK\"  }",
            "{ \"status\": \"{2}\"  }",
            "{ \"status\": \"'{2}'\"  }",
        }
    )
    void should_evaluate_simple_string_without_expressions(String expression) {
        final String evaluatedExpression = TemplateEngine.templateEngine().evalNow(expression, String.class);
        assertEquals(expression, evaluatedExpression);
    }

    private static Stream<Arguments> expressionsWithoutVariables() {
        return Stream.of(
            Arguments.of("{2} {('abc'.matches('[0-9]{2}'))} {3}", "{2} false {3}"),
            Arguments.of("{2} {a} {('78'.matches('[0-9]{2}'))} {3}", "{2} {a} true {3}"),
            Arguments.of("{2} {('718'.matches('[0-9]{2}'))} {3}", "{2} false {3}"),
            Arguments.of("{T(java.lang.String).format(\"%scd\", \"ab\")}", "abcd"),
            Arguments.of("{T(java.lang.String).format(\"%scd\", \"{2}\")}", "{2}cd"),
            Arguments.of("{  T ( java.lang.String ).format(\"%scd\", \"ab\")}", "abcd"),
            Arguments.of("{ T ( java.lang.String ).format(\"%scd\", \"{2}\")}", "{2}cd"),
            Arguments.of("{1 == 1}", "{1 == 1}"),
            Arguments.of("{(1 == 1)}", "true"),
            Arguments.of("{(12 == 1)}", "false")
        );
    }

    @ParameterizedTest
    @MethodSource("expressionsWithoutVariables")
    void should_evaluate_string_with_expression(String expression, String expectedResult) {
        final String evaluatedExpression = TemplateEngine.templateEngine().evalNow(expression, String.class);
        assertEquals(expectedResult, evaluatedExpression);
    }

    private static Stream<Arguments> expressionsWithVariables() {
        return Stream.of(
            Arguments.of("{2} {#request.pathInfo.matches('[0-9]{2}')} {3}", "{2} false {3}"),
            Arguments.of("{2} {#request.pathInfo.matches('/my/path/[A-Z]{1}[0-9]{2}')} {3}", "{2} true {3}"),
            Arguments.of("{2} {#request.pathInfo.matches('/my/path/[A-Z]{2}[0-9]{2}')} {3}", "{2} false {3}"),
            Arguments.of("{2} {  #  request.pathInfo.matches ( '/my/path/[A-Z]{1}[0-9]{2}')} {3}", "{2} true {3}"),
            Arguments.of("{2} {  #  request.pathInfo.matches ( '/my/path/[A-Z]{2}[0-9]{2}')} {3}", "{2} false {3}"),
            Arguments.of("{ '/my/path/A58' == #request.pathInfo}", "{ '/my/path/A58' == #request.pathInfo}"),
            Arguments.of("{('/my/path/A58'==#request.pathInfo)}", "true"),
            Arguments.of("{('/my/path/A59'==#request.pathInfo)}", "false"),
            Arguments.of("{T(java.lang.String).format(\"XX%sXX\", #request.pathInfo)}", "XX/my/path/A58XX"),
            Arguments.of("{T(java.lang.String).format(\"XX%sXX\", {#request.pathInfo})}", "XX/my/path/A58XX")
        );
    }

    @ParameterizedTest
    @MethodSource("expressionsWithVariables")
    void should_evaluate_string_with_expression_containing_variables(String expression, String expectedResult) {
        lenient().when(request.pathInfo()).thenReturn("/my/path/A58");
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final String evaluatedExpression = engine.evalNow(expression, String.class);
        assertEquals(expectedResult, evaluatedExpression);
    }

    private static Stream<Arguments> expressionsWithReturnType() {
        return Stream.of(
            Arguments.of("{#test.call()}", true, Boolean.class),
            Arguments.of("{#test.call().isEmpty() == false}", "hello", Boolean.class),
            Arguments.of("{#test.call()}", "hello", String.class),
            Arguments.of("{#test.call()}", 123, Integer.class),
            Arguments.of("{#test.call()}", 123L, Long.class),
            Arguments.of("{#test.call()}", 12.3f, Float.class),
            Arguments.of("{#test.call()}", (short) 1, Short.class),
            Arguments.of("{#test.call()}", List.of("hello"), List.class),
            Arguments.of("{#test.call()}", new String[] { "string" }, String[].class)
        );
    }

    @ParameterizedTest
    @MethodSource("expressionsWithReturnType")
    void should_evaluate_with_expected_return_type(String expression, Object returnedValue, Class<?> expectedReturnType) {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "class io.gravitee.el.spel.context.MyFunctionWrapper");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();

        engine.getTemplateContext().setVariable("test", new MyFunctionWrapper(returnedValue));

        final Object evaluatedExpression = engine.evalNow(expression, expectedReturnType);
        assertThat(evaluatedExpression.getClass()).isAssignableTo(expectedReturnType);
    }

    @Test
    void should_create_template_engine_from_another_template_engine() {
        TestDeferredFunctionHolder deferredFunctionHolder = new TestDeferredFunctionHolder();
        Maybe<String> defferedValue = Maybe.just("defferedValue");

        final TemplateEngine originalEngine = TemplateEngine.templateEngine();
        final SpelTemplateContext originalTemplateContext = (SpelTemplateContext) originalEngine.getTemplateContext();
        originalTemplateContext.setVariable("test", "hello");
        originalTemplateContext.setDeferredFunctionHolderVariable("defferedFunction", deferredFunctionHolder);
        originalTemplateContext.setDeferredVariable("defferedVariable", defferedValue);

        final TemplateEngine clonedEngine = TemplateEngine.fromTemplateEngine(originalEngine);
        SpelTemplateContext clonedTemplateContext = (SpelTemplateContext) clonedEngine.getTemplateContext();

        assertThat(clonedEngine).isNotSameAs(originalEngine);
        assertThat(clonedTemplateContext).isNotSameAs(originalTemplateContext);
        assertThat(clonedTemplateContext.getVariables()).isNotSameAs(originalTemplateContext.getVariables());
        assertThat(clonedTemplateContext.getDeferredVariables()).isNotSameAs(originalTemplateContext.getDeferredVariables());
        assertThat(clonedTemplateContext.getDeferredFunctionsHolders()).isNotSameAs(originalTemplateContext.getDeferredFunctionsHolders());

        assertThat(clonedTemplateContext.getVariables())
            .contains(Map.entry("test", "hello"), Map.entry("defferedFunction", deferredFunctionHolder));
        assertThat(clonedTemplateContext.getDeferredVariables()).contains(Map.entry("defferedVariable", defferedValue));
        assertThat(clonedTemplateContext.getDeferredFunctionsHolders()).contains(Map.entry("defferedFunction", deferredFunctionHolder));
    }
}
