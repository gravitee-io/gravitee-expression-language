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

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;

/**
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SecuredMethodResolver extends ReflectiveMethodResolver {

    private static final Logger logger = LoggerFactory.getLogger(SecuredMethodResolver.class);
    public static final String EL_WHITELIST_MODE_KEY = "el.whitelist.mode";
    public static final String EL_WHITELIST_LIST_KEY = "el.whitelist.list";

    public static SecuredResolver securedResolver = SecuredResolver.getInstance();

    @Override
    public Method[] getMethods(Class<?> type) {
        return securedResolver.getMethods(type);
    }
}
