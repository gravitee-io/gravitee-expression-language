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

import io.gravitee.el.spel.function.EscapeFunctionUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Utility class to escape XML content for safe use in XML/SOAP documents.
 * Provides static methods that can be called directly from EL expressions.
 *
 * <p>This class helps prevent XML/SOAP injection vulnerabilities by properly escaping
 * special XML characters. It can be used in Gravitee policies like REST-to-SOAP
 * to safely handle user input before embedding it in XML documents.</p>
 *
 * <p>Usage in EL expressions:</p>
 * <pre>{@code
 * {#xmlEscape(#request.params['userInput'])}
 * {#xmlEscape(#request.body)}
 * {#xmlEscape('Hello <world>')}
 * }</pre>
 *
 * <p>The function uses Apache Commons Text StringEscapeUtils.escapeXml10() to escape
 * XML content according to XML 1.0 specification. This includes:</p>
 * <ul>
 *   <li>{@code &} → {@code &amp;}</li>
 *   <li>{@code <} → {@code &lt;}</li>
 *   <li>{@code >} → {@code &gt;}</li>
 *   <li>{@code "} → {@code &quot;}</li>
 *   <li>{@code '} → {@code &apos;}</li>
 *   <li>Invalid control characters are removed (XML 1.0 compliance)</li>
 * </ul>
 *
 * @author GraviteeSource Team
 * @since 4.1.3
 */
public final class XmlEscapeFunction {

    private XmlEscapeFunction() {}

    /**
     * Escapes XML content for safe use in XML documents using Apache Commons Text.
     * Uses StringEscapeUtils.escapeXml10() for XML 1.0 compliant escaping.
     * Works for both element content and attributes.
     *
     * <p>This method handles different input types:</p>
     * <ul>
     *   <li>{@code String} - used directly (most common case)</li>
     *   <li>{@code Collection/Array} - elements joined with space</li>
     *   <li>{@code Number} - converted to string via {@code toString()}</li>
     *   <li>{@code Boolean} - converted to string via {@code toString()}</li>
     *   <li>{@code null} - returns {@code null}</li>
     *   <li>Other objects - converted to string via {@code toString()}</li>
     * </ul>
     *
     * <p>Examples:</p>
     * <pre>{@code
     * evaluate("Hello <world>") → "Hello &lt;world&gt;"
     * evaluate(["1</web:id><web:id>2"]) → "1&lt;/web:id&gt;&lt;web:id&gt;2"
     * evaluate(["a", "b"]) → "a b"
     * evaluate(123) → "123"
     * evaluate(true) → "true"
     * evaluate(null) → null
     * }</pre>
     *
     * @param input the object to escape (String, Collection, Array, Number, Boolean, or any object with toString())
     * @return the escaped string safe for XML use (XML 1.0 compliant), or {@code null} if input is {@code null}
     * @implNote Uses Apache Commons Text StringEscapeUtils.escapeXml10() for XML escaping
     */
    public static String evaluate(Object input) {
        if (input == null) {
            return null;
        }
        return StringEscapeUtils.escapeXml10(EscapeFunctionUtils.normalizeToText(input));
    }
}
