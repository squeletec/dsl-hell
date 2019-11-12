package fluent.api.model.impl;

import fluent.api.model.ModifiersModel;
import fluent.api.model.StaticMethodModel;
import fluent.api.model.VarModel;

import java.util.List;

public class StaticMethodModelImpl extends MethodModelImpl implements StaticMethodModel {

    public StaticMethodModelImpl(ModifiersModel modifiers, String name, List<VarModel> parameters) {
        super(modifiers, name, parameters, false);
    }

}
