package fluent.api.model.impl;

import fluent.api.model.*;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.*;
import static javax.lang.model.element.Modifier.*;
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
    public InterfaceModel interfaceModel(String packageName, String className) {
        String fullName = packageName.isEmpty() ? className : packageName + "." + className;
        return new InterfaceModelImpl(modifiers(PUBLIC, STATIC), packageName, className, fullName, DECLARED);
    }

    @Override
    public ClassModel classModel(String packageName, String className) {
        String fullName = packageName.isEmpty() ? className : packageName + "." + className;
        return new ClassModelImpl(modifiers(PUBLIC, STATIC), packageName, className, fullName, DECLARED);
    }

    @Override
    public MethodModel method(Collection<Modifier> modifiers, String method, List<VarModel> parameters) {
        return modifiers.contains(STATIC)
                ? new StaticMethodModelImpl(modifiers(modifiers), method, parameters)
                : new MethodModelImpl(modifiers(modifiers), method, parameters, false);
    }

    @Override
    public MethodModel method(String method, List<VarModel> parameters) {
        return new MethodModelImpl(modifiers(PUBLIC), method, parameters, false);
    }

    @Override
    public StatementModel statementModel(VarModel target, MethodModel method) {
        return new StatementModel() {
            @Override public String toString() {
                if(isNull(method)) {
                    return "return " + target.name() + ";";
                }
                String parameters = "(" + method.parameters().stream().map(VarModel::name).collect(joining(", ")) + ");";
                if(method.isConstructor())
                    return "return new " + method.owner().fullName() + parameters;
                return (method.returnsValue() ? "return " : "") + (method.modifiers().isStatic() ? method.owner().fullName() : target.name()) + "." + method.name() + parameters;
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
        TypeModel owner = type(method.getEnclosingElement());
        List<VarModel> parameters = method.getParameters().stream().map(this::parameter).collect(toList());
        if(method.getKind() == ElementKind.CONSTRUCTOR) {
            return new ConstructorModelImpl(parameters).returnType(owner).owner(owner);
        }
        String name = method.getSimpleName().toString();
        TypeModel returnType = visit(method.getReturnType());
        if(method.getModifiers().contains(DEFAULT)) {
            return new DefaultMethodModelImpl(name, parameters).returnType(returnType).owner(owner);
        }
        ModifiersModel modifiers = modifiers(method.getModifiers());
        if(method.getModifiers().contains(STATIC)) {
            return new StaticMethodModelImpl(modifiers, name, parameters).returnType(returnType).owner(owner);
        }
        return new MethodModelImpl(modifiers, name, parameters, false).returnType(returnType).owner(owner);
    }

    @Override
    public MethodModel constructor(TypeModel type, VarModel... parameters) {
        return new ConstructorModelImpl(asList(parameters)).owner(type).returnType(type);
    }

    @Override
    public VarModel constant(String name) {
        return parameter(classModel("", name), name);
    }

    @Override
    public MethodModel defaultMethod(String name, List<VarModel> parameters) {
        return new DefaultMethodModelImpl(name, parameters);
    }

    @Override
    public MethodModel staticMethod(String name, List<VarModel> parameters) {
        return new StaticMethodModelImpl(modifiers(PUBLIC, STATIC), name, parameters);
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
        return new PrimitiveModelImpl(t.toString(), t.getKind());
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
        return new ArrayModelImpl(
                modifiers(PUBLIC, STATIC),
                component.packageName(),
                component.simpleName() + "[]",
                t.toString(),
                t.getKind()).componentType(component);
    }

    @Override
    public TypeModel<?> visitDeclared(DeclaredType t, Element element) {
        List<TypeModel> s = new LazyList<>(() -> t.getTypeArguments().stream().map(this::visit).collect(toList()));
        String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        List<MethodModel> m = new LazyList<>(() -> methodsIn(element.getEnclosedElements()).stream().map(this::method).collect(toList()));
        Map<String, VarModel> v = new LazyMap<>(() -> fieldsIn(element.getEnclosedElements()).stream().map(this::parameter).collect(toMap(VarModel::name, e -> e)));
        ModifiersModel modifiers = modifiers(element.getModifiers());

        String fullName = t.toString();
        String rawFullName = raw(fullName);
        String simpleName = fullName.substring(rawFullName.lastIndexOf('.') + 1);
        TypeKind kind = t.getKind();
        return (element.getKind() == ElementKind.INTERFACE ? new InterfaceModelImpl(
                modifiers,
                packageName,
                simpleName,
                fullName,
                kind,
                s,
                new InterfaceModelImpl(modifiers, packageName, raw(simpleName), rawFullName, kind)
        )
                : new ClassModelImpl(
                modifiers,
                packageName,
                simpleName,
                fullName,
                kind,
                s,
                new ClassModelImpl(modifiers, packageName, raw(simpleName), rawFullName, kind)
        )).methods(m).fields(v);
    }

    private String raw(String generic) {
        return generic.split("<")[0];
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
