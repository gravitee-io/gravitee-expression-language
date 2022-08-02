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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
public class HttpHeadersPropertyAccessorTest {

    private HttpHeadersPropertyAccessor cut;

    @BeforeEach
    public void setUp() {
        cut = new HttpHeadersPropertyAccessor();
    }

    @Test
    public void canRead() throws AccessException {
        assertThat(cut.canRead(mock(EvaluationContext.class), HttpHeaders.create(), "property")).isTrue();
    }

    @Test
    public void canNotRead() throws AccessException {
        assertThat(cut.canRead(mock(EvaluationContext.class), HttpHeaderNames.ACCEPT, "property")).isFalse();
    }

    @Test
    public void shouldRead() throws AccessException {
        final HttpHeaders headers = HttpHeaders.create();
        headers.add("Header", "value1").add("Header", "value2");

        final TypedValue result = cut.read(mock(EvaluationContext.class), headers, "header");

        assertThat(result).isNotNull();
        assertThat(result.getTypeDescriptor()).isNotNull();
        assertThat(result.getTypeDescriptor().isAssignableTo(TypeDescriptor.valueOf(Collection.class))).isTrue();
        assertThat((Collection<String>) result.getValue()).hasSize(2).contains("value1", "value2");
    }

    @Test
    public void shouldReadNullValue() throws AccessException {
        final TypedValue result = cut.read(mock(EvaluationContext.class), HttpHeaders.create(), "header");

        assertThat(result).isNotNull().isEqualTo(TypedValue.NULL);
    }

    @Test
    public void shouldReadEmptyValue() throws AccessException {
        final HttpHeaders headers = HttpHeaders.create();
        headers.add("header", Collections.emptyList());

        final TypedValue result = cut.read(mock(EvaluationContext.class), headers, "header");

        assertThat(result).isNotNull().isEqualTo(TypedValue.NULL);
    }
}
