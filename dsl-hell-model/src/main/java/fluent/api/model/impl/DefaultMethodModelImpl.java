package fluent.api.model.impl;

import fluent.api.model.*;

import java.util.List;
import static java.util.Collections.emptyList;

public class DefaultMethodModelImpl extends MethodModelImpl implements DefaultMethodModel {

    public DefaultMethodModelImpl(String name, List<VarModel> parameters) {
        super(new ModifiersModelImpl(emptyList()), name, parameters, false);
    }

}
