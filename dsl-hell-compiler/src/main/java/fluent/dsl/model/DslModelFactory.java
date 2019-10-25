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

import fluent.api.model.*;
import fluent.api.model.lazy.LazyAnnotationModel;
import fluent.api.model.lazy.LazyMethodModel;
import fluent.api.model.lazy.LazyTypeModel;
import fluent.api.model.lazy.LazyVarModel;

import javax.lang.model.element.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class DslModelFactory {

    private final ModelFactory visitor;

    public DslModelFactory(ModelFactory visitor) {
        this.visitor = visitor;
    }

    public VarModel parameter(List<AnnotationModel> emptyList, TypeModel type, String name) {
        return new LazyVarModel(() -> emptyList, false, true, type, name);
    }

    public static MethodModel method(List<AnnotationModel> annotations,
                                     boolean isStatic,
                                     boolean isPublic,
                                     List<TypeModel> typeParameters,
                                     TypeModel returnType,
                                     String name,
                                     List<VarModel> parameters,
                                     StatementModel... body) {
        return new LazyMethodModel(() -> annotations, isStatic, isPublic, () -> typeParameters, returnType, name, () -> parameters, () -> asList(body));
    }

    public AnnotationModel annotation(String type) {
        return new LazyAnnotationModel(Collections::emptyList, true, true, null);
    }

    public TypeModel type(List<AnnotationModel> annotations, String packageName, String simpleName) {
        return new LazyTypeModel(() -> annotations, true, true, Collections::emptyList, packageName, simpleName, packageName.isEmpty() ? simpleName : packageName + "." + simpleName, false, Collections::emptyList, Collections::emptyList);
    }

    public TypeModel type(Element element) {
        return visitor.visit(element.asType(), element);
    }

    public VarModel parameter(VariableElement element) {
        return visitor.parameter(element);
    }

    public MethodModel method(ExecutableElement element) {
        return visitor.method(element);
    }
    public static TypeModel type(List<AnnotationModel> annotations, List<TypeModel> typeParameters, String packageName, String simpleName, Supplier<List<MethodModel>> methodSupplier) {
        TypeModel raw = new LazyTypeModel(
                () -> annotations,
                true, true,
                Collections::emptyList,
                packageName,
                simpleName,
                packageName.isEmpty() ? simpleName : packageName + "." + simpleName,
                false,
                methodSupplier,
                Collections::emptyList
        );
        if(typeParameters.isEmpty())
            return raw;
        String collect = typeParameters.stream().map(TypeModel::fullName).collect(joining(", ", "<", ">"));
        return new LazyTypeModel(
                () -> annotations,
                true, true,
                () -> typeParameters,
                packageName,
                simpleName + collect,
                raw.fullName() + collect,
                false,
                () -> raw,
                null,
                methodSupplier,
                Collections::emptyList
        );
    }

    public DslModel dsl(MethodModel factory, String delegate) {
        return new DslModel(factory, delegate);
    }

    public StatementModel statementModel(VarModel target, MethodModel method) {
        return new StatementModel() {
            @Override public String toString() {
                return (method.returnsValue() ? "return " : "") + (method.isStatic() ? target.type().fullName() : target.name()) + "." + method.name() + "(" + method.parameters().stream().map(VarModel::name).collect(joining(", ")) + ");";
            }
        };
    }
}
