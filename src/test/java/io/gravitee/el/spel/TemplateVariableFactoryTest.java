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
package io.gravitee.el.spel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.gravitee.el.AbstractSpringFactoriesLoaderTemplateVariableProviderFactory;
import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateVariableProvider;
import io.gravitee.el.TemplateVariableScope;
import io.gravitee.el.annotations.TemplateVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * @author Benoit BORDIGONI (benoit.bordigoni at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
class TemplateVariableFactoryTest {

    @Mock
    ApplicationContext applicationContext;

    @Test
    void should_find_only_one_provider() {
        AbstractSpringFactoriesLoaderTemplateVariableProviderFactory underTest = new TestApiScopeVariableProviderFactory(
            applicationContext
        );

        // mimic what BeanFactoryUtils.beanNamesForTypeIncludingAncestors should return
        when(applicationContext.getBeanNamesForType(TemplateVariableProvider.class))
            .thenReturn(
                new String[] {
                    APITemplateVariableProvider.class.getSimpleName(),
                    WrongScopeTemplateVariableProvider.class.getSimpleName(),
                    UnAnnotatedTestTemplateProvider.class.getSimpleName(),
                }
            );
        when(applicationContext.getParentBeanFactory()).thenReturn(null);

        // mimic beans being returned
        when(applicationContext.getBean(APITemplateVariableProvider.class.getSimpleName())).thenReturn(new APITemplateVariableProvider());
        when(applicationContext.getBean(WrongScopeTemplateVariableProvider.class.getSimpleName()))
            .thenReturn(new UnAnnotatedTestTemplateProvider());
        when(applicationContext.getBean(UnAnnotatedTestTemplateProvider.class.getSimpleName()))
            .thenReturn(new WrongScopeTemplateVariableProvider());

        // check that among those three beans only one is selected
        assertThat(underTest.getTemplateVariableProviders()).hasSize(1);
        assertThat(underTest.getTemplateVariableProviders().get(0)).isInstanceOf(APITemplateVariableProvider.class);
    }

    static class TestApiScopeVariableProviderFactory extends AbstractSpringFactoriesLoaderTemplateVariableProviderFactory {

        public TestApiScopeVariableProviderFactory(ApplicationContext applicationContext) {
            super(applicationContext);
        }

        @Override
        public TemplateVariableScope getTemplateVariableScope() {
            return TemplateVariableScope.API;
        }
    }

    @TemplateVariable(scopes = TemplateVariableScope.API)
    public static class APITemplateVariableProvider implements TemplateVariableProvider {

        @Override
        public void provide(TemplateContext templateContext) {
            // just testing the class
        }
    }

    static class UnAnnotatedTestTemplateProvider implements TemplateVariableProvider {

        @Override
        public void provide(TemplateContext templateContext) {
            // just testing the class
        }
    }

    @TemplateVariable(scopes = TemplateVariableScope.HEALTH_CHECK)
    static class WrongScopeTemplateVariableProvider implements TemplateVariableProvider {

        @Override
        public void provide(TemplateContext templateContext) {
            // just testing the class
        }
    }
}
