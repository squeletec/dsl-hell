package fluent.api.model.impl;

import fluent.api.model.ModifiersModel;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static javax.lang.model.element.Modifier.*;

public class ModifiersModelImpl implements ModifiersModel {
    private Set<Modifier> modifiers = new LinkedHashSet<>();

    public ModifiersModelImpl(Collection<Modifier> modifiers) {
        this.modifiers.addAll(modifiers);
    }

    @Override
    public boolean isPublic() {
        return modifiers.contains(PUBLIC);
    }

    @Override
    public boolean isStatic() {
        return modifiers.contains(STATIC);
    }

    @Override
    public boolean isDefault() {
        return modifiers.contains(DEFAULT);
    }

}
