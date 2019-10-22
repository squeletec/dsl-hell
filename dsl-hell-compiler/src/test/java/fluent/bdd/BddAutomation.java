package fluent.bdd;

import fluent.dsl.Dsl;
import fluent.dsl.bdd.When;
import fluent.dsl.bdd.then;
import fluent.dsl.def.injects;
import fluent.dsl.def.into;
import fluent.dsl.def.mustSeeOrderWith;

@Dsl
public interface BddAutomation {

    void action(@When String user, @injects Object order, @into String destination);

    void verification(@then @SUT @mustSeeOrderWith String orderId);

}
