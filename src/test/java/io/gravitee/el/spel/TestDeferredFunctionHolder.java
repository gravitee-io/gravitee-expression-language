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

import io.gravitee.el.spel.context.DeferredFunctionHolder;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TestDeferredFunctionHolder implements DeferredFunctionHolder {

    private final Integer delay;

    public TestDeferredFunctionHolder() {
        this.delay = null;
    }

    public TestDeferredFunctionHolder(int delay) {
        this.delay = delay;
    }

    public Single<Integer> getIndex(int index) {
        return Single.just(index);
    }

    public Maybe<String> get(String val1, String val2) {
        Maybe<String> maybe = Maybe.just("resolved('" + val1 + "', '" + val2 + "')");

        if (delay != null) {
            return maybe.delay(delay, TimeUnit.MILLISECONDS);
        }

        return maybe;
    }

    public Maybe<List<String>> getList(String val1, String val2) {
        Maybe<List<String>> maybe = Maybe.just(List.of("resolved('" + val1 + "')", "resolged('" + val2 + "')"));

        if (delay != null) {
            return maybe.delay(delay, TimeUnit.MILLISECONDS);
        }

        return maybe;
    }
}
