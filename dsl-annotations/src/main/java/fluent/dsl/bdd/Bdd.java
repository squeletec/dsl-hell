package fluent.dsl.bdd;

public final class Bdd {

    private Bdd() {}

    public static <T> T Given(T t) {
        return t;
    }

    public static <T> T When(T t) {
        return t;
    }

    public static <T> T and(T t) {
        return t;
    }

    public static <T> T then(T t) {
        return t;
    }

    public static <T> T andThen(T t) {
        return t;
    }

}
