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

import io.gravitee.common.util.EnvironmentUtils;
import io.gravitee.el.spel.SpelTemplateEngine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * The {@link SecuredResolver} is a thread-safe singleton class which can be used to replace the default {@link ReflectiveMethodResolver} used by the {@link org.springframework.expression.spel.support.StandardEvaluationContext}.
 * <p/>
 * This method resolver is particularly useful to restrict usage to a subset of methods in an EL expression.
 * <p/>
 * By default, the whitelist methods are loaded from whitelist file located in the classpath.
 * The list can be either replaced either completed specifying a 'el.whitelist.list' configuration (array).
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SecuredResolver {

    private static final Logger logger = LoggerFactory.getLogger(SecuredResolver.class);
    private static final Method[] EMPTY = new Method[0];
    public static final String WHITELIST_MODE = "append";
    public static final String EL_WHITELIST_MODE_KEY = "el.whitelist.mode";
    public static final String EL_WHITELIST_LIST_KEY = "el.whitelist.list";
    public static final String WHITELIST_METHOD_PREFIX = "method ";
    public static final String WHITELIST_CLASS_PREFIX = "class ";
    static final String WHITELIST_CONSTRUCTOR_PREFIX = "new ";

    private static SecuredResolver INSTANCE;
    private static final Map<Class<?>, Method[]> methodsByType = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> methodsByTypeAndSuperTypes = new ConcurrentHashMap<>();
    private static final Set<Constructor> allConstructors = ConcurrentHashMap.newKeySet();

    /**
     * Initialize the method resolver loading all whitelisted methods from environment configuration and / or built-in whitelist.
     * Once initialized, instance of {@link SecuredResolver} can be retrieved using {@link SecuredResolver#INSTANCE}.
     *
     * @param environment an optional environment, if <code>null</code>, only built-in whitelist will be initialized.
     */
    public static void initialize(@Nullable Environment environment) {
        loadWhitelistMethods(environment);

        // Force instance creation if not already done.
        getInstance();
    }

    static SecuredResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SecuredResolver();
        }

        return INSTANCE;
    }

    private SecuredResolver() {}

    protected Method[] getMethods(Class<?> type) {
        if (methodsByTypeAndSuperTypes.containsKey(type)) {
            return methodsByTypeAndSuperTypes.get(type);
        }

        Method[] methods = methodsByType.getOrDefault(type, EMPTY);

        if (type.getSuperclass() != null) {
            methods = Stream.concat(Arrays.stream(methods), Arrays.stream(getMethods(type.getSuperclass()))).toArray(Method[]::new);
        }

        for (Class<?> anInterface : type.getInterfaces()) {
            methods = Stream.concat(Arrays.stream(methods), Arrays.stream(getMethods(anInterface))).toArray(Method[]::new);
        }

        methodsByTypeAndSuperTypes.put(type, methods);

        return methods;
    }

    protected boolean isConstructorAllowed(Constructor<?> constructor) {
        return allConstructors.contains(constructor);
    }

    private static void loadWhitelistMethods(Environment environment) {
        List<Method> methods = new ArrayList<>();
        List<Constructor<?>> constructors = new ArrayList<>();
        boolean loadBuiltInWhitelist = true;

        // Load whitelist from configuration.
        if (environment != null) {
            // Built-in whitelist will not be loaded if mode is not 'append' (ie: set to 'replace').
            loadBuiltInWhitelist = WHITELIST_MODE.equals(environment.getProperty(EL_WHITELIST_MODE_KEY, WHITELIST_MODE));

            Collection<Object> configWhitelist = EnvironmentUtils
                .getPropertiesStartingWith((ConfigurableEnvironment) environment, EL_WHITELIST_LIST_KEY)
                .values();

            for (Object declaration : configWhitelist) {
                parseDeclaration(String.valueOf(declaration), methods, constructors);
            }
        }

        // Load built-in whitelist if required.
        if (loadBuiltInWhitelist) {
            InputStream input = SpelTemplateEngine.class.getResourceAsStream("/whitelist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String declaration;

            try {
                while ((declaration = reader.readLine()) != null) {
                    parseDeclaration(declaration, methods, constructors);
                }
            } catch (IOException ioe) {
                logger.error("Unable to read EL built-in whitelist", ioe);
            }
        }

        methodsByType.clear();
        methodsByType.putAll(
            methods
                .stream()
                .collect(Collectors.groupingBy(Method::getDeclaringClass))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toArray(EMPTY)))
        );

        methodsByTypeAndSuperTypes.clear();
        allConstructors.addAll(constructors);
    }

    private static void parseDeclaration(String declaration, List<Method> methods, List<Constructor<?>> constructors) {
        try {
            if (declaration.startsWith(WHITELIST_METHOD_PREFIX)) {
                methods.add(parseMethod(declaration));
            } else if (declaration.startsWith(WHITELIST_CONSTRUCTOR_PREFIX)) {
                constructors.add(parseConstructor(declaration));
            } else if (declaration.startsWith(WHITELIST_CLASS_PREFIX)) {
                methods.addAll(parseAllMethods(declaration));
                constructors.addAll(parseAllConstructors(declaration));
            }
        } catch (Exception e) {
            logger.warn("The EL whitelisted declaration [{}] cannot be loaded. Message is [{}]", declaration, e.toString());
        }
    }

    private static Method parseMethod(String declaration) throws Exception {
        String[] split = declaration.split(" ");
        String clazzName = split[1];
        String methodName = split[2];
        String[] methodArgs = {};

        if (split.length > 3) {
            methodArgs = Arrays.copyOfRange(split, 3, split.length);
        }

        Class<?>[] argumentClasses = new Class<?>[methodArgs.length];

        for (int i = 0; i < methodArgs.length; i++) {
            argumentClasses[i] = ClassUtils.forName(methodArgs[i], SpelTemplateContext.class.getClassLoader());
        }

        Class<?> clazz = ClassUtils.forName(clazzName, SpelTemplateContext.class.getClassLoader());
        return clazz.getDeclaredMethod(methodName, argumentClasses);
    }

    private static List<Method> parseAllMethods(String declaration) throws Exception {
        String[] split = declaration.split(" ");
        String clazzName = split[1];
        Class<?> clazz = ClassUtils.forName(clazzName, SpelTemplateContext.class.getClassLoader());

        return Arrays.asList(clazz.getDeclaredMethods());
    }

    private static Constructor parseConstructor(String declaration) throws Exception {
        String[] split = declaration.split(" ");
        String clazzName = split[1];
        String[] methodArgs = {};

        if (split.length > 2) {
            methodArgs = Arrays.copyOfRange(split, 2, split.length);
        }

        Class<?>[] argumentClasses = new Class<?>[methodArgs.length];

        for (int i = 0; i < methodArgs.length; i++) {
            argumentClasses[i] = ClassUtils.forName(methodArgs[i], SecuredResolver.class.getClassLoader());
        }

        Class<?> clazz = ClassUtils.forName(clazzName, SecuredResolver.class.getClassLoader());

        return clazz.getDeclaredConstructor(argumentClasses);
    }

    private static List<Constructor<?>> parseAllConstructors(String declaration) throws Exception {
        String[] split = declaration.split(" ");
        String clazzName = split[1];
        Class<?> clazz = ClassUtils.forName(clazzName, SecuredResolver.class.getClassLoader());

        return Arrays.asList(clazz.getDeclaredConstructors());
    }
}
