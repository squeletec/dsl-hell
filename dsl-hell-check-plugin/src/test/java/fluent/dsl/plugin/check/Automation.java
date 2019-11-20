package fluent.dsl.plugin.check;

import fluent.dsl.Dsl;
import fluent.dsl.bdd.When;
import fluent.validation.Check;

@Dsl
public interface Automation {

    @interface matches {}

    void verify(@When Order order, @matches @Dsl Check<? super Order> criteria);

}
