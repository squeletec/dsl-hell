package fluent.dsl.plugin.check;

import java.util.List;

public interface Order {

    int getIntValue();
    String getStringValue();
    Object[] getArrayValue();
    boolean isBooleanValue();
    List<String> getListValue();

}
