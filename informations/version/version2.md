# 2.2. Modern Java Features — Code & Explanation

## A. Sequenced Collections & Stream API — Java 16–21

### Sequenced Collections — Java 21

**Sequenced Collections** were introduced in Java 21.

They provide a unified API for collections that have a defined **encounter order**.

Before Java 21, different collection interfaces often provided different ways of accessing the first and last elements.

With `SequencedCollection`, we have a common API:

```java
SequencedCollection<String> list =
        new ArrayList<>(List.of("B", "C"));

list.addFirst("A");       // Inserts at the beginning
list.addLast("D");        // Inserts at the end

String first = list.getFirst();  // "A"
String last = list.getLast();    // "D"
```

The collection now contains:

```text
[A, B, C, D]
```

The main methods include:

```java
addFirst()
addLast()
getFirst()
getLast()
removeFirst()
removeLast()
reversed()
```

This provides a consistent API for ordered collections.

---

### Stream API

The **Stream API**, introduced in Java 8, allows us to process collections in a declarative way.

For example:

```java
List<String> processed = list.stream()
        .filter(s -> !s.startsWith("B"))
        .map(String::toLowerCase)
        .toList();
```

The processing pipeline can be understood as:

```text
Collection
    │
    ▼
 stream()
    │
    ▼
 filter()
    │
    ▼
 map()
    │
    ▼
 toList()
    │
    ▼
 Result
```

Given:

```text
[A, B, C, D]
```

the `filter()` removes `"B"`:

```text
[A, C, D]
```

Then `map()` converts the strings to lowercase:

```text
[a, c, d]
```

The final result is:

```java
[a, c, d]
```

### Important point

Since Java 16, `Stream.toList()` returns an **unmodifiable list**.

For example:

```java
List<String> result = list.stream()
        .map(String::toLowerCase)
        .toList();

result.add("x"); // UnsupportedOperationException
```

If you need a mutable list, you can use:

```java
List<String> result = list.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toCollection(ArrayList::new));
```

---

# B. Records, Compact Constructors & Sealed Classes

## Records

A **record** is a concise way to declare a class whose main purpose is to carry data.

For example:

```java
public record UserDto(
        String username,
        String email
) {
}
```

The compiler automatically provides:

* A canonical constructor
* Accessor methods
* `equals()`
* `hashCode()`
* `toString()`

For example:

```java
UserDto user = new UserDto(
        "john",
        "john@example.com"
);

String username = user.username();
String email = user.email();
```

Records are particularly useful for:

* DTOs
* API responses
* Value objects
* Immutable data carriers

---

## Compact Constructors

Records support a special type of constructor called a **compact constructor**.

It allows us to perform validation without explicitly declaring all constructor parameters and field assignments.

For example:

```java
public record UserDto(
        String username,
        String email
) {

    public UserDto {
        Objects.requireNonNull(
                username,
                "Username is required"
        );

        if (!email.contains("@")) {
            throw new IllegalArgumentException(
                    "Invalid email"
            );
        }
    }
}
```

The compiler implicitly performs the field assignments after the validation:

```text
username ──────────────► this.username
email ─────────────────► this.email
```

This is much cleaner than writing the entire constructor manually.

---

## Important Record Property

Records are **shallowly immutable**.

For example:

```java
public record User(
        String name,
        List<String> roles
) {}
```

The record reference itself is final, but the `List` can still be modified:

```java
User user = new User(
        "John",
        new ArrayList<>(List.of("ADMIN"))
);

user.roles().add("USER");
```

Therefore, a record does **not automatically make every object it contains immutable**.

If deep immutability is required, the referenced objects must also be immutable or defensively copied.

---

# Sealed Classes and Interfaces

A **sealed class or interface** allows you to explicitly control which classes are allowed to extend or implement it.

For example:

```java
public sealed interface PaymentMethod
        permits CreditCard, PayPal, Crypto {
}
```

This means that only these three types are allowed to directly implement `PaymentMethod`:

```text
PaymentMethod
     │
     ├── CreditCard
     ├── PayPal
     └── Crypto
```

---

## `final`, `sealed`, and `non-sealed`

The permitted subclasses must explicitly specify what happens to further inheritance.

### `final`

A `final` class cannot be extended:

```java
public final class CreditCard
        implements PaymentMethod {

    private String cardNumber;
}
```

Hierarchy:

```text
PaymentMethod
     │
     └── CreditCard
             X
       cannot be extended
```

---

### `non-sealed`

A `non-sealed` class removes the inheritance restriction for that branch:

```java
public non-sealed class Crypto
        implements PaymentMethod {

    private String walletAddress;
}
```

Other classes can therefore extend `Crypto`:

```text
PaymentMethod
     │
     └── Crypto
          │
          ├── Bitcoin
          ├── Ethereum
          └── OtherCrypto
```

