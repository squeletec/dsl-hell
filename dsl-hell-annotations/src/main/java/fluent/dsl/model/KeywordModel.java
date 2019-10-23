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

import java.util.List;

public class KeywordModel extends BaseModel {
    private final TypeModel type;
    private final List<String> aliases;
    private final List<ParameterModel> parameters;
    private final BindingModel binding;
    private final boolean useVarargs;
    public KeywordModel(List<AnnotationModel> annotations, TypeModel type, String name, List<String> aliases, List<ParameterModel> parameters, BindingModel binding, boolean useVarargs) {
        super(annotations, name);
        this.type = type;
        this.aliases = aliases;
        this.parameters = parameters;
        this.binding = binding;
        this.useVarargs = useVarargs;
    }
    public TypeModel type() {
        return hasBinding() ? binding.method().type() : type;
    }
    public List<String> aliases() {
        return aliases;
    }
    public List<ParameterModel> parameters() {
        return parameters;
    }
    public BindingModel binding() {
        return binding;
    }
    public boolean hasBinding() {
        return binding != null;
    }
}
