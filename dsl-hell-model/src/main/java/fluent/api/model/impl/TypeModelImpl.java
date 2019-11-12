package fluent.api.model.impl;

import fluent.api.model.*;

import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public abstract class TypeModelImpl<T extends TypeModel<T>> extends GenericModelImpl<T> implements TypeModel<T> {
    private final String packageName;
    private final String simpleName;
    private final String fullName;
    private final TypeKind kind;
    private final T rawType;
    private TypeModel<?> componentType;
    private Map<String, VarModel> fields = new LinkedHashMap<>();
    private List<MethodModel> methods = new ArrayList<>();
    private List<InterfaceModel> interfaces = new ArrayList<>();
    private final List<TypeModel> nestedClasses = new ArrayList<>();

    public TypeModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind) {
        super(modifiers);
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.kind = kind;
        this.rawType = t();
    }

    public TypeModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind, List<TypeModel> typeParameters, T rawType) {
        super(modifiers);
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.kind = kind;
        this.rawType = rawType;
        this.typeParameters().addAll(typeParameters);
    }

    protected abstract T t();

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
        return kind == TypeKind.ARRAY;
    }

    @Override
    public boolean isTypeVariable() {
        return kind == TypeKind.TYPEVAR;
    }

    @Override
    public T rawType() {
        return rawType;
    }

    @Override
    public TypeModel<?> componentType() {
        return componentType;
    }

    @Override
    public T componentType(TypeModel<?> componentType) {
        this.componentType = componentType;
        return t();
    }

    @Override
    public List<MethodModel> methods() {
        return methods;
    }

    @Override
    public T methods(List<MethodModel> methods) {
        this.methods = methods;
        return t();
    }

    @Override
    public Map<String, VarModel> fields() {
        return fields;
    }

    @Override
    public T fields(Map<String, VarModel> fields) {
        this.fields = fields;
        return t();
    }

    @Override
    public List<InterfaceModel> interfaces() {
        return interfaces;
    }

    @Override
    public List<TypeModel> types() {
        return nestedClasses;
    }

    @Override
    public T typeParameters(List<TypeModel> typeParameters) {
        if(typeParameters.isEmpty())
            return t();
        String collect = typeParameters.stream().map(TypeModel::fullName).collect(joining(", ", "<", ">"));
        return construct(collect, typeParameters);
    }

    protected abstract T construct(String collect, List<TypeModel> typeParameters);

    @Override
    public String toString() {
        return fullName();
    }

}
