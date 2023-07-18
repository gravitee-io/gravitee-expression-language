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

import io.reactivex.rxjava3.annotations.NonNull;
import java.util.Map;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;

/**
 * @author Guillaume CUSNIEUX (guillaume.cusnieux at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ReadOnlyMapAccessor extends MapAccessor {

    @NonNull
    @Override
    public TypedValue read(@NonNull EvaluationContext context, Object target, @NonNull String name) throws AccessException {
        Map<?, ?> map = (Map<?, ?>) target;
        Object value = map.get(name);
        if (value == null && !map.containsKey(name)) {
            return TypedValue.NULL;
        } else {
            return new TypedValue(value);
        }
    }

    @Override
    public boolean canRead(@NonNull EvaluationContext context, Object target, @NonNull String name) throws AccessException {
        return target instanceof Map;
    }

    @Override
    public boolean canWrite(@NonNull EvaluationContext context, Object target, @NonNull String name) throws AccessException {
        return false;
    }
}
