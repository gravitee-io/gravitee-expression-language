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
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateEngine;
import io.gravitee.el.exceptions.ExpressionEvaluationException;
import io.gravitee.el.spel.EvaluableRequest;
import io.gravitee.el.spel.Request;
import io.gravitee.gateway.api.http.HttpHeaders;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.StringUtils;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SpelTemplateEngineTest {

    @Mock
    protected Request request;

    @Before
    public void init() {
        initMocks(this);
        SecuredResolver.initialize(null);
    }

    @Test
    public void shouldTransformWithRequestHeader() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.headers['X-Gravitee-Endpoint']}");
        assertEquals("my_api_host", value);
    }

    @Test
    public void shouldTransformWithRequestHeader_concat() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.headers['X-Gravitee-Endpoint'] + '-custom'}");
        assertEquals("my_api_host-custom", value);
    }

    @Test
    public void shouldTransformWithRequestHeader_multipleValues() {
        final HttpHeaders headers = HttpHeaders
            .create()
            .add("X-Gravitee-Endpoint", "my_api_host")
            .add("X-Gravitee-Endpoint", "my_api_host2");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.headers['X-Gravitee-Endpoint']}");
        assertEquals("my_api_host,my_api_host2", value);
    }

    @Test
    public void shouldTransformWithRequestHeader_multipleValues_withIndex() {
        final HttpHeaders headers = HttpHeaders
            .create()
            .add("X-Gravitee-Endpoint", "my_api_host")
            .add("X-Gravitee-Endpoint", "my_api_host2");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.headers['X-Gravitee-Endpoint'][0]}");
        assertEquals("my_api_host", value);
    }

    @Test
    public void shouldTransformWithRequestHeader_withSpaces() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{ #request.headers['X-Gravitee-Endpoint'] }");
        assertEquals("my_api_host", value);
    }

    @Test
    public void shouldTransformWithRequestHeader_getValue() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        EvaluableRequest value = engine.getValue("{#request}", EvaluableRequest.class);
        HttpHeaders headersValue = engine.getValue("{#request.headers}", HttpHeaders.class);
        assertEquals("my_api_host", value.getHeaders().getFirst("X-Gravitee-Endpoint"));
        assertEquals("my_api_host", headersValue.getFirst("X-Gravitee-Endpoint"));
    }

    @Test
    public void shouldTransformWithRequestQueryParameter() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Collections.singletonList("myparam"));

        when(request.parameters()).thenReturn(parameters);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.params['param']}");
        assertEquals("myparam", value);
    }

    @Test
    public void shouldTransformWithRequestQueryParameter_getValue() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Collections.singletonList("myparam"));

        when(request.parameters()).thenReturn(parameters);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        EvaluableRequest value = engine.getValue("{#request}", EvaluableRequest.class);
        MultiValueMap<String, String> paramsValue = engine.getValue("{#request.params}", MultiValueMap.class);
        assertEquals("myparam", value.getParams().getFirst("param"));
        assertEquals("myparam", paramsValue.getFirst("param"));
    }

    @Test
    public void shouldTransformWithRequestQueryParameterMultipleValues() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Arrays.asList("myparam", "myparam2"));

        when(request.parameters()).thenReturn(parameters);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.params['param'][1]}");
        assertEquals("myparam2", value);
    }

    @Test
    public void shouldTransformWithRequestQueryParameterMultipleValues_getValue() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("param", Arrays.asList("myparam", "myparam2"));

        when(request.parameters()).thenReturn(parameters);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        EvaluableRequest value = engine.getValue("{#request}", EvaluableRequest.class);
        MultiValueMap<String, String> paramsValue = engine.getValue("{#request.params}", MultiValueMap.class);
        assertEquals("myparam2", value.getParams().get("param").get(1));
        assertEquals("myparam2", paramsValue.get("param").get(1));
    }

    @Test
    public void shouldTransformWithProperties() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/123");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("name_123", "Doe");
        properties.put("firstname_123", "John");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));
        engine.getTemplateContext().setVariable("properties", properties);

        String value = engine.convert(
            "<user><id>{#request.paths[2]}</id><name>{#properties['name_123']}</name><firstname>{#properties['firstname_' + #request.paths[2]]}</firstname></user>"
        );
        assertNotNull(value);
    }

    @Test
    public void shouldTransformJsonContent() {
        final HttpHeaders headers = HttpHeaders.create().add("X-Gravitee-Endpoint", "my_api_host");

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/123");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("name_123", "Doe");
        properties.put("firstname_123", "John");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));
        engine.getTemplateContext().setVariable("properties", properties);

        String content = "[{id: {#request.paths[2]}, firstname: {#properties['firstname_123']}, name: {#properties['name_123']}, age: 0}]";
        String value = engine.convert(content);
        assertNotNull(value);
    }

    @Test
    public void shouldCallRandomFunction() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        String content = "age: {(T(java.lang.Math).random() * 60).intValue()}";
        String value = engine.convert(content);
        assertNotNull(value);
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
        String value = engine.convert(content);
        assertEquals("DOE", value);

        content = "{#xpath(#request.content, './/age', 'number') + 20}";
        value = engine.convert(content);
        assertEquals("55.0", value);

        content = "{#xpath(#request.content, './/something')}";
        value = engine.convert(content);
        assertTrue(StringUtils.isEmpty(value));
    }

    @Test
    public void shouldJsonPathFunction() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35 }");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        String content = "{#jsonPath(#request.content, '$.lastname')}";
        String value = engine.convert(content);
        assertEquals("DOE", value);

        content = "{#jsonPath(#request.content, '$.age') + 20}";
        value = engine.convert(content);
        assertEquals("55", value);

        content = "{#jsonPath(#request.content, '$.something')}";
        value = engine.convert(content);
        Assert.assertNull(value);
    }

    @Test
    public void shouldCheckRequestContentFunction() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("pong\n");

        String assertion = "#response.content.startsWith('pong')";

        ExpressionParser parser = new SpelExpressionParser();
        Expression expr = parser.parseExpression(assertion);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("response", req);

        boolean success = expr.getValue(context, boolean.class);

        assertTrue(success);
    }

    @Test
    public void shouldJsonPathFunctionForBoolean() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("profile", "{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35 }");

        String content = "{#jsonPath(#profile, '$.identity_provider_id') == 'idp_6'}";
        boolean result = engine.getValue(content, boolean.class);
        Assert.assertFalse(result);

        content = "{#jsonPath(#profile, '$.lastname') == 'DOE'}";
        result = engine.getValue(content, boolean.class);
        assertTrue(result);

        content = "{#jsonPath(#profile, '$.lastname') == 'DONE'}";
        result = engine.getValue(content, boolean.class);
        Assert.assertFalse(result);
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
        boolean result = engine.getValue(content, boolean.class);
        assertTrue(result);

        content = "{#jsonPath(#profile, '$.groups').contains('Group4')}";
        result = engine.getValue(content, boolean.class);
        Assert.assertFalse(result);

        content = "{#jsonPath(#profile, '$.emptiness').contains('Group4')}";
        result = engine.getValue(content, boolean.class);
        Assert.assertFalse(result);

        content = "{#jsonPath(#profile, '$.nothingness')?.contains('Group4')?:false}";
        result = engine.getValue(content, boolean.class);
        Assert.assertFalse(result);
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowedClassMethod() {
        String expression = "{T(java.lang.Class).forName('java.lang.Math')}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        engine.getValue(expression, Object.class);
    }

    @Test
    public void shouldAllowMathMethod() {
        String expression = "{T(java.lang.Math).abs(60)}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        assertEquals((Integer) 60, engine.getValue(expression, Integer.class));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowAddParam() {
        when(request.parameters()).thenReturn(new LinkedMultiValueMap<>());

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        engine.getValue("{#request.params.add('test', 'test')}", String.class);
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowRemoveParam() {
        when(request.parameters()).thenReturn(new LinkedMultiValueMap<>());

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        engine.getValue("{#request.params.remove('test')}", String.class);
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowToAddHeader() {
        when(request.headers()).thenReturn(HttpHeaders.create());

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        engine.getValue("{#request.headers.add('X-Gravitee-Endpoint', 'test')}", String.class);
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowToRemoveHeader() {
        when(request.headers()).thenReturn(HttpHeaders.create());

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        engine.getValue("{#request.headers.remove('X-Gravitee-Endpoint')}", String.class);
    }

    @Test
    public void shouldAllowMethodFromSuperType() {
        ArrayList<String> list = new ArrayList<>();
        list.add("test");

        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.add("test");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("list", list);
        engine.getTemplateContext().setVariable("linkedList", linkedList);

        assertTrue(engine.getValue("{# list.contains('test') and #linkedList.contains('test')}", Boolean.class));
    }

    @Test
    public void shouldAllowMethodFromConfiguration() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[O]", "method java.lang.System getenv");

        SecuredResolver.initialize(environment);

        TemplateEngine engine = TemplateEngine.templateEngine();
        assertEquals(System.getenv(), engine.getValue("{(T(java.lang.System)).getenv()}", Map.class));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowMethodWhenBuiltInWhitelistNotLoaded() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "replace") // The configured whitelist replaces the built-in (doesn't contains Math.abs(int) method).
            .withProperty(EL_WHITELIST_LIST_KEY + "[O]", "method java.lang.System getenv");

        SecuredResolver.initialize(environment);

        String expression = "{T(java.lang.Math).abs(60)}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        engine.getValue(expression, Object.class);
    }

    @Test
    public void shouldIgnoreUnknownWhitelistedClassesOrMethods() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[O]", "class io.gravitee.Unknown")
            .withProperty(EL_WHITELIST_LIST_KEY + "[1]", "method java.lang.Math unknown");

        SecuredResolver.initialize(environment);

        String expression = "{T(java.lang.Math).abs(60)}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        assertEquals((Integer) 60, engine.getValue(expression, Integer.class));
    }

    @Test
    public void shouldAllowConstructorsFromWhitelistedClasses() {
        String expression = "{(new java.lang.String(\"Gravitee\"))}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        assertEquals("Gravitee", engine.getValue(expression, String.class));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldNotAllowConstructorsFromUnknownClasses() {
        String expression = "{(new java.lang.Thread())}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        engine.getValue(expression, String.class);
    }

    @Test
    public void shouldAllowConstructorsFromWhitelistedConstructors() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new java.lang.Exception java.lang.String");

        SecuredResolver.initialize(environment);
        String expression = "{(new java.lang.Exception(\"Gravitee\"))}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        assertEquals(new Exception("Gravitee").getMessage(), engine.getValue(expression, Exception.class).getMessage());
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldIgnoreConstructorsOthersThanWhitelisted() {
        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new java.lang.Exception java.lang.String");

        SecuredResolver.initialize(environment);
        String expression = "{(new java.lang.Exception())}";
        TemplateEngine engine = TemplateEngine.templateEngine();

        engine.getValue(expression, Exception.class);
    }
}
