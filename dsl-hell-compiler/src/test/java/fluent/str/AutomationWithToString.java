package fluent.str;

import fluent.dsl.Dsl;

@Dsl
public class AutomationWithToString {

    private final String string;

    public AutomationWithToString(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

}
