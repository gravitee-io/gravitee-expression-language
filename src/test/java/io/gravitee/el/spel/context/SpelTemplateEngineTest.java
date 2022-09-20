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
package io.gravitee.el.spel.context;

import static io.gravitee.el.spel.context.SecuredMethodResolver.EL_WHITELIST_LIST_KEY;
import static io.gravitee.el.spel.context.SecuredMethodResolver.EL_WHITELIST_MODE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateEngine;
import io.gravitee.el.exceptions.ExpressionEvaluationException;
import io.gravitee.el.spel.EvaluableRequest;
import io.gravitee.el.spel.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.context.SimpleExecutionContext;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.reactivex.observers.TestObserver;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
public class SpelTemplateEngineTest {

    @Mock
    protected Request request;

    @BeforeEach
    public void init() {
        reinitSecuredResolver(null);
    }

    @Test
    public void shouldTransformWithRequestHeader() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.headers['X-Gravitee-Endpoint']}", String.class).test();
        obs.assertResult("my_api_host");
    }

    @Test
    public void shouldTransformWithRequestHeader_concat() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.headers['X-Gravitee-Endpoint'] + '-custom'}", String.class).test();
        obs.assertResult("my_api_host-custom");
    }

    @Test
    public void shouldTransformWithRequestHeader_concat_prefix() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("custom-{#request.headers['X-Gravitee-Endpoint']}", String.class).test();
        obs.assertResult("custom-my_api_host");
    }

    @Test
    public void shouldTransformWithRequestHeader_emptyHeaderList() {
        HttpHeaders headers = mock(HttpHeaders.class);

        when(headers.getAll(any())).thenReturn(Collections.emptyList());
        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Boolean> obs = engine.eval("{#request.headers['X-Gravitee-Endpoint'] == null}", Boolean.class).test();
        obs.assertResult(true);
    }

    @Test
    public void shouldTransformWithRequestHeader_multipleValues() {
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
    public void shouldTransformWithRequestHeader_multipleValues_withIndex() {
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
    public void shouldTransformWithRequestHeader_withSpaces() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{ #request.headers['X-Gravitee-Endpoint'] }", String.class).test();
        obs.assertResult("my_api_host");
    }

    @Test
    public void shouldTransformWithRequestHeader_getValue() {
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
    public void shouldTransformWithRequestQueryParameter() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Collections.singletonList("myparam"));

        when(request.parameters()).thenReturn(parameters);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.params['param']}", String.class).test();
        obs.assertResult("myparam");
    }

    @Test
    public void shouldTransformWithRequestQueryParameter_getValue() {
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
    public void shouldTransformWithRequestQueryParameterMultipleValues() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Arrays.asList("myparam", "myparam2"));

        when(request.parameters()).thenReturn(parameters);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<String> obs = engine.eval("{#request.params['param'][1]}", String.class).test();
        obs.assertResult("myparam2");
    }

    @Test
    public void shouldTransformWithProperties() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

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
    public void shouldTransformJsonContent() {
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
    public void shouldCallRandomFunction() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval("age: {(T(java.lang.Math).random() * 60).intValue()}", String.class).test();
        obs.assertValue(v -> v.matches("age: \\d+"));
        obs.assertComplete();
    }

    @Test
    public void shouldXpathFunction() {
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
    public void shouldJsonPathFunction() {
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
    public void shouldCheckRequestContentFunction() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("pong\n");

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        final TestObserver<Boolean> obs = engine.eval("{#request.content.startsWith('pong')}", Boolean.class).test();
        obs.assertResult(true);
    }

    @Test
    public void shouldJsonPathFunctionForBoolean() {
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
    public void shouldJsonPathFunctionGroupsForBoolean() {
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
    public void shouldNotAllowedClassMethod() {
        String expression = "{T(java.lang.Class).forName('java.lang.Math')}";
        final TemplateEngine engine = TemplateEngine.templateEngine();

        final TestObserver<Object> obs = engine.eval(expression, Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldAllowMathMethod() {
        String expression = "{T(java.lang.Math).abs(60)}";
        final TemplateEngine engine = TemplateEngine.templateEngine();

        final TestObserver<Integer> obs = engine.eval(expression, Integer.class).test();
        obs.assertResult(60);
    }

    @Test
    public void shouldNotAllowAddParam() {
        when(request.parameters()).thenReturn(new LinkedMultiValueMap<>());

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.params.add('test', 'test')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldNotAllowRemoveParam() {
        when(request.parameters()).thenReturn(new LinkedMultiValueMap<>());

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.params.remove('test')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldNotAllowToAddHeader() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.headers.add('X-Gravitee-Endpoint', 'test')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldNotAllowToRemoveHeader() {
        when(request.headers()).thenReturn(HttpHeaders.create());

        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final TestObserver<Object> obs = engine.eval("{#request.headers.remove('X-Gravitee-Endpoint')}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldAllowMethodFromSuperType() {
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
    public void shouldAllowMethodFromConfiguration() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "method java.lang.System getenv");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Map> obs = engine.eval("{(T(java.lang.System)).getenv()}", Map.class).test();
        obs.assertResult(System.getenv());
    }

    @Test
    public void shouldNotAllowMethodWhenBuiltInWhitelistNotLoaded() {
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
    public void shouldIgnoreUnknownWhitelistedClassesOrMethods() {
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
    public void shouldAllowConstructorsFromWhitelistedClasses() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval("{(new java.lang.String(\"Gravitee\"))}", String.class).test();
        obs.assertResult("Gravitee");
    }

    @Test
    public void shouldNotAllowConstructorsFromUnknownClasses() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Object> obs = engine.eval("{(new java.lang.Thread())}", Object.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldAllowConstructorsFromWhitelistedConstructors() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new java.lang.Exception java.lang.String");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Exception> obs = engine.eval("{(new java.lang.Exception(\"Gravitee\"))}", Exception.class).test();
        obs.assertValue(e -> "Gravitee".equals(e.getMessage()));
    }

    @Test
    public void shouldNotAllowConstructorsOthersThanWhitelisted() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new java.lang.Exception java.lang.String");

        reinitSecuredResolver(environment);

        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Exception> obs = engine.eval("{(new java.lang.Exception())}", Exception.class).test();
        obs.assertError(ExpressionEvaluationException.class);
    }

    @Test
    public void shouldEvaluateSimpleContentWithNewline() {
        final String expression = "{\n  \"status\": \"OK\"\n}";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval(expression, String.class).test();
        obs.assertResult(expression);
    }

    @Test
    public void shouldEvaluateSimpleContentWithTab() {
        String expression = "{\t\"status\": \"OK\"  }";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval(expression, String.class).test();
        obs.assertResult(expression);
    }

    @Test
    public void shouldEvaluateSimpleContentWithSpace() {
        String expression = "{ \"status\": \"OK\"  }";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<String> obs = engine.eval(expression, String.class).test();
        obs.assertResult(expression);
    }

    @Test
    public void shouldReadMapValueWithDot() {
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
    public void shouldEvaluateWithBrackets() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Boolean> obs = engine.eval("{ (#timestamp > 2) && (#timestamp < 2) }", Boolean.class).test();
        obs.assertResult(false);
    }

    @Test
    public void shouldEvaluateBooleanWithoutBraces() {
        final TemplateEngine engine = TemplateEngine.templateEngine();
        final TestObserver<Boolean> obs = engine.eval("true", Boolean.class).test();
        obs.assertResult(true);
    }

    private void reinitSecuredResolver(Environment environment) {
        ReflectionTestUtils.setField(SecuredResolver.class, "INSTANCE", null);
        SecuredResolver.initialize(environment);

        ReflectionTestUtils.setField(SecuredMethodResolver.class, "securedResolver", SecuredResolver.getInstance());
        ReflectionTestUtils.setField(SecuredContructorResolver.class, "securedResolver", SecuredResolver.getInstance());
    }

    @Test
    public void shouldThrowParsingExceptionWithWrongExpression() {
        String wrongExpression = "{#";
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.eval(wrongExpression, Boolean.class).test().assertFailure(ParseException.class);
    }

    @Test
    public void shouldGetFirstHeader() {
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
    public void shouldConvertToSingleValueMap() {
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
    public void shouldHeadersContainsAllKeys() {
        final HttpHeaders headers = HttpHeaders.create().add("Header1", "value1").add("Header2", "value2");
        when(request.headers()).thenReturn(headers);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertTrue(engine.getValue("{ #request.headers.containsAllKeys({'Header1', 'Header2'}) }", Boolean.class));
        assertFalse(engine.getValue("{ #request.headers.containsAllKeys({'Header1', 'Header2', 'Header3'}) }", Boolean.class));
    }

    @Test
    public void shouldGetValueAsBoolean() {
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
    void shouldEvaluateSimpleRegex() {
        String pathInfo = "/user";
        String regex = "^\\/user";

        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathInfo);
        assertTrue(matcher.matches());

        when(request.pathInfo()).thenReturn(pathInfo);
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertTrue(engine.getValue("{#request.pathInfo.matches('" + regex + "')}", Boolean.class));
    }

    @Test
    void shouldEvaluateRegexWithQuantifiers() {
        String pathInfo = "/user/01234567-abcd-abcd-abcd-012345678912/file";
        String regex = "^\\/user\\/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(\\/file)?$";

        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathInfo);
        assertTrue(matcher.matches());

        when(request.pathInfo()).thenReturn(pathInfo);
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        assertTrue(engine.getValue("{#request.pathInfo.matches('" + regex + "')}", Boolean.class));
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
    void shouldEvaluateSimpleStringWithoutExpressions(String expression) {
        final String evaluatedExpression = TemplateEngine.templateEngine().getValue(expression, String.class);
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
    void shouldEvaluateStringWithExpression(String expression, String expectedResult) {
        final String evaluatedExpression = TemplateEngine.templateEngine().getValue(expression, String.class);
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
            Arguments.of("{('/my/path/A59'==#request.pathInfo)}", "false")
        );
    }

    @ParameterizedTest
    @MethodSource("expressionsWithVariables")
    void shouldEvaluateStringWithExpressionContainingVariables(String expression, String expectedResult) {
        lenient().when(request.pathInfo()).thenReturn("/my/path/A58");
        final TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        final String evaluatedExpression = engine.getValue(expression, String.class);
        assertEquals(expectedResult, evaluatedExpression);
    }
}
