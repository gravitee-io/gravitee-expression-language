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

import static io.gravitee.el.spel.context.SecuredMethodResolver.EL_WHITELIST_LIST_KEY;
import static io.gravitee.el.spel.context.SecuredMethodResolver.EL_WHITELIST_MODE_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.el.TemplateEngine;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

/**
 * Regression tests covering three distinct ways {@link SecuredResolver}'s method/constructor
 * whitelist could be bypassed, all guarding the same invariant: whitelisting a class or a
 * signature must never implicitly authorize behavior that was never independently reviewed.
 *
 * <ul>
 *   <li>{@code whitelisting_a_class_must_not_authorize_an_unwhitelisted_subclass_override}:
 *       whitelisting a class used to implicitly authorize any of its subclasses to override that
 *       class's methods, because {@link SecuredResolver#getMethods(Class)} merged in whatever an
 *       ancestor class had whitelisted without checking whether the concrete type shadows it with
 *       its own, unreviewed declaration -- and {@link java.lang.reflect.Method#invoke} dispatches
 *       virtually, so the override would run.</li>
 *   <li>{@code a_compiled_expression_must_not_bypass_the_whitelist_when_reused_against_a_different_type}:
 *       expressions are parsed once and cached process-wide, keyed only by expression text, so the
 *       same AST node -- once compiled by SpEL after enough evaluations -- can later be evaluated
 *       against an entirely unrelated runtime type bound to the same variable name.</li>
 *   <li>{@code a_compiled_constructor_expression_must_not_bypass_the_whitelist_for_a_different_overload}:
 *       the same reuse pattern, applied to constructor invocation -- warming up a {@code new}
 *       expression with arguments matching a whitelisted overload must not let a later call with
 *       arguments matching a different, non-whitelisted overload slip through.</li>
 * </ul>
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SecuredResolverWhitelistBypassTest {

    /**
     * Comfortably above SpEL's ~100-hit threshold for {@code SpelCompilerMode.MIXED} to compile a node.
     */
    private static final int EVALUATIONS_TO_FORCE_COMPILATION = 250;

    // --- Shape 1: unwhitelisted subclass overriding a whitelisted superclass method -----------

    public static class SafeBase {

        public String reveal() {
            return "safe superclass implementation";
        }
    }

    /**
     * Deliberately NOT whitelisted, but IS-A {@link SafeBase}. Its override of {@code reveal()}
     * must never run just because {@link SafeBase} is whitelisted.
     */
    public static class DangerousSubclass extends SafeBase {

        static final AtomicBoolean INVOKED = new AtomicBoolean(false);

        @Override
        public String reveal() {
            INVOKED.set(true);
            return "SANDBOX ESCAPED VIA SUBCLASS";
        }
    }

    // --- Shape 2: compiled MethodReference reused across unrelated runtime types ---------------

    public interface Greeter {
        String greet();
    }

    /**
     * Stands in for a legitimate, reviewed, EL-reachable type: explicitly added to the whitelist below.
     */
    public static class SafeGreeter implements Greeter {

        @Override
        public String greet() {
            return "hello from the whitelisted implementation";
        }
    }

    /**
     * Deliberately NOT whitelisted, but implements the same interface/method signature as
     * {@link SafeGreeter}. Its "payload" only flips an in-memory flag, standing in for whatever a
     * real attacker-reachable, non-whitelisted method could do, without actually doing it.
     */
    public static class DangerousGreeter implements Greeter {

        static final AtomicBoolean INVOKED = new AtomicBoolean(false);

        @Override
        public String greet() {
            INVOKED.set(true);
            return "SANDBOX ESCAPED VIA COMPILED REUSE";
        }
    }

    // --- Shape 3: compiled ConstructorReference reused across overloads of the same class ------

    /**
     * Exposes two overloads: only the {@code String} one is whitelisted below. If SpEL's compiler
     * caches the wrong {@link java.lang.reflect.Constructor} once compiled, calling the same
     * expression with an {@code Integer} argument could invoke the non-whitelisted overload.
     */
    public static class OverloadedCtor {

        static final AtomicBoolean DANGEROUS_CTOR_INVOKED = new AtomicBoolean(false);

        public OverloadedCtor(String safeArg) {
            // Whitelisted overload: no side effect.
        }

        public OverloadedCtor(Integer dangerousArg) {
            DANGEROUS_CTOR_INVOKED.set(true);
        }
    }

    @AfterEach
    void resetWhitelist() {
        SecuredResolverTestInitializer.reinit();
    }

    @Test
    void whitelisting_a_class_must_not_authorize_an_unwhitelisted_subclass_override() {
        DangerousSubclass.INVOKED.set(false);

        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "class " + SafeBase.class.getName());
        SecuredResolverTestInitializer.reinit(environment);

        TemplateEngine engine = TemplateEngine.templateEngine();
        engine.getTemplateContext().setVariable("subject", new DangerousSubclass());

        try {
            engine.evalNow("{#subject.reveal()}", String.class);
        } catch (Exception expectedIfWhitelistIsPerSubclass) {
            // Safe outcome: DangerousSubclass would need to be independently whitelisted.
        }

        assertThat(DangerousSubclass.INVOKED)
            .as("whitelisting SafeBase.reveal() must not implicitly authorize DangerousSubclass's unreviewed override")
            .isFalse();
    }

    @Test
    void a_compiled_expression_must_not_bypass_the_whitelist_when_reused_against_a_different_type() {
        DangerousGreeter.INVOKED.set(false);

        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "class " + SafeGreeter.class.getName());
        SecuredResolverTestInitializer.reinit(environment);

        String expression = "{#greeter.greet()}";

        // 1) Warm up the shared, process-wide expression cache and force SpEL's MIXED-mode
        //    compiler to kick in, using ONLY the whitelisted type.
        String lastWarmupResult = null;
        for (int i = 0; i < EVALUATIONS_TO_FORCE_COMPILATION; i++) {
            TemplateEngine warmup = TemplateEngine.templateEngine();
            warmup.getTemplateContext().setVariable("greeter", new SafeGreeter());
            lastWarmupResult = warmup.eval(expression, String.class).blockingGet();
        }
        assertThat(lastWarmupResult).isEqualTo("hello from the whitelisted implementation");

        // 2) Re-evaluate the SAME (now possibly compiled) cached expression, from a brand-new
        //    TemplateEngine / EvaluationContext, but bind "greeter" to the NON-whitelisted type.
        TemplateEngine attackerControlled = TemplateEngine.templateEngine();
        attackerControlled.getTemplateContext().setVariable("greeter", new DangerousGreeter());

        String result = null;
        try {
            result = attackerControlled.eval(expression, String.class).blockingGet();
        } catch (Exception expectedIfWhitelistHolds) {
            // Expected if the whitelist is correctly re-applied: evaluation is rejected.
        }

        assertThat(DangerousGreeter.INVOKED)
            .as(
                "the whitelist must not be bypassed via a compiled/cached expression reused against a " +
                "different runtime type (result was [%s])",
                result
            )
            .isFalse();
    }

    @Test
    void a_compiled_constructor_expression_must_not_bypass_the_whitelist_for_a_different_overload() {
        OverloadedCtor.DANGEROUS_CTOR_INVOKED.set(false);

        ConfigurableEnvironment environment = new MockEnvironment()
            .withProperty(EL_WHITELIST_MODE_KEY, "append")
            .withProperty(EL_WHITELIST_LIST_KEY + "[0]", "new " + OverloadedCtor.class.getName() + " java.lang.String");
        SecuredResolverTestInitializer.reinit(environment);

        String expression = "{new " + OverloadedCtor.class.getName() + "(#arg)}";

        // 1) Warm up and force compilation using ONLY the whitelisted (String) overload.
        for (int i = 0; i < EVALUATIONS_TO_FORCE_COMPILATION; i++) {
            TemplateEngine warmup = TemplateEngine.templateEngine();
            warmup.getTemplateContext().setVariable("arg", "safe-value");
            warmup.eval(expression, OverloadedCtor.class).blockingGet();
        }
        assertThat(OverloadedCtor.DANGEROUS_CTOR_INVOKED).isFalse();

        // 2) Re-evaluate the SAME (now possibly compiled) cached expression with an Integer
        //    argument, which only matches the NON-whitelisted overload.
        TemplateEngine attackerControlled = TemplateEngine.templateEngine();
        attackerControlled.getTemplateContext().setVariable("arg", 42);

        try {
            attackerControlled.eval(expression, OverloadedCtor.class).blockingGet();
        } catch (Exception expectedIfWhitelistHolds) {
            // Expected if the constructor whitelist correctly re-validates the resolved overload.
        }

        assertThat(OverloadedCtor.DANGEROUS_CTOR_INVOKED)
            .as(
                "the per-signature constructor whitelist must not be bypassed via a compiled/cached " +
                "expression reused with arguments matching a different, non-whitelisted overload"
            )
            .isFalse();
    }
}
