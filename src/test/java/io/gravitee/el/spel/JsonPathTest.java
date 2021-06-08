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

import io.gravitee.el.spel.function.json.JsonPathFunction;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Test;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JsonPathTest {

    @Test
    public void shouldNotGetValueFromCache() throws IOException {
        Map<String, String> context1 = new HashMap<>();
        context1.put("key", "first");
        Object value = JsonPathFunction.evaluate(context1, "concat(\"/\", $.key)");
        Assert.assertEquals("/first", value);

        Map<String, String> context2 = new HashMap<>();
        context2.put("key", "second");
        value = JsonPathFunction.evaluate(context2, "concat(\"/\", $.key)");
        Assert.assertEquals("/second", value);
    }
}
