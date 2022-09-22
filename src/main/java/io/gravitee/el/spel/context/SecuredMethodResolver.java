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

import io.reactivex.rxjava3.annotations.NonNull;
import java.lang.reflect.Method;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;

/**
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SecuredMethodResolver extends ReflectiveMethodResolver {

    public static final String EL_WHITELIST_MODE_KEY = "el.whitelist.mode";
    public static final String EL_WHITELIST_LIST_KEY = "el.whitelist.list";

    public static SecuredResolver securedResolver = SecuredResolver.getInstance();

    @NonNull
    @Override
    public Method[] getMethods(@NonNull Class<?> type) {
        return securedResolver.getMethods(type);
    }
}
