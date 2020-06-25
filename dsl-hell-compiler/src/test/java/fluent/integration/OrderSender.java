package fluent.integration;

import java.util.function.BiFunction;
import java.util.function.Function;

public class OrderSender<F, G, T> {
    private final F fr;
    private final BiFunction<F, G, T> f;

    public OrderSender(F fr, BiFunction<F, G, T> f) {
        this.fr = fr;
        this.f = f;
    }

    public T into(G g) {
        return f.apply(fr, null);
    }
}
