package fluent.api.model.impl;

import fluent.api.model.*;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericModelImpl<T> extends ElementModelImpl implements GenericModel<T> {
    private final List<TypeModel> typeParameters = new ArrayList<>();

    public GenericModelImpl(ModifiersModel modifiers) {
        super(modifiers);
    }

    @Override
    public List<TypeModel> typeParameters() {
        return typeParameters;
    }

}
