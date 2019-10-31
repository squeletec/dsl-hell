package fluent.api.model.impl;

import fluent.api.model.AnnotationModel;
import fluent.api.model.ElementModel;
import fluent.api.model.ModifiersModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementModelImpl implements ElementModel {

    private final List<AnnotationModel> annotations = new ArrayList<>();
    private final ModifiersModel modifiers;
    private final Map<String, Object> metadata = new HashMap<>();

    public ElementModelImpl(ModifiersModel modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public List<AnnotationModel> annotations() {
        return annotations;
    }

    @Override
    public ModifiersModel modifiers() {
        return modifiers;
    }

    @Override
    public Map<String, Object> metadata() {
        return metadata;
    }

}
