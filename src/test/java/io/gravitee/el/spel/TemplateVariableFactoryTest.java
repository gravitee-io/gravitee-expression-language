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
import org.springframework.core.io.support.SpringFactoriesLoader;

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
        AbstractSpringFactoriesLoaderTemplateVariableProviderFactory underTest = new TestFactory(applicationContext);
        assertThat(SpringFactoriesLoader.forDefaultResourceLocation().load(TemplateVariableProvider.class)).hasSize(3);
        assertThat(underTest.getTemplateVariableProviders()).hasSize(1);
        assertThat(underTest.getTemplateVariableProviders().get(0)).isInstanceOf(APITemplateVariableProvider.class);
    }

    static class TestFactory extends AbstractSpringFactoriesLoaderTemplateVariableProviderFactory {

        public TestFactory(ApplicationContext applicationContext) {
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
