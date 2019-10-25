/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2018, Ondrej Fischer
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
package fluent.api.model.lazy;

import fluent.api.model.*;

import java.util.List;
import java.util.function.Supplier;

public class LazyMethodModel extends LazyGenericModel implements MethodModel {

    private final TypeModel returnType;
    private final String name;
    private final Lazy<List<VarModel>> parameters;
    private final Lazy<List<StatementModel>> body;

    public LazyMethodModel(Supplier<List<AnnotationModel>> annotationSupplier,
                           boolean isStatic,
                           boolean isPublic,
                           Supplier<List<TypeModel>> typeParameters,
                           TypeModel returnType,
                           String name,
                           Supplier<List<VarModel>> parametersSupplier,
                           Supplier<List<StatementModel>> bodySupplier) {
        super(annotationSupplier, isStatic, isPublic, typeParameters);
        this.returnType = returnType;
        this.name = name;
        this.parameters = Lazy.lazy(parametersSupplier);
        this.body = Lazy.lazy(bodySupplier);
    }

    @Override
    public TypeModel returnType() {
        return returnType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<VarModel> parameters() {
        return parameters.get();
    }

    @Override
    public boolean returnsValue() {
        return !"void".equals(returnType.fullName());
    }

    @Override
    public List<StatementModel> body() {
        return body.get();
    }

}
