package fluent.generic;

import fluent.dsl.Dsl;
import fluent.dsl.def.in;
import fluent.dsl.def.injects;
import fluent.dsl.def.mustSee;

import java.util.Queue;

@Dsl
public interface GenericAutomation<T> {

    void performAddition(@injects T element, @in Queue<T> queue);
    void verifyAddition(@mustSee T element, @in Queue<T> t);

}