---

### Why are sealed classes useful?

They are particularly useful when the domain has a **closed set of known alternatives**.

For example:

```java
public sealed interface PaymentMethod
        permits CreditCard, PayPal, Crypto {
}
```

The compiler knows that these are the permitted direct implementations.

This works particularly well with **pattern matching for switch**:

```java
String description = switch (paymentMethod) {
    case CreditCard c -> "Credit card";
    case PayPal p -> "PayPal";
    case Crypto c -> "Cryptocurrency";
};
```

The compiler can reason about the permitted hierarchy and can detect whether cases are missing.

---

# C. Virtual Threads — Project Loom — Java 21

**Virtual Threads** were finalized in Java 21 as part of **Project Loom**.

They are lightweight threads managed by the **JVM**, rather than being directly tied one-to-one to operating-system threads.

This makes it possible to create very large numbers of concurrent tasks.

---

# Platform Threads vs Virtual Threads

A traditional Java thread is a **platform thread**.

Conceptually:

```text
Java Thread
     │
     ▼
OS Thread
```

If an application creates thousands of platform threads, it can become expensive because each thread consumes operating-system resources and has a relatively large stack.

Virtual threads work differently:

```text
Virtual Thread ──┐
Virtual Thread ──┤
Virtual Thread ──┤
Virtual Thread ──┼──► JVM Scheduler
Virtual Thread ──┤
Virtual Thread ──┘
                       │
                       ▼
                 Carrier Threads
                       │
                       ▼
                    OS Threads
```

A large number of virtual threads can therefore be multiplexed over a much smaller number of platform threads, called **carrier threads**.

---

# Blocking I/O and Virtual Threads

This is one of the most important concepts to understand.

Imagine a virtual thread performing a database query:

```java
User user = database.findUser(id);
```

The operation may spend most of its time waiting for the database.

With a traditional platform thread:

```text
Platform Thread
     │
     ▼
Database query
     │
     │ waiting...
     │
     ▼
Platform thread remains occupied
```

With a virtual thread, when the operation is supported for virtual-thread scheduling, the JVM can **park the virtual thread** while it waits.

Conceptually:

```text
Virtual Thread
     │
     ▼
Database I/O
     │
     ▼
Park virtual thread
     │
     ▼
Carrier Thread becomes available
     │
     ▼
Runs another Virtual Thread
```

When the I/O operation completes, the virtual thread can be scheduled again.

This allows a large number of concurrent I/O-bound operations without requiring one platform thread per operation.

---

# A Simple Example

```java
try (var executor =
        Executors.newVirtualThreadPerTaskExecutor()) {

    IntStream.range(0, 100_000)
            .forEach(i -> {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    return i;
                });
            });
}
```

This creates **100,000 virtual-thread tasks**.

The important point is that creating 100,000 **virtual threads** is fundamentally different from creating 100,000 **platform threads**.

---

# Is `Thread.sleep()` Non-Blocking?

This is an important interview trap.

In the example:

```java
Thread.sleep(Duration.ofSeconds(1));
```

the virtual thread is still **blocked from making progress**, but it does not normally pin and occupy a carrier thread during the sleep.

Conceptually:

```text
Virtual Thread
     │
     ▼
sleep()
     │
     ▼
Parked
     │
     │ carrier thread is free
     ▼
Another Virtual Thread can run
```

Therefore, saying:

> "`Thread.sleep()` is non-blocking"

is not quite correct.

A better statement is:

> **"With virtual threads, `Thread.sleep()` parks the virtual thread without blocking the underlying carrier thread."**

This distinction is important.

---

# The Pinning Problem

One of the important Virtual Thread concepts is **pinning**.

A virtual thread is said to be **pinned** when it cannot be unmounted from its carrier thread while performing certain operations.

Historically, one important cause of pinning was entering a `synchronized` block or method.

For example:

```java
synchronized (lock) {
    performBlockingOperation();
}
```

If the virtual thread performs a blocking operation while holding the monitor, it may remain mounted on its carrier thread.

Conceptually:

```text
Virtual Thread
      │
      ▼
synchronized block
      │
      ▼
Blocking operation
      │
      ▼
Pinned to Carrier Thread
```

This can reduce scalability if it happens frequently.

---

# Modern Java and Pinning

There is an important version-specific nuance here.

In modern JDKs, the JVM has significantly improved virtual-thread behavior around `synchronized`.

Therefore, the statement:

> **"Every `synchronized` block pins a virtual thread."**

is no longer correct.

The real concern is:

> **"Certain operations can pin a virtual thread to its carrier thread, so blocking operations performed while a virtual thread is pinned can reduce scalability."**

Native or foreign calls can also be relevant.

