package fluent.api.model.impl;

import fluent.api.model.ModifiersModel;
import fluent.api.model.PrimitiveModel;
import fluent.api.model.TypeModel;

import javax.lang.model.type.TypeKind;
import java.util.List;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class PrimitiveModelImpl extends TypeModelImpl<PrimitiveModel> implements PrimitiveModel {
    public PrimitiveModelImpl(String simpleName, TypeKind kind) {
        super(new ModifiersModelImpl(asList(PUBLIC, STATIC)), "", simpleName, simpleName, kind);
    }

    @Override
    protected PrimitiveModel t() {
        return this;
    }

    @Override
    protected PrimitiveModel construct(String collect, List<TypeModel> typeParameters) {
        return this;
    }

}
