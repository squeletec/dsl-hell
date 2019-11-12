package fluent.bdd;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class BddTest implements BddAutomationDsl.Delegate {

    @Test
    public void test() {
        Given("A").injects("").into("");
        When("A").injects("").into("");
        //then(SUT).mustSeeOrderWith("A");
    }

    @Override
    public BddAutomationDsl delegate() {
        return BddAutomationDsl.create(mock(BddAutomation.class));
    }
}
