package fluent.generic;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.mockito.Mockito.mock;

public class GenericTest {

    @Test
    public void test() {
        GenericAutomation<String> mock = mock(GenericAutomation.class);
        GenericAutomationDsl<String> dsl = GenericAutomationDsl.create(mock);
        Queue<String> queue = new LinkedList<>();
        dsl.injects("A").in(queue);
        dsl.mustSee("A").in(queue);
    }

}
