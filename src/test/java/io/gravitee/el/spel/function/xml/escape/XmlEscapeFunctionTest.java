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
package io.gravitee.el.spel.function.xml.escape;

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.el.TemplateEngine;
import io.gravitee.el.spel.context.SecuredContructorResolver;
import io.gravitee.el.spel.context.SecuredMethodResolver;
import io.gravitee.el.spel.context.SecuredResolver;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class XmlEscapeFunctionTest {

    @BeforeEach
    void setUp() {
        reinitSecuredResolver();
    }

    private void reinitSecuredResolver() {
        ReflectionTestUtils.setField(SecuredResolver.class, "instance", null);
        SecuredResolver.initialize(null);

        SecuredResolver instance = ReflectionTestUtils.invokeMethod(SecuredResolver.class, "getInstance");
        ReflectionTestUtils.setField(SecuredMethodResolver.class, "securedResolver", instance);
        ReflectionTestUtils.setField(SecuredContructorResolver.class, "securedResolver", instance);
    }

    @Test
    void should_escape_basic_xml_characters() {
        TemplateEngine engine = TemplateEngine.templateEngine();

        // Test script tag
        TestObserver<String> obs1 = engine.eval("{#xmlEscape('<script>alert(\"xss\")</script>')}", String.class).test();
        obs1.assertComplete();
        assertThat(obs1.values().get(0)).isEqualTo("&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;");

        // Test quotes
        TestObserver<String> obs2 = engine.eval("{#xmlEscape('He said \"Hello\"')}", String.class).test();
        obs2.assertComplete();
        assertThat(obs2.values().get(0)).isEqualTo("He said &quot;Hello&quot;");

        // Test single quotes
        TestObserver<String> obs3 = engine.eval("{#xmlEscape(\"It's working\")}", String.class).test();
        obs3.assertComplete();
        assertThat(obs3.values().get(0)).isEqualTo("It&apos;s working");

        // Test angle brackets
        TestObserver<String> obs4 = engine.eval("{#xmlEscape('Hello <world>')}", String.class).test();
        obs4.assertComplete();
        assertThat(obs4.values().get(0)).isEqualTo("Hello &lt;world&gt;");

        // Test ampersand
        TestObserver<String> obs5 = engine.eval("{#xmlEscape('Test & more')}", String.class).test();
        obs5.assertComplete();
        assertThat(obs5.values().get(0)).isEqualTo("Test &amp; more");
    }

    @Test
    void should_handle_null_input_in_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        TestObserver<String> obs = engine.eval("{#xmlEscape(null)}", String.class).test();
        obs.assertComplete();
        // null input should result in no value emitted
        assertThat(obs.values()).isEmpty();
    }

    @Test
    void should_handle_empty_string_in_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        TestObserver<String> obs = engine.eval("{#xmlEscape(\"\")}", String.class).test();
        obs.assertComplete();
        String result = obs.values().get(0);
        assertThat(result).isEmpty();
    }

    @Test
    void should_handle_different_object_types() {
        TemplateEngine engine = TemplateEngine.templateEngine();

        // String
        TestObserver<String> stringObs = engine.eval("{#xmlEscape('Hello <world>')}", String.class).test();
        stringObs.assertResult("Hello &lt;world&gt;");

        // Number
        TestObserver<String> numberObs = engine.eval("{#xmlEscape(123)}", String.class).test();
        numberObs.assertResult("123");

        // Boolean
        TestObserver<String> booleanObs = engine.eval("{#xmlEscape(true)}", String.class).test();
        booleanObs.assertResult("true");

        // Float
        TestObserver<String> floatObs = engine.eval("{#xmlEscape(42.5)}", String.class).test();
        floatObs.assertResult("42.5");
    }

    @Test
    void should_handle_mixed_content_in_el() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        TestObserver<String> obs = engine.eval("{#xmlEscape('Hello <world> & \"friends\"')}", String.class).test();
        obs.assertComplete();
        String result = obs.values().get(0);
        assertThat(result).isEqualTo("Hello &lt;world&gt; &amp; &quot;friends&quot;");
    }

    @Test
    void should_escape_injection_patterns() {
        TemplateEngine engine = TemplateEngine.templateEngine();

        String[] patterns = {
            "<script>alert(1)</script>",
            "<iframe src=test></iframe>",
            "<img src=x onerror=alert(1)>",
            "<svg onload=alert(1)>",
            "<body onload=alert(1)>",
        };

        for (String pattern : patterns) {
            TestObserver<String> obs = engine.eval("{#xmlEscape('" + pattern + "')}", String.class).test();
            obs.assertComplete();
            String result = obs.values().get(0);

            assertThat(result).doesNotContain("<", ">").contains("&lt;", "&gt;");
        }
    }

    @Test
    void should_escape_soap_specific_content() {
        TemplateEngine engine = TemplateEngine.templateEngine();
        TestObserver<String> obs = engine.eval("{#xmlEscape('<soap:Body><name>John</name></soap:Body>')}", String.class).test();
        obs.assertComplete();
        String result = obs.values().get(0);
        assertThat(result).isEqualTo("&lt;soap:Body&gt;&lt;name&gt;John&lt;/name&gt;&lt;/soap:Body&gt;");
    }

    @Test
    void should_handle_special_characters() {
        TemplateEngine engine = TemplateEngine.templateEngine();

        // Unicode characters
        TestObserver<String> obs1 = engine.eval("{#xmlEscape('Hello 世界 <test>')}", String.class).test();
        obs1.assertComplete();
        assertThat(obs1.values().get(0)).isEqualTo("Hello 世界 &lt;test&gt;");

        // Control characters (XML 1.0 removes invalid control characters)
        TestObserver<String> obs2 = engine.eval("{#xmlEscape('Hello\u0001World')}", String.class).test();
        obs2.assertComplete();
        assertThat(obs2.values().get(0)).isEqualTo("HelloWorld");

        // Emoji
        TestObserver<String> obs3 = engine.eval("{#xmlEscape('Emoji 🚀 <tag>')}", String.class).test();
        obs3.assertComplete();
        assertThat(obs3.values().get(0)).isEqualTo("Emoji 🚀 &lt;tag&gt;");
    }

    @Test
    void should_handle_collections_and_arrays() {
        TemplateEngine engine = TemplateEngine.templateEngine();

        // Test single element collection
        TestObserver<String> obs1 = engine.eval("{#xmlEscape({'1</web:id><web:id>2'})}", String.class).test();
        obs1.assertComplete();
        assertThat(obs1.values().get(0)).isEqualTo("1&lt;/web:id&gt;&lt;web:id&gt;2");

        // Test multiple elements collection
        TestObserver<String> obs2 = engine.eval("{#xmlEscape({'hello', 'world'})}", String.class).test();
        obs2.assertComplete();
        assertThat(obs2.values().get(0)).isEqualTo("hello world");

        // Test empty collection
        TestObserver<String> obs3 = engine.eval("{#xmlEscape({})}", String.class).test();
        obs3.assertComplete();
        assertThat(obs3.values().get(0)).isEmpty();

        // Test array with a single element
        TestObserver<String> obs4 = engine.eval("{#xmlEscape({'<test>value</test>'})}", String.class).test();
        obs4.assertComplete();
        assertThat(obs4.values().get(0)).isEqualTo("&lt;test&gt;value&lt;/test&gt;");

        // Test array with multiple elements
        TestObserver<String> obs5 = engine.eval("{#xmlEscape({'<a>', '<b>'})}", String.class).test();
        obs5.assertComplete();
        assertThat(obs5.values().get(0)).isEqualTo("&lt;a&gt; &lt;b&gt;");
    }

    @Test
    void should_handle_xml_injection_attack() {
        String injectionPayload = "1</web:id><web:id>2";
        String result = XmlEscapeFunction.evaluate(injectionPayload);

        assertThat(result).isEqualTo("1&lt;/web:id&gt;&lt;web:id&gt;2").doesNotContain("<", ">").contains("&lt;", "&gt;");
    }
}
