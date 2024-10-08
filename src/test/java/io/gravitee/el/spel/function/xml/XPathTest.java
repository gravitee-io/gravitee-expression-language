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
package io.gravitee.el.spel.function.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class XPathTest {

    @Test
    public void should_extract_value_with_xPath() {
        final String input =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<root>" +
            "<lastname>DOE</lastname>" +
            "<firstname>JOHN</firstname>" +
            "<age>35</age>" +
            "</root>";

        Object lastname = XPathFunction.evaluate(input, ".//lastname");
        Assertions.assertEquals("DOE", lastname);
    }

    @Test
    public void should_extract_child_node_value() {
        final String input =
            """
            <S:Example xmlns:S="http://www.w3.org/2003/05/soap-envelope">
                <S:Body>
                    <Foo xmlns:ns2="dummy:example:ns" xmlns="another:dummy:ns">
                        <Priority>500</Priority>
                        <ListRessource>
                            <Ressource>
                                <Bar>/Baz</Bar>
                            </Ressource>
                        </ListRessource>
                    </Foo>
                </S:Body>
            </S:Example>
        """;
        var result = XPathFunction.evaluate(input, ".//Bar");
        Assertions.assertEquals("/Baz", result);
    }
}
