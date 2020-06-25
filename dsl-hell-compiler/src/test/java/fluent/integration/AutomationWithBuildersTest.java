package fluent.integration;

import org.testng.annotations.Test;

import java.time.LocalDate;

import static fluent.integration.Order.Side.BUY;
import static fluent.integration.OrderCheck.Factory.side;
import static fluent.integration.OrderWith.orderId;
import static fluent.validation.ComparisonChecks.moreThan;
import static org.mockito.Mockito.mock;

public class AutomationWithBuildersTest {

    @Test
    public void test() {
        AutomationWithBuilders mock = mock(AutomationWithBuilders.class);
        AutomationWithBuildersDsl Tester = AutomationWithBuildersDsl.create(mock);

        Tester.injectsOrderWith(
                orderId("").side(BUY).quantity(1).ric("A").price(0.00)
        ).into("");
        Tester.mustSeeOrderWith().orderId("").and(side(BUY).ric("A").quantity(moreThan(0)));

        Tester.injects(MessageWith.payload("").date(LocalDate.now()).build());

        Tester.injectsOrderWith(null).into("");
    }

}
