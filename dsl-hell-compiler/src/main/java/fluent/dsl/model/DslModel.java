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
package fluent.dsl.model;

import fluent.api.model.MethodModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;

import java.util.*;

import static java.util.Collections.emptyList;

public class DslModel {
    private final TypeModel type;
    private final MethodModel factory;
    private final MethodModel delegate;
    private final Map<String, VarModel> constants = new LinkedHashMap<>();

    public DslModel(MethodModel factory, String delegate) {
        this.factory = factory;
        this.type = factory.returnType();
        this.delegate = DslModelFactory.method(emptyList(), true, true, emptyList(), type, delegate, emptyList());
    }

    public TypeModel type() {
        return type;
    }

    public MethodModel factory() {
        return factory;
    }

    public MethodModel delegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return type.fullName();
    }

    public Collection<VarModel> constants() {
        return constants.values();
    }

    public VarModel addConstant(VarModel constant) {
        return constants.computeIfAbsent(constant.name(), key -> constant);
    }

}
