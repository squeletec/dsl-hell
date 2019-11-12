package fluent.api.model.impl;

import fluent.api.model.InterfaceModel;
import fluent.api.model.ModifiersModel;
import fluent.api.model.TypeModel;

import javax.lang.model.type.TypeKind;
import java.util.List;

public class InterfaceModelImpl extends TypeModelImpl<InterfaceModel> implements InterfaceModel {

    public InterfaceModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind) {
        super(modifiers, packageName, simpleName, fullName, kind);
    }

    public InterfaceModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind, List<TypeModel> typeParameters, InterfaceModel rawType) {
        super(modifiers, packageName, simpleName, fullName, kind, typeParameters, rawType);
    }

    @Override
    protected InterfaceModel t() {
        return this;
    }

    @Override
    protected InterfaceModel construct(String collect, List<TypeModel> typeParameters) {
        return new InterfaceModelImpl(modifiers(), packageName(), simpleName() + collect, fullName() + collect, TypeKind.DECLARED, typeParameters, this);
    }

}
