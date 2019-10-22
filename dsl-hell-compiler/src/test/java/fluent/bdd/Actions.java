package fluent.bdd;

import fluent.dsl.Dsl;
import fluent.dsl.def.injects;
import fluent.dsl.def.into;

@Dsl
public interface Actions {
    void inject(@injects Object object, @into String topic);
}
