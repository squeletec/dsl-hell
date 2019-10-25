# DSL HELL
![Released version](https://img.shields.io/maven-central/v/foundation.fluent.api/dsl-hell.svg)

Did you need higher/business level description, that BDD/Cucumber allows, but still benefit from Java general programming
language flexibility, and you didn't want to end up maintaining complex Java DSL (fluent interface) class / interface
hierarchy, that would give it to you?

Then you can try this project, and see if it gives you what you need.

## Start in 3 steps
There are no more than following 3 simple steps to get from 0 to full higher level Java DSL.

### 1. Add dependencies
This project uses annotation processing based code generator. So you need following dependencies:

Standard compile dependency with annotations to define DSL "keywords":
```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>dsl-hell-annotations</artifactId>
    <version>${dsl-hell.version}</version>
</dependency>
```

Then you need the dependency on the annotation processor for your compiler. It can be added couple of different ways.
Simples (but not exactly correct) is adding it as standard dependency:
```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>dsl-hell-annotations</artifactId>
    <version>${dsl-hell.version}</version>
</dependency>
```
Now you are ready to build your DSL.

### 2. Define your DSL keywords
We'll be building Java based DSL, which is achieved by method chaining. What we want to be able to write, is e.g.

```java
Tester.entersUsername(validUsername).andPassword(validPassword).at(loginPage);
Tester.shouldSee(welcomeMessage);
```

`Tester` is our root object of the DSL. We are going to generate it's class (interface).
We need to prepare keywords for this DSL. By keywords I mean all the methods, and could also be artificial objects.
This is done by annotations, annotated with `@Dsl` annotation.

This annotation can be used anywhere on following elements:
1. The keyword annotation itself
2. Package containing the annotation or any parent package.
3. Class / interface / annotation, which may contain nested annotation.

For simplicity and minimum amount of work let's use last option:

```java
@Dsl
public interface Automation {

    @interface entersUsername {}
    @interface andPassword {}
    @interface at {}
    @interface shouldSee {}

}
```

DSL keywords are prepared. Now we caan define our binding of functional methods to our DSL.

### 3. Bind your methods via DSL

Now we'll only focus on the automation logic, using standard Java interface method with all arguments needed for it,
and only by annotations decorate it so, that full DSL can be generated.

For simplicity let's have all together in the Automation interface:

```java
@Dsl
public interface Automation {

    @interface entersUsername {}
    @interface andPassword {}
    @interface shouldSee {}

    void loginAction(@entersUsername String username, @andPassword String password, @at Sting url);

    void validateMessage(@shouldSee String message);

}
```
Now recompile zour code in order to generate the code, e.g. by `mvn compile`.
We are done. Now there is new interface `AutomationDsl` generated with factory method accepting instance of this our
interface, and providing the DSL we described above.

See usage in simple test (assume using mockito and some testing framework):
```java
public class AutomationTest {
    private final String validUsername = "John Doe";
    private final String validPassword = "$3cr3T";
    private final String welcomeMessage = "Welcome John";
    private final String loginPage = "http://my.server.com/login";

    @Test
    public void test() {
        Automation mock = mock(Automation.class);
        AutomationDsl Tester = AutomationDsl.create(mock);
    
        // Here we use our DSL
        Tester.entersUsername(validUsername).andPassword(validPassword).at(loginPage);
        Tester.shouldSee(welcomeMessage);
    
        // Using mockito we can verify, that DSL propagated properly to our automation calls
        verify(mock).loginAction(username, password, url);
        verify(mock).validateMessage(validMessage);
    }

}
```

Binding methods are only invoked at the very end method of each sentence.
To make sure, that nobody will miss it, the generated code benefits from following compiler extension: 
[fluent-api-end-check](https://github.com/c0stra/fluent-api-end-check)


## Full user guide
TBD

### 1. Customizations of the DSL

### 2. Support for generics
