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
package fluent.api.model;

import fluent.api.model.lazy.LazyMethodModel;
import fluent.api.model.lazy.LazyTypeModel;
import fluent.api.model.lazy.LazyVarModel;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class ModelFactory implements TypeVisitor<TypeModel, Element> {
    private final Elements elements;
    private final Types types;

    public ModelFactory(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    public MethodModel method(ExecutableElement element) {
        return new LazyMethodModel(
                Collections::emptyList,
                element.getModifiers().contains(STATIC),
                element.getModifiers().contains(PUBLIC),
                Collections::emptyList,
                visit(element.getReturnType()),
                element.getSimpleName().toString(),
                () -> element.getParameters().stream().map(this::parameter).collect(toList()),
                Collections::emptyList
        );
    }

    public VarModel parameter(VariableElement element) {
        return new LazyVarModel(
                Collections::emptyList,
                element.getModifiers().contains(STATIC),
                element.getModifiers().contains(PUBLIC),
                visit(element.asType()),
                element.getSimpleName().toString()
        );
    }

    public TypeModel type(Element element) {
        return visit(element.asType(), element);
    }

    @Override
    public TypeModel visit(TypeMirror t, Element typeElement) {
        return t.accept(this, typeElement);
    }

    @Override
    public TypeModel visit(TypeMirror t) {
        return visit(t, types.asElement(t));
    }

    private TypeModel visitDefault(TypeMirror t, boolean isTypeVariable) {
        return new LazyTypeModel(
                Collections::emptyList,
                true,
                true,
                Collections::emptyList,
                "",
                t.toString(),
                t.toString(),
                isTypeVariable,
                Collections::emptyList,
                Collections::emptyList
        );
    }

    @Override
    public TypeModel visitPrimitive(PrimitiveType t, Element element) {
        return visitDefault(t, false);
    }

    @Override
    public TypeModel visitNull(NullType t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitArray(ArrayType t, Element element) {
        TypeModel component = visit(t.getComponentType());
        return new LazyTypeModel(
                Collections::emptyList,
                true,
                true,
                Collections::emptyList,
                component.packageName(),
                component.simpleName() + "[]",
                t.toString(),
                true,
                null,
                () -> component,
                () -> methodsIn(element.getEnclosedElements()).stream().map(this::method).collect(toList()),
                () -> fieldsIn(element.getEnclosedElements()).stream().map(this::parameter).collect(toList())
        );
    }

    @Override
    public TypeModel visitDeclared(DeclaredType t, Element element) {
        Supplier<List<TypeModel>> s = () -> t.getTypeArguments().stream().map(this::visit).collect(toList());
        String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        boolean isStatic = element.getModifiers().contains(STATIC);
        boolean isPublic = element.getModifiers().contains(PUBLIC);
        Supplier<List<MethodModel>> m = () -> methodsIn(element.getEnclosedElements()).stream().map(this::method).collect(toList());
        Supplier<List<VarModel>> v = () -> fieldsIn(element.getEnclosedElements()).stream().map(this::parameter).collect(toList());

        Supplier<TypeModel> r = () -> new LazyTypeModel(
                Collections::emptyList,
                isStatic,
                isPublic,
                s,
                packageName,
                element.getSimpleName().toString(),
                element.toString(),
                false,
                null,
                null,
                m,
                v
        );

        return new LazyTypeModel(
                Collections::emptyList,
                isStatic,
                isPublic,
                s,
                packageName,
                packageName.isEmpty() ? t.toString() :t.toString().substring(packageName.length() + 1),
                t.toString(),
                false,
                r,
                null,
                m,
                v
        );
    }

    @Override
    public TypeModel visitError(ErrorType t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitTypeVariable(TypeVariable t, Element typeElement) {
        return visitDefault(t, true);
    }

    @Override
    public TypeModel visitWildcard(WildcardType t, Element typeElement) {
        if(t.getSuperBound() != null)
            return visit(t.getSuperBound());
        return visit(t.getExtendsBound());
    }

    @Override
    public TypeModel visitExecutable(ExecutableType t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitNoType(NoType t, Element typeElement) {
        return visitDefault(t, true);
    }

    @Override
    public TypeModel visitUnknown(TypeMirror t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitUnion(UnionType t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitIntersection(IntersectionType t, Element typeElement) {
        return null;
    }

}
