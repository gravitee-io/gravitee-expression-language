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
package io.gravitee.el.spel;

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
    }

    @Test
    public void shouldTransformWithRequestHeader() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAll(new HashMap<String, String>() {
            {
                put("X-Gravitee-Endpoint", "my_api_host");
            }
        });

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        String value = engine.convert("{#request.headers['X-Gravitee-Endpoint']}");
        Assert.assertEquals("my_api_host", value);
    }

    @Test
    public void shouldTransformWithRequestHeader_getValue() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAll(new HashMap<String, String>() {
            {
                put("X-Gravitee-Endpoint", "my_api_host");
            }
        });

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/99/products/123456");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));

        EvaluableRequest value = engine.getValue("{#request}", EvaluableRequest.class);
        HttpHeaders headersValue = engine.getValue("{#request.headers}", HttpHeaders.class);
        Assert.assertEquals("my_api_host", value.getHeaders().getFirst("X-Gravitee-Endpoint"));
        Assert.assertEquals("my_api_host", headersValue.getFirst("X-Gravitee-Endpoint"));
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
        Assert.assertEquals("myparam", value);
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
        Assert.assertEquals("myparam", value.getParams().getFirst("param"));
        Assert.assertEquals("myparam", paramsValue.getFirst("param"));
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
        Assert.assertEquals("myparam2", value);
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
        Assert.assertEquals("myparam2", value.getParams().get("param").get(1));
        Assert.assertEquals("myparam2", paramsValue.get("param").get(1));
    }

    @Test
    public void shouldTransformWithProperties() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAll(new HashMap<String, String>() {
            {
                put("X-Gravitee-Endpoint", "my_api_host");
            }
        });

        when(request.headers()).thenReturn(headers);
        when(request.path()).thenReturn("/stores/123");

        final Map<String, Object> properties = new HashMap<>();
        properties.put("name_123", "Doe");
        properties.put("firstname_123", "John");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", new EvaluableRequest(request));
        engine.getTemplateContext().setVariable("properties", properties);

        String value = engine.convert("<user><id>{#request.paths[2]}</id><name>{#properties['name_123']}</name><firstname>{#properties['firstname_' + #request.paths[2]]}</firstname></user>");
        Assert.assertNotNull(value);
    }

    @Test
    public void shouldTransformJsonContent() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAll(new HashMap<String, String>() {
            {
                put("X-Gravitee-Endpoint", "my_api_host");
            }
        });

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
        Assert.assertNotNull(value);
    }

    @Test
    public void shouldCallRandomFunction() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        String content = "age: {(T(java.lang.Math).random() * 60).intValue()}";
        String value = engine.convert(content);
        Assert.assertNotNull(value);
    }

    @Test
    public void shouldXpathFunction() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<root>" +
                "<lastname>DOE</lastname>" +
                "<firstname>JOHN</firstname>" +
                "<age>35</age>" +
                "</root>");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        String content = "{#xpath(#request.content, './/lastname')}";
        String value = engine.convert(content);
        Assert.assertEquals("DOE", value);

        content = "{#xpath(#request.content, './/age', 'number') + 20}";
        value = engine.convert(content);
        Assert.assertEquals("55.0", value);

        content = "{#xpath(#request.content, './/something')}";
        value = engine.convert(content);
        Assert.assertTrue(StringUtils.isEmpty(value));
    }

    @Test
    public void shouldJsonPathFunction() {
        EvaluableRequest req = Mockito.mock(EvaluableRequest.class);
        when(req.getContent()).thenReturn("{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35 }");

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("request", req);

        String content = "{#jsonPath(#request.content, '$.lastname')}";
        String value = engine.convert(content);
        Assert.assertEquals("DOE", value);

        content = "{#jsonPath(#request.content, '$.age') + 20}";
        value = engine.convert(content);
        Assert.assertEquals("55", value);

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

        Assert.assertTrue(success);
    }

    @Test
    public void shouldJsonPathFunctionForBoolean() {

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("profile", "{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35 }");

        String content = "{#jsonPath(#profile, '$.identity_provider_id') == 'idp_6'}";
        boolean result = engine.getValue(content , boolean.class);
        Assert.assertFalse(result);

        content = "{#jsonPath(#profile, '$.lastname') == 'DOE'}";
        result = engine.getValue(content , boolean.class);
        Assert.assertTrue(result);

        content = "{#jsonPath(#profile, '$.lastname') == 'DONE'}";
        result = engine.getValue(content , boolean.class);
        Assert.assertFalse(result);

    }

    @Test
    public void shouldJsonPathFunctionGroupsForBoolean() {

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("profile", "{ \"lastname\": \"DOE\", \"firstname\": \"JOHN\", \"age\": 35"
                + ", \"groups\" : [\"Group1\", \"Group2\", \"Group3\"]"
                + ", \"emptiness\" : []"
                + ", \"nothingness\" : null"
                + " }");

        String content = "{#jsonPath(#profile, '$.groups').contains('Group2')}";
        boolean result = engine.getValue(content , boolean.class);
        Assert.assertTrue(result);

        content = "{#jsonPath(#profile, '$.groups').contains('Group4')}";
        result = engine.getValue(content , boolean.class);
        Assert.assertFalse(result);

        content = "{#jsonPath(#profile, '$.emptiness').contains('Group4')}";
        result = engine.getValue(content , boolean.class);
        Assert.assertFalse(result);

        content = "{#jsonPath(#profile, '$.nothingness')?.contains('Group4')?:false}";
        result = engine.getValue(content , boolean.class);
        Assert.assertFalse(result);
    }
    
}
