package fluent.staticEntry;

import org.testng.annotations.Test;

import static fluent.staticEntry.automation.StaticAutomationDsl.Static.When;

public class StaticAutomationTest {

    @Test
    public void staticAutomationTest() {
        When("A").then("B");
    }

}
