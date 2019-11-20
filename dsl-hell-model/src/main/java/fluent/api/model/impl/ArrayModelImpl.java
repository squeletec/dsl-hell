package fluent.api.model.impl;

import fluent.api.model.ArrayModel;
import fluent.api.model.ModifiersModel;
import fluent.api.model.TypeModel;

import javax.lang.model.type.TypeKind;
import java.util.List;

public class ArrayModelImpl extends TypeModelImpl<ArrayModel> implements ArrayModel {

    public ArrayModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind) {
        super(modifiers, packageName, simpleName, fullName, kind);
    }

    public ArrayModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind, List<TypeModel<?>> typeParameters, ArrayModel rawType) {
        super(modifiers, packageName, simpleName, fullName, kind, typeParameters, rawType);
    }

    @Override
    protected ArrayModel t() {
        return this;
    }

    @Override
    protected ArrayModel construct(String collect, List<TypeModel<?>> typeParameters) {
        return new ArrayModelImpl(modifiers(), packageName(), simpleName(), fullName(), TypeKind.ARRAY, typeParameters, this);
    }

}
