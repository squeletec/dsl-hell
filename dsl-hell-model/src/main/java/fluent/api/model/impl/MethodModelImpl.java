package fluent.api.model.impl;

import fluent.api.model.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;

public class MethodModelImpl extends GenericModelImpl<MethodModel> implements MethodModel {

    private static final TypeModel DEFAULT_TYPE = new TypeModelImpl(new ModifiersModelImpl(asList(PUBLIC, STATIC)), "", "void", "void", VOID);
    private TypeModel returnType = DEFAULT_TYPE;
    private final String name;
    private final List<VarModel> parameters;
    private final List<StatementModel> body = new ArrayList<>();
    private final boolean isConstructor;
    private TypeModel owner;

    public MethodModelImpl(ModifiersModel modifiers, String name, List<VarModel> parameters, boolean isConstructor) {
        super(modifiers);
        this.name = name;
        this.parameters = parameters;
        this.isConstructor = isConstructor;
    }

    @Override
    public TypeModel returnType() {
        return returnType;
    }

    @Override
    public MethodModel returnType(TypeModel returnType) {
        this.returnType = returnType;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<VarModel> parameters() {
        return parameters;
    }

    @Override
    public boolean returnsValue() {
        return !"void".equals(returnType.fullName());
    }

    @Override
    public boolean isConstructor() {
        return isConstructor;
    }

    @Override
    public List<StatementModel> body() {
        return body;
    }

    @Override
    public TypeModel owner() {
        return owner;
    }

    @Override
    public MethodModel owner(TypeModel owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public MethodModel typeParameters(List<TypeModel> typeParameters) {
        typeParameters().addAll(typeParameters);
        return this;
    }
}
