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
