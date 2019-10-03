package fluent.dsl.bdd;

import fluent.dsl.Dsl;
import fluent.dsl.Parametrized;

@Parametrized
@Dsl
public @interface When {
    // Aliases
    @interface Given {}
    @interface and {}
}
