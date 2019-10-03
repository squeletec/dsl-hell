package fluent.dsl.bdd;

import fluent.dsl.Dsl;
import fluent.dsl.Parametrized;

@Parametrized
@Dsl
public @interface then {
    // Aliases
    @interface andThen {}
}
