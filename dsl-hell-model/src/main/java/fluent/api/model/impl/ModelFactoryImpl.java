package fluent.api.model.impl;

import fluent.api.model.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class ModelFactoryImpl implements ModelFactory, TypeVisitor<TypeModel, Element> {

    private final Elements elements;
    private final Types types;

    public ModelFactoryImpl(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    @Override
    public VarModel parameter(TypeModel model, String parameterName) {
        return new VarModelImpl(modifiers(), model, parameterName);
    }

    @Override
    public TypeModel type(String packageName, String className) {
        String fullName = packageName.isEmpty() ? className : packageName + "." + className;
        return new TypeModelImpl(modifiers(PUBLIC, STATIC), packageName, className, fullName, DECLARED);
    }

    @Override
    public MethodModel method(Collection<Modifier> modifiers, String method, List<VarModel> parameters) {
        return new MethodModelImpl(modifiers(modifiers), method, parameters);
    }

    @Override
    public MethodModel method(String method, List<VarModel> parameters) {
        return new MethodModelImpl(modifiers(PUBLIC), method, parameters);
    }

    @Override
    public StatementModel statementModel(VarModel target, MethodModel method) {
        return new StatementModel() {
            @Override public String toString() {
                return (method.returnsValue() ? "return " : "") + (method.modifiers().isStatic() ? target.type().fullName() : target.name()) + "." + method.name() + "(" + method.parameters().stream().map(VarModel::name).collect(joining(", ")) + ");";
            }
        };
    }

    @Override
    public TypeModel type(Element element) {
        return visit(element.asType(), element);
    }

    @Override
    public VarModel parameter(VariableElement model) {
        return new VarModelImpl(modifiers(model.getModifiers()), visit(model.asType()), model.getSimpleName().toString());
    }

    @Override
    public MethodModel method(ExecutableElement method) {
        return new MethodModelImpl(
                modifiers(method.getModifiers()),
                method.getSimpleName().toString(),
                method.getParameters().stream().map(this::parameter).collect(toList())
        ).returnType(visit(method.getReturnType()));
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
        return new TypeModelImpl(modifiers(PUBLIC, STATIC), "", t.toString(), t.toString(), t.getKind());
    }

    @Override
    public TypeModel visitPrimitive(PrimitiveType t, Element element) {
        return visitDefault(t);
    }

    @Override
    public TypeModel visitNull(NullType t, Element typeElement) {
        return null;
    }

    private ModifiersModel modifiers(Modifier... modifiers) {
        return modifiers(asList(modifiers));
    }

    private ModifiersModel modifiers(Collection<Modifier> modifiers) {
        return new ModifiersModelImpl(modifiers);
    }

    @Override
    public TypeModel visitArray(ArrayType t, Element element) {
        TypeModel component = visit(t.getComponentType());
        return new TypeModelImpl(
                modifiers(PUBLIC, STATIC),
                component.packageName(),
                component.simpleName() + "[]",
                t.toString(),
                t.getKind()
        ).componentType(component)
                .methods(new LazyList<>(() -> methodsIn(element.getEnclosedElements()).stream().map(this::method).collect(toList())))
                .fields(new LazyList<>(() -> fieldsIn(element.getEnclosedElements()).stream().map(this::parameter).collect(toList())));
    }

    @Override
    public TypeModel visitDeclared(DeclaredType t, Element element) {
        List<TypeModel> s = new LazyList<>(() -> t.getTypeArguments().stream().map(this::visit).collect(toList()));
        String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        List<MethodModel> m = new LazyList<>(() -> methodsIn(element.getEnclosedElements()).stream().map(this::method).collect(toList()));
        List<VarModel> v = new LazyList<>(() -> fieldsIn(element.getEnclosedElements()).stream().map(this::parameter).collect(toList()));

        return new TypeModelImpl(
                modifiers(element.getModifiers()),
                packageName,
                packageName.isEmpty() ? t.toString() :t.toString().substring(packageName.length() + 1),
                t.toString(),
                t.getKind(),
                s
        ).rawType(new TypeModelImpl(
                modifiers(element.getModifiers()),
                packageName,
                element.getSimpleName().toString(),
                element.toString(),
                t.getKind()
        ).methods(m).fields(v)
        ).methods(m).fields(v);
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
