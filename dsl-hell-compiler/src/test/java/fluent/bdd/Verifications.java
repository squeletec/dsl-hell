package fluent.bdd;

import fluent.dsl.Dsl;
import fluent.dsl.def.in;
import fluent.dsl.def.mustSeeOrderWith;

@Dsl
public interface Verifications {
    void verify(@mustSeeOrderWith String id, @in String topic);
}
