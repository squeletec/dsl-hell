package fluent.integration;

import org.testng.annotations.Test;

import static fluent.integration.Order.Side.BUY;
import static fluent.integration.OrderBuilder.orderWith;
import static fluent.integration.OrderCheck.Factory.side;
import static fluent.validation.ComparisonChecks.moreThan;
import static org.mockito.Mockito.mock;

public class AutomationWithBuildersTest {

    @Test
    public void test() {
        AutomationWithBuilders mock = mock(AutomationWithBuilders.class);
        AutomationWithBuildersDsl Tester = AutomationWithBuildersDsl.create(mock);

        Tester.injects(orderWith().orderId("").side(BUY).quantity(1).ric("A").price(0.00)).into("");
        Tester.mustSeeOrderWith().orderId("").and(side(BUY).ric("A").quantity(moreThan(0)));
    }

}
