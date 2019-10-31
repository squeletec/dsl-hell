package fluent.integration;

import fluent.api.generator.validation.FluentCheck;
import fluent.dsl.Dsl;
import fluent.dsl.bdd.When;
import fluent.dsl.def.injects;
import fluent.dsl.def.into;
import fluent.dsl.def.mustSeeOrderWith;
import fluent.dsl.def.orderId;
import fluent.validation.Check;

@Dsl
public interface AutomationWithBuilders {

    void action(@injects Order order, @into String target);

    void verification(@mustSeeOrderWith @orderId String orderId, @When.and @FluentCheck Check<Order> check);

}
