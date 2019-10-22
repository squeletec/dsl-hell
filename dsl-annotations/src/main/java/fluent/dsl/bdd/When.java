package fluent.dsl.bdd;

public @interface When {
    int value() default 0;
    // Aliases
    @interface Given {}
    @interface and {}
}
