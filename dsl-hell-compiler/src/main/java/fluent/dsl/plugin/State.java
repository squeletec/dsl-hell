package fluent.dsl.plugin;

import fluent.api.model.StatementModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;

import java.util.Set;

import static java.util.Collections.emptySet;

public interface State {

    State method(String name);

    default State keyword(String name) {
        return keyword(name, emptySet());
    }

    State keyword(String name, Set<String> aliases);

    State parameter(VarModel variable);

    State constant(VarModel element);

    void body(TypeModel returnType, StatementModel... method);

}
