# 2. Overall Evolution of Java — Java 8 to Java 25

Java has evolved significantly since Java 8, with major improvements in:

* Language expressiveness
* Functional programming
* Type inference
* Concurrency
* JVM performance
* Modularity
* Pattern matching
* Data-oriented programming
* Developer productivity

A particularly important distinction is between **LTS (Long-Term Support)** releases and regular feature releases.

---

# 2.1. Major Versions and LTS Releases

| Version        | Status           | Major Innovations and Key Features                                                                                                               |
| -------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Java 8**     | LTS              | Lambdas, Stream API, Optional, `java.time` (JSR-310), default and static interface methods, Metaspace                                            |
| **Java 9**     | Feature Release  | JPMS / Project Jigsaw, module system, JShell, collection factory methods, improved HTTP client incubation                                        |
| **Java 10**    | Feature Release  | Local variable type inference with `var`, Application Class-Data Sharing                                                                         |
| **Java 11**    | LTS              | Standardized HTTP Client, `var` in lambda parameters, new String/File APIs, Flight Recorder                                                      |
| **Java 12–16** | Feature Releases | Switch expressions, text blocks, pattern matching for `instanceof`, records, sealed classes                                                      |
| **Java 17**    | LTS              | Sealed Classes, Pattern Matching for `instanceof`, enhanced switch preview, strong encapsulation                                                 |
| **Java 18–20** | Feature Releases | UTF-8 by default, Simple Web Server, Virtual Threads preview, Pattern Matching for `switch`, Record Patterns                                     |
| **Java 21**    | LTS              | Virtual Threads, Sequenced Collections, Pattern Matching for `switch`, Record Patterns, String Templates preview, Structured Concurrency preview |
| **Java 22–24** | Feature Releases | Unnamed Variables and Patterns, Foreign Function & Memory API, Stream Gatherers, Flexible Constructor Bodies, evolving concurrency features      |
| **Java 25**    | LTS              | Latest LTS release, including finalized and improved language, concurrency, and JVM features from recent release cycles                          |

---

# 2.2. Java 8 — The Major Turning Point

**Java 8** was one of the most important releases in Java's history.

It introduced several features that fundamentally changed how Java code was written.

---

## Lambda Expressions

Before Java 8, passing behavior as a parameter generally required an anonymous class:

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```

Java 8 introduced lambda expressions:

```java
Runnable task = () -> System.out.println("Hello");
```

This made Java much more suitable for **functional-style programming**.

---

## Stream API

The **Stream API** introduced a declarative way of processing collections.

Instead of explicitly managing loops:

```java
List<String> result = new ArrayList<>();

for (String name : names) {
    if (name.length() > 5) {
        result.add(name.toUpperCase());
    }
}
```

we can write:

```java
List<String> result = names.stream()
        .filter(name -> name.length() > 5)
        .map(String::toUpperCase)
        .toList();
```

The Stream API is particularly useful for:

* Filtering
* Mapping
* Aggregation
* Grouping
* Sorting
* Parallel data processing

---

## Optional

Java 8 introduced `Optional<T>` as a way to explicitly represent the possible absence of a value.

Instead of:

```java
User user = findUser(id);

if (user != null) {
    // ...
}
```

we can have:

```java
Optional<User> user = findUser(id);
```

Then:

```java
user.ifPresent(u -> System.out.println(u.getName()));
```

`Optional` is mainly intended to make the absence of a return value explicit and reduce accidental `NullPointerException`s.

---

## java.time

Java 8 introduced the modern date and time API through `java.time`.

Examples include:

```java
LocalDate
LocalDateTime
Instant
Duration
Period
ZonedDateTime
```

For example:

```java
LocalDate today = LocalDate.now();

LocalDate birthday = LocalDate.of(1995, 5, 10);

Period age = Period.between(birthday, today);
```

This API is generally preferred over the older `java.util.Date` and `Calendar` APIs.

---

## Default Methods in Interfaces

Java 8 allowed interfaces to contain **default methods**:

```java
public interface Vehicle {

    default void start() {
        System.out.println("Starting...");
    }
}
```

This allowed interfaces to evolve without necessarily breaking every existing implementation.

Interfaces can also contain static methods:

```java
public interface MathUtils {

