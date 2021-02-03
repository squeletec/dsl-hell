# DSL HELL
![Released version](https://img.shields.io/maven-central/v/foundation.fluent.api/dsl-hell.svg)

Did you need higher/business level description, that BDD/Cucumber allows, but still benefit from Java general programming
language flexibility, and you didn't want to end up maintaining complex Java DSL (fluent interface) class / interface
hierarchy (boilerplate code), that would give it to you?

Then you can try this project, define DSL and bind it a bit "cucumbrish" way to your actions, and see if it gives you
what you need.

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
    <artifactId>dsl-hell-compiler</artifactId>
    <version>${dsl-hell.version}</version>
</dependency>
```
Additional ways how to configure annotation processor in maven can be found here:
[fluent-api-end-check](https://github.com/c0stra/fluent-api-end-check)

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

DSL keywords are prepared. Now we can define our binding of functional methods to our DSL.

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
Now recompile your code in order to generate the DSL code, e.g. by `mvn compile`.
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

## Releases notes

#### Version 1.3 (October 25th 2019)
- Re-implemented aliases for DSL methods
- Reorganized code and modules.

#### Version 1.2 (October 25th 2019)
- Implemented support for generics
- Extracted and rewritten model of the Java elements, so additional features can come to provide more complete DSL

#### Version 1.1 (October 21st 2019)
- Initial implementation for simple cases

## Full user guide
The purpose of this DSL generator project is to reduce manual maintaining of boilerplate code, that needs to be
normally written, when using fluent interface aka Java DSL.

So one should focus still only on the functionality, one wants to develop / automate using standard practises and as
simple API as needed, and with minimal effort get nice readable fluent interface / DSL on top of it.

With `dsl-hell` it's not needed to develop and maintain whole grammar with all branching and chaining in all
the interfaces anymore, that make up a fluent interface, but instead only describe how should our DSL sentences look, and how
are they bound to the simple API.

Example of full DSL:
```java
TBD
```

The same using `dsl-hell`:
```java
TBD
``` 

### 1. Possible constructs
#### 1.1 Keyword chain
Keywords are defined by annotations which themselves are annotated with annotation `@Dsl` (or have this annotation in
their hierarchy of outer classes or owning packages).

E.g.
```java
@Dsl @interface injects {}
@Dsl @interface message {}
```

Each keyword used in the binding will turn into a method in the generated DSL, which will be only accessible after
invoking all methods of previous keywords. So keyword (method) chaining is strictly positional:

```java
@Dsl
public interface Binding {
    void action(@injects @message String message);
}

// Possible:
dsl.injects().message("Ahoj");

// Not possible:
dsl.message().injects("Ahoj");
```

Method names created based on keyword annotations will be named exactly the same way, as the annotation, including
case. That on one hand breaks Java conventions for annotation naming (they normally start with a capital letter), but
on the other hand gives the flexibility to control case of methods in the chain (e.g. first method start with capital,
and others in the chain don't).

Still using the name of the annotation makes sure, tht method name will be proper java identifier. 

#### 1.2 Parameters

In fact the purpose of the DSL is mostly to collect parameters for use in the target (binding) method. The simple API
method for which a DSL sentence gets generated, contains keywords as well as the parameters. The logic is simple:

Every parameter in the API will become a parameter of the predecessor keyword:
```java
void action(@injects @message String message);
```
will generate a chain of unparametrized method `injects()` and parametrized method `message(String message)`.

In other words parameters are added to the last keyword, until another keyword appears. So we can follow single
parameter keywords DSL, if we introduce a keyword before every parameter:
```java
void action(@entersUsername @Strign username, @andPassword String password, @at String loginPage);
```
that will end up with `entersUsername(String username)`, `andPassword(String password)` and `at(String loginPage)`.

However, we don't need to have only 1 parameter methods in the DSL. Following code:
```java
void action(@schedule Runnable task, @after long value, TimeUnit unit);
```
ends up with `schedule(Runnable task)` and 2 parameter keyword method `after(long value, TimeUnit unit)`, which allows
still very nicely readable code:
```java
App.schedule(task).after(10, MINUTES);
```

#### 1.3 Common DSL prefix
Keywords do not need to be used only at method parameters, but they might be used with different meaning also elsewhere.

When used directly on the binding class / interface, they will cause generating "common prefix" of keyword method:

```java
@Dsl
@withRestAPI @call
interface RestAPIAutomation {
    void verifyMethodA(@serviceA String body, @andValidate Check<? super String> check);
    void verifyMethodB(@serviceB String body, @andValidate Check<? super String> check);
}
```

Now all our calls will start with a common prefix:
```java
Tester.withRestAPI().call().serviceA(body).andValidate(nonNull());
Tester.withRestAPI().call().serviceB(body).andValidate(nonNull());
```

In fact one can introduce the prefix keywords into every method, achieving the same effect. That doesn't make much
sense, but if one would like to group methods / sentences together but with multiple groups, then it's only possible
on the method level. Generator is able to properly merge common prefixes.

#### 1.4 Sentence suffix
Keywords defined at method level are a bit trickier, because they are a bit counter-intuitive.

Intuitively they appear before the method, but as it's the only place, not bound to any parameter, they are used as
**DSL sentence suffix**.

So following code:
```java
@exists action(@shouldSeeThat String message);
```

results in this:
```java
Tester.shouldSeeThat(message).exists();
```

It might be beneficial sometimes to use postfix notation in DSL, so that's why method's annotation is used this way.

### 2. What gets generated

To understand, what problems may arise when generating DSL using `dsl-hell` let's see what gets generated.

#### 2.1 Nested interfaces

Whole DSL for one binding class is generated within one java source file.
The DSL is organized so, that the top level (root) interface contains entry methods only. Interfaces following in the
chaining are always nested interfaces of their predecessor.

Example:
```java
public interface AutomationDsl {
    EntersUsernameString entersUsername(String username);

    interface EntersUsernameString {
        AndPasswordString andPassword(String password);

        interface AndPasswordString {
            void at(String loginPage);
        }
    }
}
```

#### 2.2 Factory method and anonymous implementation

Implementation is completely done inside the factory method, and it's achieved by anonymous classes. With this simple
principle there is no need to deal with fields and passing all parameters collected up to now, because they are
simply in the scope:

```java
static AutomationDsl create(Automation impl) {
    return new AutomationDsl() {
        public EntersUsernameString entersUsername(String username) {
            return new EntersUsernameString() {
                public AndPasswordString andPassword(String password) {
                    return new AndPasswordString() {
                        public void at(String loginPage) {
                            impl.action(username, password, loginPage);
                        }
                    }
                }
            };
        }
    };
}
```

Here we can see, that if some parameter of the action is named `impl`, then there will be naming conflict. To solve
that you can customize the parameter `parmaeterName` on the `@Dsl` annotation, see chapter _3. Customization of the DSL_.

#### 2.3 Delegate interface with default implementations

The generator automatically generates also delegate for the top level interface e.g: `AutomationDsl.Delegate`, which
contains additional delegate method by default named `delegate` (see chapter _3. Customization of the DSL_), and all
top level methods are routed to it.

This simplifies use cases, where we want to implement the interface for some reason instead of using the default
anonymous implementation. E.g. DSL should be implemented directly by some test class or so.

#### 2.4 Syntactic sugar constants

It could be useful for various reasons to use additional "artificial" parameters as syntactic sugar. It especially
helps when trying to design decomposed DSL, which should at some point be merged, or other tricks, but it might
also be just matter of preference for readability.

Let's say we choose BDD, and we want to have following syntax:
```java
When(User).entersUsername(validUsername).andPassword(validPassword);
```
But `User` should be just some constant / singleton used for readability, and is not really important for the underlying
binding API, so it's not part of it.

For that case, there is another annotation: `@Constant`. That marks an annotation to become such singleton parameter
of previous keyword.

That has even higher priority over `@Dsl` annotation. So if an annotation has both `@Dsl` (or in the hierarchy), and
`@Constant` it's used as constant.

Example of constant definition is simple:
```java
@Constant @interface User {}
```

Then it will be used in the binding:
```java
void logonAction(@When @User @entersUsername String username, @andPassword String password, @at String loginPage);
```

It will generate class `User` inside the root level DSL interface, which will have no methods and private constructor
and only one singleton instance named also `User` as member of the interface.

Then it can be used the way described above. It is treated as any other parameter, so it can even participate in
multiparametrized keywords.


### 3. Customizations of the DSL

Customization of the DSL is possible via attributes of the `@Dsl` annotation. Let's summarize them in
following table:

| Attribute        | Description                                                            | Default value / behavior |
|------------------|------------------------------------------------------------------------|--------------------------|
| `packageName`    | name of the package in which the DSL root interface will be generated. | If not specified, then package of the binding class / interface is used. |
| `className`      | simple class name of the generated root DSL class                      | If not specified, it will be derived from the binding class / interface by suffix `Dsl` |
| `factoryMethod`  | name of the factory method creating DSL instance                       | `create`                 |
| `parameterName`  | name of the parameter, via which the binding is passed to the factory. It helps avoiding naming conflict with other parameters. | `impl` |
| `delegateMethod` | name of the method, that creates a delegate                            | `delegate`               |
| `useVarargs`     | enables turning last parameter of every DSL method into varargs, if it was an array. Vararg methods are often very useful in DSL. | `true` |


### 4. Support for generics

Now a bit more advanced topics.

#### 4.1 Generic DSL model

Imagine simple case, when we are testing a generic service, e.g. queue. We'd like to have also generic automation for it.

This is still not very difficult to achieve. See example:

```java
@Dsl
public class QueueAutomation<T> {
    private final Queue<T> queue;
    
    void action(@add T element);

    void verification(@shouldPoll T element);
}
```

This generates properly generic DSL.

#### 4.2 Generic methods

With generic methods it's a bit more tricky.

Simple API method has all parameters available to the compiler at the same time, so it can do proper type inference:
```java
<T> void action(@queueAt String queueName, @inject T element);
```

Although this example is not very meaningful, it demonstrates a situation we may end up. First method `queueAt(String)`
is not able to infer `T`. If it was generic already, it would degrade `T` to `Object`. We want to have the DSL smarter.

The generator is able to identify, which type parameters are needed at which moment in the chaining, so it can make
the intermediate interfaces generic only when needed, and postpone the inference up to methods, where it's known.
It is done partially.

See example:
```java
<A, B> void action(@string String parameter, @A A a, @B B b, @A A c);
```

Now we can do perfect inference:
```java
// Compiles
dsl.string("A").A(5).B(4.5).C(3);

// Doesn't compile due to type mismatch for C
dsl.string("A").A(5).B(4.5).C("C");
```

### 5. DSL syntax tricks

#### 5.1 BDD Semantics

To add BDD semantics on top of any fluent interface one can simply use static methods from class `Bdd`:

Given this import:
```java
import static fluent.dsl.bdd.Bdd.*;
```

you can use:

````java
When(Tester).entersUsername(valiedUsername).andPassword(validPassword).atUrl(loginPage);
then(Tester).shouldSeeMessage(welcomeMessage);
````

#### 5.2 BDD generated DSL

The previous BDD style is very simple to achieve, but as the DSL root object is the same,
for all `<T> T Given(T)`, `<T> T When(T)` or `<T> T then(T)`, DSL for each separate type
of actions is the same. It may be desired, but maybe not.

If not, one can generate BDD style DSL using ready to use keywords `fluent.dsl.bdd.When` and
`fluent.ds.bdd.then`:

```java
void action(@When @User @entersUsername String username, @andPassword String password, @atUrl String url);
```

#### 5.3 Optically readable syntax

```java
When (User). entersUsername (validUsername). andPassword (validPassword). atUrl (loginPage);
```

Note extra spaces making the Java statement look a bit like a sentence with placeholders. 

### 6. Combination with other DSL constructs

Fluent DSL generated using this tool doesn't support to incorporate fluent builders or other extensions to it.
However, it is possible to combine it with other DSL constructs.

#### 6.1 Fluent builders

#### 6.2 Validators



## Useful links

https://github.com/c0stra/fluent-api-end-check - Compile time check of DSL sentence completeness.

https://github.com/c0stra/fluent-api-generator - Other useful code generators

https://github.com/c0stra/fluent-validation-support - Framework for various validation with transparency and type safety in mind.
