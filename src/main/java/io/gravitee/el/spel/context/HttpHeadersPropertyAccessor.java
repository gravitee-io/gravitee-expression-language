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

import io.gravitee.gateway.api.http.HttpHeaders;
import java.util.List;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class HttpHeadersPropertyAccessor implements PropertyAccessor {

    private final Class<?>[] TARGET_CLASSES = new Class[] { HttpHeaders.class };

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return TARGET_CLASSES;
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return true;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        final HttpHeaders headers = (HttpHeaders) target;

        List<String> values = headers.getAll(name);

        if (values == null) {
            return TypedValue.NULL;
        }

        // We can't do that if we wanted to ensure backward compatibility for #request.headers['my-header'][0]
        // return values.size() > 1 ? new TypedValue(values) : new TypedValue(values.get(0));
        return new TypedValue(values);
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        // Do nothing
    }
}
