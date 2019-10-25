/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2019, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package fluent.dsl.bdd;

/**
 * Simple BDD static starting methods giving possibility to use any DSL in BDD way, e.g. if a DSL object
 * allows following chaining:
 *
 * tester.injectsMessage("Hello world!");
 * tester.mustSeeMessage("Hello world!")
 *
 * Then using these methods one can simply give BDD semantics to it:
 *
 * When (tester). injectsMessage("Ahoj!");
 * then (tester). mustSeeMessage("Ahoj!");
 */
public final class Bdd {

    private Bdd() {}

    /**
     * Given() should mean that there is some pre-requisite for the action, that may need to be st-up
     * by some code.
     *
     * @param dsl Root DSL object providing method, that can be used for chaining.
     * @param <T> Type of the DSL object.
     * @return The DSL object to continue chaining.
     */
    public static <T> T Given(T dsl) {
        return dsl;
    }

    /**
     * When() should mean the action to be automated and later verified using then().
     *
     * @param dsl Root DSL object providing method, that can be used for chaining.
     * @param <T> Type of the DSL object.
     * @return The DSL object to continue chaining.
     */
    public static <T> T When(T dsl) {
        return dsl;
    }

    /**
     * and() should mean additional action to be automated and later verified using then().
     * In fact it can be used also as additional validation step.
     *
     * @param dsl Root DSL object providing method, that can be used for chaining.
     * @param <T> Type of the DSL object.
     * @return The DSL object to continue chaining.
     */
    public static <T> T and(T dsl) {
        return dsl;
    }

    /**
     * then() should mean validation step to perform.
     * In fact it can be used also as additional action step.
     *
     * @param dsl Root DSL object providing method, that can be used for chaining.
     * @param <T> Type of the DSL object.
     * @return The DSL object to continue chaining.
     */
    public static <T> T then(T dsl) {
        return dsl;
    }

    /**
     * andThen() should mean additional validation step to perform.
     * In fact it can be used also as additional action step.
     *
     * @param dsl Root DSL object providing method, that can be used for chaining.
     * @param <T> Type of the DSL object.
     * @return The DSL object to continue chaining.
     */
    public static <T> T andThen(T dsl) {
        return dsl;
    }

}
