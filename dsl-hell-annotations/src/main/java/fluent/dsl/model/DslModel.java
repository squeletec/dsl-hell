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

import java.util.*;

import static fluent.dsl.model.DslUtils.capitalize;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class DslModel extends TypeModel {
    private final String packageName;
    private final KeywordModel factory;
    private final KeywordModel delegate;
    private final Map<String, ParameterModel> constants = new LinkedHashMap<>();

    public DslModel(String packageName, List<AnnotationModel> annotations, String name, String factory, ParameterModel source, String delegate) {
        super(annotations, name);
        this.packageName = packageName;
        this.factory = new KeywordModel(emptyList(), this, factory, emptyList(), singletonList(source), null, false);
        this.delegate = new KeywordModel(emptyList(), this, delegate, emptyList(), emptyList(), null, false);
    }

    public String packageName() {
        return packageName;
    }

    public KeywordModel factory() {
        return factory;
    }

    public KeywordModel delegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return packageName + "." + name();
    }

    public ParameterModel addConstant(String name) {
        return constants.computeIfAbsent(name, key -> new ParameterModel(emptyList(), new TypeModel(emptyList(), capitalize(name)), name));
    }

    public Collection<ParameterModel> constants() {
        return constants.values();
    }

}
