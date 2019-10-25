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

import fluent.api.model.AnnotationModel;
import fluent.api.model.MethodModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;

import java.util.List;
import java.util.function.Supplier;

import static fluent.api.model.lazy.Lazy.lazy;
import static java.util.Objects.isNull;

public class LazyTypeModel extends LazyGenericModel implements TypeModel {

    private final String simpleName;
    private final String packageName;
    private final String fullName;
    private final boolean isArray;
    private final Lazy<TypeModel> rawType;
    private final Lazy<TypeModel> componentType;
    private final Lazy<List<MethodModel>> methods;
    private final Lazy<List<VarModel>> fields;

    public LazyTypeModel(Supplier<List<AnnotationModel>> annotationSupplier,
                         boolean isStatic,
                         boolean isPublic,
                         Supplier<List<TypeModel>> typeParameters,
                         String packageName,
                         String simpleName,
                         String fullName,
                         boolean isArray,
                         Supplier<TypeModel> rawTypeSupplier,
                         Supplier<TypeModel> componentTypeSupplier,
                         Supplier<List<MethodModel>> methodsSupplier,
                         Supplier<List<VarModel>> fieldsSupplier) {
        super(annotationSupplier, isStatic, isPublic, typeParameters);
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.fullName = fullName;
        this.isArray = isArray;
        this.rawType = lazy(argOrThis(rawTypeSupplier));
        this.componentType = lazy(argOrThis(componentTypeSupplier));
        this.methods = lazy(methodsSupplier);
        this.fields = lazy(fieldsSupplier);
    }

    private Supplier<TypeModel> argOrThis(Supplier<TypeModel> s) {
        return isNull(s) ? () -> this : s;
    }

    public LazyTypeModel(Supplier<List<AnnotationModel>> annotationSupplier,
                         boolean isStatic,
                         boolean isPublic,
                         Supplier<List<TypeModel>> typeParameters,
                         String packageName,
                         String simpleName,
                         String fullName,
                         Supplier<List<MethodModel>> methodsSupplier,
                         Supplier<List<VarModel>> fieldsSupplier) {
        super(annotationSupplier, isStatic, isPublic, typeParameters);
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.isArray = false;
        this.fullName = fullName;
        this.rawType = lazy(this);
        this.componentType = lazy(this);
        this.methods = lazy(methodsSupplier);
        this.fields = lazy(fieldsSupplier);
    }


    @Override
    public String simpleName() {
        return simpleName;
    }

    @Override
    public String packageName() {
        return packageName;
    }

    @Override
    public String fullName() {
        return fullName;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }

    @Override
    public TypeModel rawType() {
        return rawType.get();
    }

    @Override
    public TypeModel componentType() {
        return componentType.get();
    }

    @Override
    public List<MethodModel> methods() {
        return methods.get();
    }

    @Override
    public List<VarModel> fields() {
        return fields.get();
    }

    @Override
    public String toString() {
        return fullName();
    }

}
