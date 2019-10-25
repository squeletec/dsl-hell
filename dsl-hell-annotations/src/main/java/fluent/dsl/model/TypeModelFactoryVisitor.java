package fluent.dsl.model;

import fluent.api.model.MethodModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;
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

public class TypeModelFactoryVisitor implements TypeVisitor<TypeModel, Element> {
    private final Elements elements;
    private final Types types;

    public TypeModelFactoryVisitor(Elements elements, Types types) {
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

    @Override
    public TypeModel visit(TypeMirror t, Element typeElement) {
        return t.accept(this, typeElement);
    }

    @Override
    public TypeModel visit(TypeMirror t) {
        return visit(t, types.asElement(t));
    }

    private TypeModel visitDefault(TypeMirror t) {
        return new LazyTypeModel(
                Collections::emptyList,
                true,
                true,
                Collections::emptyList,
                "",
                t.toString(),
                t.toString(),
                Collections::emptyList,
                Collections::emptyList
        );
    }

    @Override
    public TypeModel visitPrimitive(PrimitiveType t, Element element) {
        return visitDefault(t);
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
        return visitDefault(t);
    }

    @Override
    public TypeModel visitWildcard(WildcardType t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitExecutable(ExecutableType t, Element typeElement) {
        return null;
    }

    @Override
    public TypeModel visitNoType(NoType t, Element typeElement) {
        return visitDefault(t);
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
