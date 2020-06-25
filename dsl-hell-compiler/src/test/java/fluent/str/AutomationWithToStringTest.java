package fluent.str;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AutomationWithToStringTest {
    @Test
    public void test() {
        Assert.assertEquals(AutomationWithToStringDsl.create(new AutomationWithToString("AA")).toString(), "AA");
    }
}
