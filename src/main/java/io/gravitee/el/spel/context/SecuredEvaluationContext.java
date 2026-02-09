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

import java.util.*;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.*;
import org.springframework.expression.spel.support.*;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SecuredEvaluationContext implements EvaluationContext {

    // Secured constructor resolver to only allow whitelisted constructors.
    private static final List<ConstructorResolver> constructorResolvers = Collections.singletonList(new SecuredContructorResolver());

    // Read only property access.
    private static final List<PropertyAccessor> propertyAccessors = Arrays.asList(
        DataBindingPropertyAccessor.forReadOnlyAccess(),
        new HttpHeadersPropertyAccessor(),
        new ReadOnlyMapAccessor()
    );

    // Secure method resolver to allow only whitelisted methods.
    private static final List<MethodResolver> methodResolvers = Collections.singletonList(new SecuredMethodResolver());

    private static final DefaultConversionService conversionService = new DefaultConversionService();

    static {
        conversionService.addConverter(new ReactiveValueConverter());
    }

    // Standards.
    private static final TypeLocator typeLocator = new StandardTypeLocator();
    private static final TypeConverter typeConverter = new StandardTypeConverter(conversionService);
    private static final TypeComparator typeComparator = new StandardTypeComparator();
    private static final OperatorOverloader operatorOverloader = new StandardOperatorOverloader();

    protected TypedValue rootObject;

    private final Map<String, Object> variables = new HashMap<>();

    public SecuredEvaluationContext() {
        // No root object by default.
        rootObject = TypedValue.NULL;
    }

    @Override
    public TypedValue getRootObject() {
        return this.rootObject;
    }

    @Override
    public List<PropertyAccessor> getPropertyAccessors() {
        return propertyAccessors;
    }

    @Override
    public List<ConstructorResolver> getConstructorResolvers() {
        return constructorResolvers;
    }

    @Override
    public List<MethodResolver> getMethodResolvers() {
        return methodResolvers;
    }

    @Override
    public BeanResolver getBeanResolver() {
        // No bean resolver allowed.
        return null;
    }

    @Override
    public TypeLocator getTypeLocator() {
        return typeLocator;
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    public TypeComparator getTypeComparator() {
        return typeComparator;
    }

    @Override
    public OperatorOverloader getOperatorOverloader() {
        return operatorOverloader;
    }

    @Override
    public void setVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    @Override
    public Object lookupVariable(String name) {
        return this.variables.get(name);
    }

    void putVariables(Map<String, Object> variables) {
        this.variables.putAll(variables);
    }

    Map<String, Object> getVariables() {
        return this.variables;
    }
}
