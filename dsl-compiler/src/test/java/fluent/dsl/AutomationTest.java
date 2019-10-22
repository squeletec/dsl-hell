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

package fluent.dsl;

import org.testng.annotations.Test;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AutomationTest {

    @Test
    public void test() {
        Automation mock = mock(Automation.class);
        AutomationDsl dsl = AutomationDsl.create(mock);
        dsl.with().injects("Order 1").into("dest");
        dsl.with().mustSee("Order 1").in("dest");
        int aaa = dsl.with().into("AAA");
        verify(mock).injectOrder("Order 1", "dest");
        verify(mock).verifyOrder("Order 1", "dest");
    }

    @Test
    public void testTime() {
        Automation mock = mock(Automation.class);
        AutomationDsl dsl = AutomationDsl.create(mock);
        dsl.with().mustSee(2, HOURS);
        dsl.with().emptyMethod();
        verify(mock).verifyTime(2, HOURS);
        verify(mock).emptyMethod();
    }
}