You should therefore avoid presenting:

```java
synchronized → always pinned
```

as an absolute rule in a modern Java interview.

---

# `ReentrantLock` and Virtual Threads

For scenarios where a lock is needed around potentially blocking operations, `ReentrantLock` can sometimes be preferable.

For example:

```java
private final ReentrantLock lock = new ReentrantLock();

public void process() {

    lock.lock();

    try {
        performOperation();
    } finally {
        lock.unlock();
    }
}
```

The important point is not:

> "Never use `synchronized` with virtual threads."

That would be incorrect.

The better principle is:

> **"Avoid pinning virtual threads during long-running or blocking operations, and use appropriate synchronization mechanisms when necessary."**

---

# When Should You Use Virtual Threads?

Virtual threads are particularly useful for **I/O-bound applications**.

For example:

```text
HTTP Request
     │
     ▼
Database query
     │
     ▼
HTTP call
     │
     ▼
Another database query
     │
     ▼
Response
```

If most of the execution time is spent waiting for external systems, virtual threads can be highly effective.

Typical use cases include:

* REST APIs
* Microservices
* Database-heavy applications
* HTTP clients
* Messaging systems
* High-concurrency servers

---

# When Do Virtual Threads Not Help Much?

Virtual threads do not magically make CPU-bound code faster.

For example:

```java
while (true) {
    calculateSomething();
}
```

If the application is spending all its time doing CPU calculations, virtual threads do not create additional CPU capacity.

If you have:

```text
8 CPU cores
```

you still have approximately:

```text
8 CPU cores
```

Virtual threads mainly improve **concurrency**, not raw CPU performance.

A useful distinction is:

```text
I/O-bound workload
       │
       ▼
Virtual Threads → Very useful


CPU-bound workload
       │
       ▼
Virtual Threads → Limited benefit
```

---

# Virtual Threads vs Reactive Programming

One of the major motivations behind Project Loom is to make highly concurrent applications easier to write using a familiar **blocking programming model**.

Traditional thread-per-request architecture:

```text
Request
   │
   ▼
Platform Thread
   │
   ├── Database
   ├── HTTP
   └── File I/O
```

At very high concurrency, this can become expensive.

Reactive programming instead uses asynchronous pipelines:

```text
Request
   │
   ▼
Async Pipeline
   │
   ├── DB async
   ├── HTTP async
   └── Transform
```

Virtual threads allow us to write code that looks much more like the traditional blocking model:

```java
User user = database.findUser(id);
Order order = orderService.findOrder(user);
Payment payment = paymentService.process(order);
```

while still allowing a large number of concurrent I/O-bound operations.

This can significantly simplify application code compared with deeply asynchronous or reactive code.

---

# Key Interview Answer

If an interviewer asks:

> **"What are Virtual Threads?"**

A strong answer would be:

> **"Virtual Threads were finalized in Java 21 as part of Project Loom. They are lightweight threads managed by the JVM and multiplexed over a smaller number of platform threads, called carrier threads. Their main purpose is to make high-concurrency, especially I/O-bound applications, easier to build and more scalable.**
>
> **When a virtual thread performs a supported blocking operation, such as waiting for I/O or sleeping, it can be parked and its carrier thread can execute another virtual thread. This allows applications to handle very large numbers of concurrent tasks without requiring one operating-system thread per task.**
>
> **Virtual threads do not make CPU-bound operations faster. Their main benefit is efficient concurrency, particularly when tasks spend significant time waiting for external resources.**
>
> **A potential issue is pinning, where a virtual thread remains mounted on its carrier thread during certain operations. Modern JDKs have improved the interaction between virtual threads and synchronization, so it is not correct to say that every `synchronized` block automatically causes pinning."**

---

# The Big Picture

The three major modern Java concepts can be summarized like this:

```text
Sequenced Collections
        │
        ▼
Better and more consistent collection APIs


Records + Sealed Classes
        │
        ▼
Better data modeling and domain modeling


Virtual Threads
        │
        ▼
Massive concurrency with a simple programming model
```

And the key idea behind Virtual Threads is:

```text
                    HIGH CONCURRENCY
                           │
                           ▼
                  Virtual Threads
                           │
            ┌──────────────┴──────────────┐
            │                             │
        Lightweight                  JVM-managed
            │                             │
            └──────────────┬──────────────┘
                           ▼
                    Carrier Threads
                           │
                           ▼
                     OS Threads
```

The essential distinction to remember is:

> **Platform threads are expensive OS-backed execution resources. Virtual threads are lightweight JVM-managed tasks that can be multiplexed over a smaller number of platform threads.**

And the most important use case is:

> **Virtual Threads are primarily a scalability mechanism for high-concurrency, I/O-bound workloads, not a mechanism for making CPU-bound code execute faster.**