    static int add(int a, int b) {
        return a + b;
    }
}
```

---

# 2.3. Java 9 — Modularity and Project Jigsaw

Java 9 introduced the **Java Platform Module System (JPMS)**, also known as **Project Jigsaw**.

The goal was to make large Java applications and the JDK itself more modular.

Instead of simply having packages:

```text
com.example.user
com.example.order
com.example.payment
```

you can define explicit modules and their dependencies.

For example:

```java
module com.example.application {

    requires com.example.database;

    exports com.example.api;
}
```

A module can explicitly declare:

* What it requires
* What it exports
* What it hides

This improves encapsulation and dependency management.

---

# 2.4. Java 10 — `var`

Java 10 introduced **local variable type inference** with `var`.

Instead of:

```java
Map<String, List<User>> usersByDepartment =
        new HashMap<>();
```

you can write:

```java
var usersByDepartment = new HashMap<String, List<User>>();
```

The compiler still knows the exact static type.

`var` does **not** make Java dynamically typed.

For example:

```java
var name = "John";
```

The compiler knows that:

```text
name → String
```

The type is inferred at compile time.

---

# 2.5. Java 11 — HTTP Client

Java 11 standardized the modern HTTP Client API:

```java
HttpClient
HttpRequest
HttpResponse
```

For example:

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://example.com"))
        .GET()
        .build();

HttpResponse<String> response =
        client.send(request, HttpResponse.BodyHandlers.ofString());
```

The API supports modern HTTP functionality, including:

* HTTP/1.1
* HTTP/2
* Asynchronous requests

This reduced the need for third-party HTTP clients for many use cases.

---

# 2.6. Java 12–17 — More Expressive Java

This period introduced several features that significantly improved Java's syntax and ability to model data.

The most important ones include:

* Switch expressions
* Text blocks
* Pattern matching for `instanceof`
* Records
* Sealed classes

---

# 2.7. Switch Expressions

Traditional `switch` statements often required verbose code:

```java
int result;

switch (operation) {
    case ADD:
        result = 1;
        break;
    case SUBTRACT:
        result = -1;
        break;
    default:
        result = 0;
}
```

Modern Java allows a `switch` expression:

```java
int result = switch (operation) {
    case ADD -> 1;
    case SUBTRACT -> -1;
    default -> 0;
};
```

The `switch` can now directly produce a value.

---

# 2.8. Pattern Matching for `instanceof`

Before pattern matching:

```java
if (obj instanceof String) {
    String text = (String) obj;
    System.out.println(text.length());
}
```

Modern Java allows:

```java
if (obj instanceof String text) {
    System.out.println(text.length());
}
```

The cast and variable declaration are combined.

This makes type-checking code more concise and readable.

---

# 2.9. Records

**Records** were introduced to simplify the creation of immutable data carriers.

Instead of writing:

```java
public final class User {

    private final String name;
    private final int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }

    // equals()
    // hashCode()
    // toString()
}
```

you can write:

```java
public record User(String name, int age) {
}
```

The compiler generates the canonical constructor, accessors, `equals()`, `hashCode()`, and `toString()`.

Records are particularly useful for:

* DTOs
* API responses
* Configuration objects
* Value-oriented data

---

# 2.10. Sealed Classes

**Sealed classes** allow you to explicitly control which classes can extend a class or interface.

For example:

```java
public sealed interface Shape
        permits Circle, Rectangle {
}
```

Then:

```java
public final class Circle implements Shape {
}
```

and:

```java
public final class Rectangle implements Shape {
}
```

The compiler knows the complete set of permitted implementations.

This is particularly useful for modeling **closed hierarchies**.

It also works very well with pattern matching:

```java
double area = switch (shape) {
    case Circle c -> calculateCircleArea(c);
    case Rectangle r -> calculateRectangleArea(r);
};
```

---

# 2.11. Text Blocks

Java introduced **text blocks** to make multi-line strings much easier to write.

Instead of:

```java
String json = "{\n" +
              "  \"name\": \"John\",\n" +
              "  \"age\": 30\n" +
              "}";
```

you can write:

```java
String json = """
        {
          "name": "John",
          "age": 30
        }
        """;
```

This is particularly useful for:

* JSON
* SQL
* HTML
* XML
* Multi-line templates

---

# 2.12. Java 17 — LTS and Stronger Encapsulation

