# DSL HELL
Extreme Java DSL code generator.

No release done yet.

Annotation processing based code generator used to generate fluent API binding to  simple automation methods.

## Example

Automation interface with annotation metadata describing, how to invoke it using fluent API:

```java
@Dsl(className = "User", factoryMethod = "newUser")
public interface Automation {

    // These annotations can be externalzed
    @interface User {}
    @interface entersUsername {}
    @interface andPassword {}
    @interface atUrl {}
    @interface mustSee {}
    @interface entersOrder {}

    void userLogin(@entersUsername String username, @andPassword String password, @atUrl String url);

    void verifyLogonMessage(@mustSee @message String message);

}
```

Generated fluent interface sentence examples

```java
import static fluent.api.bdd.Bdd.When;
import static fluent.api.bdd.Bdd.then;

public class GeneratedUserDslTest {

    private final User John = newUser(mock(Automation.class));

    private final String validUserName = "John Doe";
    private final String validPassword = "$3cr3T";
    private final String invalidPassword = "password";
    private final String loginPage = "http://my.server.com/login";

    @Test
    public void successfulLoginScreenTest() {
        When (John). entersUsername (validUserName). andPassword (validPassword). atUrl (loginPage);
        then (John). mustSeeMessage ("Welcome My Name!");
    }

    @Test
    public void unsuccessfulLoginScreenTest() {
        When (John). entersUsername (validUserName). andPassword (invalidPassword). atUrl (loginPage);
        then (John). mustSeeMessage ("Invalid username or password!");
    }

    @Test
    public void testDirectDsl() {
        John.entersUsername(validUserName).andPassword(validPassword).atUrl(loginPage);
        John.mustSeeMessage("Welcome " + validUserName + "!");
    }

}
```

## Defining DSL

DSL is generated based on metadata defined using annotations.

1. Class or interface annotated with `@Dsl` will be processed, and used to build DSL for it.
2. Annotation annotated with `@Dsl` will be identified as description of the DSL sentences to be bound
   to the model's methods.
3. Annotations defined in class / interface annotated by `@Dsl`, or defined in package annotated with `@Dsl`
   will be identified as description of the DSL sentences.
4. Each annotation results in a method, unless it is annotated also with `@Constant`.
5. Annotation annotated `@Constant` generates extra singleton class which can is used as parameter
   for current method.
6. Dsl method will use all parameters until next `@Dsl` annotation. Those parameters can be either
   constants generated using annotation above, or real binding method's parameters.
7. If first parameter of binding method is annotated with `@Dsl` annotated annotation, this is used in DSL,
   and real method name is ignored in DSL. Otherwise method name is used also in DSL.
8. Method, that doesn't use any `@Dsl` annotated annotations results in it's copy in final DSL.
9. DSL annotations used on the method itself are used as suffix.
