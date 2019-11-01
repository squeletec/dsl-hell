package fluent.integration;

import fluent.api.generator.validation.FluentCheck;
import fluent.dsl.Dsl;
import fluent.dsl.bdd.When;
import fluent.dsl.def.*;
import fluent.validation.Check;

@Dsl
public interface AutomationWithBuilders {

    void action(@injectsOrderWith @Dsl Order order, @into String target);

    void verification(@mustSeeOrderWith @orderId String orderId, @When.and @FluentCheck Check<Order> check);

    <T> void action(@injects @Dsl Message<T> message);

}
