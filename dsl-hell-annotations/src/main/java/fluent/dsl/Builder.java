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

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Basic annotation, that has 2 main features:
 *
 *  1. It triggers code generation, when used on standard class or interface (and dsh-hell-compiler annotation
 *     processor is available to the compiler).
 *     In that case the annotated class / interface serves as the automation "binding", and fluent interface
 *     allowing use of all methods but with descriptive fluent java DSL will be generated.
 *
 *  2. When used on an annotation, package, or class / annotation, tha has nested annotations in it, it marks
 *     annotations within it's scope as keywords for the DSL. So they can be used in "method binding" signature
 *     to instruct the annotation processor, what the DSL sentences should look like.
 */
@Target({PARAMETER})
public @interface Builder {

    /**
     * Applies only to usage #1 (on class / interface)
     * It defines the package, in which the generated root DSL interface will be generated.
     *
     * @return Desired package name. If the value is empty string, then package name from the annotated
     * class / interface is used.
     */
    String packageName() default "";

    /**
     * Applies only to usage #1 (on class / interface)
     * It defines the name, of the root DSL generated interface.
     *
     * @return Desired name. If the value is empty, then simple name of the annotated class / interface with
     *         suffix "Dsl" is used.
     */
    String className() default "";

    /**
     * Applies only to usage #1 (on class / interface)
     * It defines name of the factory method, which creates instances of the root DSL.
     *
     * @return Name of the factory method. Default is "create".
     */
    String factoryMethod() default "create";

    /**
     * Applies to both usages (however not yet properly reflected when used in use case #2)
     * Flag indicating, if last parameter of type array in DSL method should be automatically convert
     * to vararg. Varargs in DSL are very convenient, and mostly DSL splits parameters into many methods,
     * each of them can turn into vararg.
     *
     * @return Flag indicating auto conversion to vararg. Default is to auto-convert.
     */
    boolean useVarargs() default true;

}
