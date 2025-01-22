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
package io.gravitee.el;

import io.gravitee.el.annotations.TemplateVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;

/**
 * @author Benoit Bordigoni (benoit.bordigoni at graviteesource.com)
 * @author GraviteeSource Team
 */
@RequiredArgsConstructor
public abstract class AbstractSpringFactoriesLoaderTemplateVariableProviderFactory implements TemplateVariableProviderFactory {

    private List<TemplateVariableProvider> providers;

    protected final ApplicationContext applicationContext;

    @Override
    public List<TemplateVariableProvider> getTemplateVariableProviders() {
        if (providers == null) {
            providers =
                Stream
                    .of(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, TemplateVariableProvider.class))
                    .map(name -> (TemplateVariableProvider) applicationContext.getBean(name))
                    .filter(provider -> {
                        TemplateVariable annotation = provider.getClass().getAnnotation(TemplateVariable.class);
                        return annotation != null && Arrays.asList(annotation.scopes()).contains(getTemplateVariableScope());
                    })
                    .toList();
        }
        return providers;
    }
}