**Java 17** became an important LTS release after Java 11.

It finalized several features introduced during previous releases, including:

* Sealed classes
* Pattern matching for `instanceof`

Java 17 also strengthened the encapsulation of internal JDK APIs.

This encouraged developers to rely on supported public APIs rather than internal implementation details.

---

# 2.13. Java 21 — Another Major Turning Point

**Java 21** is one of the most important modern Java releases.

It introduced several major features, particularly around concurrency.

The most important ones include:

* Virtual Threads
* Pattern Matching for `switch`
* Record Patterns
* Sequenced Collections
* Generational ZGC
* Several improvements to structured concurrency and scoped values

---

# 2.14. Virtual Threads — Project Loom

Virtual Threads are one of the biggest changes to Java concurrency.

Traditional Java threads are relatively expensive because they are closely associated with operating-system threads.

Creating thousands or millions of traditional threads can therefore be problematic.

Virtual threads are lightweight threads managed by the JVM.

For example:

```java
Thread.startVirtualThread(() -> {
    processRequest();
});
```

Or:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

    executor.submit(() -> processRequest());
}
```

The major advantage is that applications can support very large numbers of concurrent tasks, especially when those tasks spend significant time waiting for:

* Network I/O
* Database queries
* HTTP calls
* File I/O

Conceptually:

```text
Traditional Threads

Java Thread  ───────────────► OS Thread
Java Thread  ───────────────► OS Thread
Java Thread  ───────────────► OS Thread
Java Thread  ───────────────► OS Thread


Virtual Threads

Virtual Thread ──┐
Virtual Thread ──┤
Virtual Thread ──┤
Virtual Thread ──┤──► JVM ──► OS Threads
Virtual Thread ──┤
Virtual Thread ──┘
```

The JVM can schedule many virtual threads over a much smaller number of operating-system threads.

---

# 2.15. Pattern Matching for `switch`

Java 21 finalized pattern matching for `switch`.

Instead of:

```java
if (obj instanceof String s) {
    ...
} else if (obj instanceof Integer i) {
    ...
}
```

you can write:

```java
String result = switch (obj) {
    case String s -> "String: " + s;
    case Integer i -> "Integer: " + i;
    default -> "Unknown";
};
```

This becomes particularly powerful when combined with **sealed classes**.

---

# 2.16. Record Patterns

Record patterns allow you to deconstruct records directly.

Suppose we have:

```java
record Point(int x, int y) {}
```

Instead of:

```java
if (obj instanceof Point point) {
    int x = point.x();
    int y = point.y();
}
```

you can write:

```java
if (obj instanceof Point(int x, int y)) {
    System.out.println(x + ", " + y);
}
```

This is called **pattern deconstruction**.

It becomes particularly powerful when combined with nested records and pattern matching.

---

# 2.17. Sequenced Collections

Java 21 introduced **Sequenced Collections**.

The goal is to provide a common API for collections where element order matters.

For example:

```java
SequencedCollection<String> names = new ArrayList<>();
```

The API provides operations such as:

```java
getFirst()
getLast()
addFirst()
addLast()
reversed()
```

This creates a more consistent API for ordered collections.

---

# 2.18. Java 22–25

Java continued its six-month release cycle after Java 21.

Several features evolved through preview and incubation stages before becoming finalized.

Important developments include:

* Unnamed variables and patterns
* Flexible constructor bodies
* Foreign Function & Memory API
* Stream Gatherers
* Evolving concurrency APIs
* Improvements to pattern matching
* Improvements to the JVM and garbage collectors

---

# 2.19. Unnamed Variables and Patterns

Modern Java allows `_` to indicate that a value is intentionally unused.

For example:

```java
try {
    Integer.parseInt(input);
} catch (NumberFormatException _) {
    System.out.println("Invalid number");
}
```

The `_` communicates:

> "I know about this value, but I deliberately don't need it."

This improves readability and avoids meaningless variable names.

---

# 2.20. Flexible Constructor Bodies

Modern Java has evolved constructor rules to allow certain statements to appear **before the explicit `super(...)` or `this(...)` constructor invocation**, provided they are safe and do not access the object under construction in an invalid way.

Conceptually:

```java
public class Child extends Parent {

