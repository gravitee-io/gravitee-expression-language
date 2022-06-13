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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.gravitee.el.spel.CachedExpression;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.EvaluationContext;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
class SpelTemplateContextTest {

    protected static final String VARIABLE_NAME = "test";
    protected static final String VARIABLE_VALUE = "testValue";
    protected static final String MOCK_EXCEPTION = "Mock exception";

    @Mock
    private CachedExpression cachedExpression;

    private SpelTemplateContext cut;

    @BeforeEach
    void init() {
        cut = new SpelTemplateContext();
    }

    @Test
    void shouldHaveDefaultJsonPathAndXPathVariables() {
        assertNotNull(cut.lookupVariable("jsonPath"));
        assertNotNull(cut.lookupVariable("xpath"));
    }

    @Test
    void shouldSetVariable() {
        final String value = VARIABLE_VALUE;
        cut.setVariable(VARIABLE_NAME, value);
        assertEquals(value, cut.lookupVariable(VARIABLE_NAME));
    }

    @Test
    void shouldTriggerCompletable() {
        final Completable completable = Completable.complete();

        when(cachedExpression.getVariables()).thenReturn(Set.of(VARIABLE_NAME));
        cut.setDeferredVariable(VARIABLE_NAME, completable);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();

        obs.assertValueCount(1);
        obs.assertComplete();
    }

    @Test
    void shouldTriggerMaybe() {
        final Maybe<String> maybe = Maybe.just(VARIABLE_VALUE);

        when(cachedExpression.getVariables()).thenReturn(Set.of(VARIABLE_NAME));
        cut.setDeferredVariable(VARIABLE_NAME, maybe);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();

        obs.assertValue(evaluationContext -> {
            assertEquals(VARIABLE_VALUE, evaluationContext.lookupVariable(VARIABLE_NAME));
            return true;
        });
        obs.assertComplete();
    }

    @Test
    void shouldTriggerSingle() {
        final Single<String> single = Single.just(VARIABLE_VALUE);

        when(cachedExpression.getVariables()).thenReturn(Set.of(VARIABLE_NAME));
        cut.setDeferredVariable(VARIABLE_NAME, single);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();

        obs.assertValue(evaluationContext -> {
            assertEquals(VARIABLE_VALUE, evaluationContext.lookupVariable(VARIABLE_NAME));
            return true;
        });
        obs.assertComplete();
    }

    @Test
    void shouldTriggerMaybeWhenSubVariableExpression() {
        final Maybe<String> maybe = Maybe.just(VARIABLE_VALUE);

        when(cachedExpression.getVariables()).thenReturn(Set.of(VARIABLE_NAME + ".sub.variable"));
        cut.setDeferredVariable(VARIABLE_NAME, maybe);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();

        obs.assertValue(evaluationContext -> {
            assertEquals(VARIABLE_VALUE, evaluationContext.lookupVariable(VARIABLE_NAME));
            return true;
        });
        obs.assertComplete();
    }

    @Test
    void shouldTriggerMultipleDeferredVariables() {
        final Maybe<String> maybe1 = Maybe.just(VARIABLE_VALUE + "1");
        final Maybe<String> maybe2 = Maybe.just(VARIABLE_VALUE + "2");
        final Maybe<String> maybe3 = Maybe.just(VARIABLE_VALUE + "3");
        final Maybe<String> maybe4 = Maybe.just(VARIABLE_VALUE + "4");

        when(cachedExpression.getVariables()).thenReturn(Set.of(VARIABLE_NAME + "1", VARIABLE_NAME + "3", VARIABLE_NAME + "4"));
        cut.setDeferredVariable(VARIABLE_NAME + "1", maybe1);
        cut.setDeferredVariable(VARIABLE_NAME + "2", maybe2); // Not in the list of variables. Should not be resolved.
        cut.setDeferredVariable(VARIABLE_NAME + "3", maybe3);
        cut.setDeferredVariable(VARIABLE_NAME + "4", maybe4);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();

        obs.assertValue(evaluationContext -> {
            assertEquals(VARIABLE_VALUE + "1", evaluationContext.lookupVariable(VARIABLE_NAME + "1"));
            assertEquals(VARIABLE_VALUE + "3", evaluationContext.lookupVariable(VARIABLE_NAME + "3"));
            assertEquals(VARIABLE_VALUE + "4", evaluationContext.lookupVariable(VARIABLE_NAME + "4"));
            assertNull(evaluationContext.lookupVariable(VARIABLE_VALUE + "2"));
            return true;
        });
        obs.assertComplete();
    }

    @Test
    void shouldErrorWhenResolutionReturnsError() {
        final Single<String> single = Single.error(new RuntimeException(MOCK_EXCEPTION));

        when(cachedExpression.getVariables()).thenReturn(Set.of(VARIABLE_NAME));
        cut.setDeferredVariable(VARIABLE_NAME, single);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();
        obs.assertErrorMessage(MOCK_EXCEPTION);
    }

    @Test
    void shouldNotAddVariableIfNotUsedInTheExpression() {
        final Single<String> single = Single.just(VARIABLE_VALUE);

        when(cachedExpression.getVariables()).thenReturn(Set.of());
        cut.setDeferredVariable(VARIABLE_NAME, single);

        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();

        obs.assertValue(evaluationContext -> {
            assertNull(evaluationContext.lookupVariable(VARIABLE_NAME));
            return true;
        });
        obs.assertComplete();
    }

    @Test
    void shouldThrowExceptionWhenDeferredVariableIsNull() {
        assertThrows(NullPointerException.class, () -> cut.setDeferredVariable(VARIABLE_NAME, (Completable) null));
    }

    @Test
    void shouldJustReturnContextIfNoDeferredVariableSet() {
        final TestObserver<EvaluationContext> obs = cut.evaluationContext(cachedExpression).test();
        obs.assertValueCount(1);
        obs.assertComplete();
    }
}
