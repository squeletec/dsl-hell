package fluent.api.model.impl;

import fluent.api.model.ConstructorModel;
import fluent.api.model.VarModel;

import java.util.List;

import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PUBLIC;

public class ConstructorModelImpl extends MethodModelImpl implements ConstructorModel {

    public ConstructorModelImpl(List<VarModel> parameters) {
        super(new ModifiersModelImpl(asList(PUBLIC)), "<init>", parameters, true);
    }

}