    public Child(String value) {

        // Validation or preparation
        if (value == null) {
            throw new IllegalArgumentException();
        }

        super(value);
    }
}
```

This gives developers more flexibility when validating or preparing constructor arguments.

---

# 2.21. Foreign Function & Memory API

The **Foreign Function & Memory API (FFM API)** provides a modern way for Java applications to interact with:

* Native code
* Native memory
* C libraries
* Memory segments outside the Java heap

It is intended to provide a safer and more modern alternative to some use cases traditionally handled through JNI.

Conceptually:

```text
Java Application
       │
       ▼
 Foreign Function & Memory API
       │
       ├──────────────► Native Memory
       │
       └──────────────► Native Functions
```

This is particularly relevant for high-performance systems and interoperability with native libraries.

---

# 2.22. The Overall Evolution

A useful way to remember Java's evolution is:

```text
Java 8
  │
  ├── Lambdas
  ├── Streams
  ├── Optional
  └── java.time
       │
       ▼
Java 9–11
  │
  ├── Modules
  ├── var
  └── Modern HTTP Client
       │
       ▼
Java 12–17
  │
  ├── Switch Expressions
  ├── Records
  ├── Sealed Classes
  ├── Pattern Matching
  └── Text Blocks
       │
       ▼
Java 21
  │
  ├── Virtual Threads
  ├── Pattern Matching for switch
  ├── Record Patterns
  ├── Sequenced Collections
  └── Generational ZGC
       │
       ▼
Java 22–25
  │
  ├── Unnamed Variables & Patterns
  ├── Flexible Constructor Bodies
  ├── FFM API
  ├── Stream Gatherers
  └── Continued JVM & concurrency improvements
```

---

# 2.23. The Most Important Versions to Know for an Interview

If you are preparing for a **modern Java developer interview**, you do not need to memorize every feature of every release.

The most important milestones are:

| Version     | What You Should Remember                                             |
| ----------- | -------------------------------------------------------------------- |
| **Java 8**  | Lambdas, Streams, Optional, `java.time`                              |
| **Java 9**  | Modules / JPMS                                                       |
| **Java 10** | `var`                                                                |
| **Java 11** | Standard HTTP Client, LTS                                            |
| **Java 17** | Records, Sealed Classes, Pattern Matching, LTS                       |
| **Java 21** | Virtual Threads, Pattern Matching for `switch`, Record Patterns, LTS |
| **Java 25** | Latest LTS, continued evolution of language, JVM, GC and concurrency |

A particularly useful progression to remember is:

```text
Java 8  → Functional programming
Java 11 → Modern APIs + Modules
Java 17 → Data modeling + Pattern Matching
Java 21 → Modern concurrency
Java 25 → Latest LTS / Modern Java platform
```

---

# Key Interview Answer

If an interviewer asks:

> **"What are the major changes between Java 8 and modern Java?"**

A strong answer would be:

> **"Java has evolved significantly since Java 8. Java 8 introduced functional programming features such as lambdas, the Stream API, Optional, and the modern date and time API. Java 9 introduced the module system, while Java 10 introduced local variable type inference with `var`, and Java 11 brought the standardized HTTP Client.**
>
> **Java 17 introduced important features for modeling data and domain hierarchies, such as records and sealed classes, as well as pattern matching for `instanceof`. Java 21 was another major milestone, especially because it introduced virtual threads, which make it much easier to handle large amounts of concurrent I/O-bound work. It also finalized pattern matching for `switch` and record patterns.**
>
> **Java 22 through 25 continued this evolution with features such as unnamed variables and patterns, flexible constructor bodies, the Foreign Function & Memory API, and further improvements to concurrency, the JVM, and garbage collection. Java 25 is the latest LTS release."**

---

# The Big Picture

The evolution of Java can essentially be summarized as a move from:

```text
Java 8
"How can I write Java more concisely?"
        │
        ▼
Java 17
"How can I model data and domain concepts better?"
        │
        ▼
Java 21
"How can I handle massive concurrency more efficiently?"
        │
        ▼
Java 25
"How can Java continue improving the language,
JVM, concurrency and performance?"
```

The key idea is that **modern Java is not a completely different language from Java 8**.

Rather, Java has progressively added features that make the language:

* More expressive
* More concise
* Safer
* Better suited to functional programming
* Better suited to data modeling
* Better suited to massive concurrency
* More performant at the JVM level
