package fluent.bdd;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class BddTest implements BddAutomationDsl.Delegate {

    @Test
    public void test() {
        When("A").injects("").into().destination("");
        then(null).mustSeeOrderWith().orderId("A");
    }

    @Override
    public BddAutomationDsl delegate() {
        return BddAutomationDsl.create(mock(BddAutomation.class));
    }
}
