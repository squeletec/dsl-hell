package fluent.api.model;

import javax.lang.model.element.Modifier;
import java.util.Set;

public interface ModifiersModel {

    boolean isPublic();

    boolean isStatic();

    boolean isDefault();

    Set<Modifier> keywords();
}
