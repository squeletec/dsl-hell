package fluent.generic;

import fluent.dsl.Dsl;
import fluent.dsl.def.in;
import fluent.dsl.def.injects;
import fluent.dsl.def.mustSee;

import java.util.Queue;

@Dsl
public interface GenericMethodAutomation {

    <T> void performAddition(@in String a, @injects T element, @in Queue<T> queue);
    <T> void verifyAddition(@in String a, @mustSee T element, @in Queue<T> t);

}
