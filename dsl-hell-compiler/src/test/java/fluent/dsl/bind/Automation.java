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

package fluent.dsl.bind;

import fluent.dsl.Dsl;
import fluent.dsl.bdd.When.and;
import fluent.dsl.def.*;
import fluent.validation.Check;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Dsl
@withApplication
public interface Automation {

    void injectOrder(@injects String order, @into String destination);

    void verifyOrder(@mustSee String order, @in String destination);

    void verifyTime(@mustSee long value, TimeUnit unit);

    void verifyOrder(@mustSeeOrderWith @orderId String orderId, @and Check<? super String> orderCheck);

    void verifyArray(@mustSee String[] values, @in String target);

    @only void verifyNestedArray(@mustSee String[][] values);

    @only void verifyGeneric(@mustSee List<String> strings);

    @only void verifyNestedGeneric(@mustSee Supplier<List<String>> strings);

    @only void verifyVararg(@mustSee int... ints);

    void copy(String value);

    void emptyMethod();

    static int generate(@into String salt) {
        return 5;
    }

}
