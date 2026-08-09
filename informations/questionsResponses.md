# Java / Spring Boot / Angular — Senior Developer Interview Guide

## Lombard Odier — Private Banking / Switzerland

> **Target profile:** Senior Full-Stack Developer — Java / Spring Boot / Angular
> **Target environment:** Private banking, Geneva / Switzerland
> **Java target:** Java 21–25 concepts
> **Spring target:** Spring Boot 3.x / 4.x, Spring Framework 6.x / 7.x
> **Angular target:** Angular 20–22 concepts
> **Level:** Senior

---

# 1. What Lombard Odier Is Likely Looking For

Lombard Odier describes itself as a global wealth and asset manager serving private and institutional clients, with a strong emphasis on trust, transparency, excellence, innovation, integrity and teamwork.

For a senior developer, the interview is therefore unlikely to be purely about syntax.

The interviewer may want to determine whether you can:

* write robust Java;
* understand the JVM;
* reason about concurrency;
* design maintainable Spring applications;
* understand transactions and persistence;
* design APIs;
* secure financial applications;
* reason about distributed systems;
* write testable code;
* understand Angular architecture;
* diagnose production problems;
* make appropriate architectural trade-offs;
* communicate clearly with technical and business stakeholders;
* work safely with sensitive financial data.

A particularly important distinction:

> **A senior developer does not merely know how a technology works. They know when to use it, when not to use it, and what trade-off it introduces.**

---

# 2. The Interview Mental Model

For almost every technical question, use this structure:

1. **Definition**
2. **Why it exists**
3. **How it works**
4. **Example**
5. **Trade-offs**
6. **Production considerations**

For example:

### Question

> Why would you use a virtual thread?

Weak answer:

> "Because virtual threads are faster."

Strong answer:

> "Virtual threads are lightweight JVM-managed threads designed primarily for workloads that spend significant time waiting, especially on blocking I/O. Instead of dedicating an OS thread to a blocked request, the JVM can suspend the virtual thread and reuse the underlying carrier thread for another virtual thread. This allows a large number of concurrent I/O-bound tasks without requiring reactive programming everywhere. They are not a replacement for CPU parallelism."

That final sentence is particularly important.

---

# 3. JAVA CORE

---

# 3.1 What is the difference between JDK, JRE and JVM?

### Answer

* **JVM** executes Java bytecode.
* **JRE** historically contained the JVM plus the libraries required to run Java applications.
* **JDK** contains the tools required to develop Java applications, including the compiler and runtime.

Modern Java distributions are generally distributed as a JDK rather than a separate JRE.

---

# 3.2 Why is Java platform-independent?

Java source code is compiled into bytecode:

```text
Java source
    ↓
javac
    ↓
.class bytecode
    ↓
JVM
    ↓
Operating system
```

The bytecode is platform-independent.

The JVM implementation is platform-specific.

Therefore:

```text
Same bytecode
     ↓
Linux JVM
Windows JVM
macOS JVM
```

---

# 3.3 Is Java pass-by-reference or pass-by-value?

Java is **always pass-by-value**.

For objects, the value being copied is the reference.

```java
void change(User user) {
    user.setName("Alice");
}
```

The reference is copied, so both references point to the same object.

But:

```java
void replace(User user) {
    user = new User("Alice");
}
```

does not replace the caller's reference.

Conceptually:

```text
caller
  |
  | reference A
  ↓
 User object

method parameter
  |
  | copied reference A
  ↓
 same User object
```

---

# 3.4 What is the difference between `==` and `.equals()`?

`==` compares:

* primitive values for primitives;
* object references for objects.

`.equals()` compares logical equality when the class overrides it.

```java
String a = new String("hello");
String b = new String("hello");

a == b        // false
a.equals(b)   // true
```

---

# 3.5 What is the contract between `equals()` and `hashCode()`?

If:

```java
a.equals(b)
```

is `true`, then:

```java
a.hashCode() == b.hashCode()
```

must also be true.

The reverse is not required.

Two different objects may have the same hash code.

This matters enormously for:

```java
HashMap
HashSet
ConcurrentHashMap
```

---

# 3.6 Why should objects used as HashMap keys generally be immutable?

Consider:

```java
Map<User, String> map = new HashMap<>();

User user = new User("Alice");

map.put(user, "admin");
```

If the fields used by `hashCode()` change afterward, the object may effectively be stored in one bucket while its new hash points to another.

```text
Before mutation:

hash(user) → bucket 5

After mutation:

hash(user) → bucket 12
```

The entry is still physically in bucket 5.

This can make lookup fail.

Therefore, keys should generally be immutable.

---

# 3.7 What is immutability?

An immutable object cannot change its observable state after construction.

Example:

```java
public final class User {

    private final String name;

    public User(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
```

Advantages:

* thread safety;
* easier reasoning;
* safe sharing;
* reliable map keys;
* fewer side effects.

Java examples include:

```java
String
Integer
LocalDate
LocalDateTime
BigDecimal
```

and records are particularly useful for immutable data carriers.

---

# 3.8 What is a record?

```java
public record User(
    Long id,
    String name
) {}
```

A record automatically provides:

* final components;
* constructor;
* accessors;
* `equals()`;
* `hashCode()`;
* `toString()`.

Records are particularly appropriate for:

* DTOs;
* API responses;
* immutable values;
* commands;
* events.

They are not necessarily appropriate for every JPA entity.

---

# 3.9 What is the difference between a class and a record?

A record is primarily a concise declaration of a data carrier.

A normal class gives you much more freedom over:

* mutable state;
* inheritance;
* encapsulation;
* lifecycle;
* identity.

A record is a semantic statement:

> "This type primarily represents data."

---

# 3.10 What are sealed classes?

```java
public sealed interface Payment
    permits CardPayment, BankTransfer {
}
```

Only permitted types can implement the interface.

This is useful when the domain has a closed hierarchy.

For example:

```text
Payment
 ├── CardPayment
 ├── BankTransfer
 └── CashPayment
```

The compiler can therefore reason more precisely about the possible subtypes.

---

# 3.11 What are pattern matching and switch expressions?

Modern Java allows more expressive type handling.

```java
if (payment instanceof BankTransfer transfer) {
    transfer.execute();
}
```

And:

```java
return switch (payment) {
    case CardPayment card -> processCard(card);
    case BankTransfer transfer -> processTransfer(transfer);
};
```

This is especially powerful with sealed hierarchies.

---

# 3.12 What is `Optional`?

`Optional<T>` represents the presence or absence of a value.

```java
Optional<User> findUser(Long id)
```

Instead of:

```java
User findUser(Long id)
```

potentially returning `null`.

Good:

```java
return repository.findById(id)
    .map(UserDto::from)
    .orElseThrow(UserNotFoundException::new);
```

Avoid:

```java
Optional<User> optional = ...
if (optional.isPresent()) {
    ...
}
```

when a more functional expression is clearer.

---

# 3.13 Should Optional be used everywhere?

No.

Generally:

### Good

```java
Optional<User> findById(Long id)
```

### Usually bad

```java
class User {
    private Optional<String> name;
}
```

### Usually bad

```java
void process(Optional<User> user)
```

`Optional` is primarily useful for expressing an optional return value.

---

# 3.14 What is the difference between `List`, `Set` and `Map`?

## List

Ordered collection, duplicates allowed.

```java
List<String> names = new ArrayList<>();
```

Example:

```text
Alice
Bob
Alice
```

---

## Set

Collection where duplicates are not allowed.

```java
Set<String> names = new HashSet<>();
```

---

## Map

Associates keys with values.

```java
Map<Long, User> users = new HashMap<>();
```

Conceptually:

```text
key → value
1   → Alice
2   → Bob
```

---

# 3.15 ArrayList vs LinkedList

### ArrayList

Internally array-backed.

```text
[ A ][ B ][ C ][ D ][ E ]
```

Excellent for:

* random access;
* iteration;
* most general-purpose list usage.

### LinkedList

Node-based.

```text
A ↔ B ↔ C ↔ D
```

Can be useful for specific deque operations, but it is often overused.

For normal application code:

> Prefer `ArrayList` unless you have a concrete reason to use `LinkedList`.

---

# 3.16 HashSet vs TreeSet

### HashSet

Hash-based.

Typical complexity:

```text
add       O(1)
contains  O(1)
remove    O(1)
```

No sorted order guarantee.

### TreeSet

Tree-based.

Typical complexity:

```text
add       O(log n)
contains  O(log n)
remove    O(log n)
```

Maintains sorted order.

Use:

```java
HashSet
```

when you need efficient membership.

Use:

```java
TreeSet
```

when ordering/sorted navigation matters.

---

# 3.17 HashMap vs TreeMap

### HashMap

```java
Map<Long, User> users = new HashMap<>();
```

Optimized for lookup by key.

Average:

```text
get  → O(1)
put  → O(1)
```

### TreeMap

```java
Map<Long, User> users = new TreeMap<>();
```

Maintains keys in sorted order.

Typical:

```text
get → O(log n)
put → O(log n)
```

Use `TreeMap` when you need operations such as:

```java
floorKey()
ceilingKey()
firstKey()
lastKey()
subMap()
```

---

# 3.18 What is LinkedHashMap?

`LinkedHashMap` combines hashing with predictable encounter order.

Useful when you want:

```text
fast lookup
+
insertion order
```

It can also be configured for access order, which makes it useful for LRU-style caches.

---

# 3.19 What is ConcurrentHashMap?

A thread-safe map designed for concurrent access.

```java
ConcurrentHashMap<String, User> users;
```

It is generally preferable to synchronizing an entire `HashMap` when you need high-concurrency access.

Important:

> Thread-safe does not automatically mean that a sequence of multiple operations is atomic.

For example:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

is not an atomic compound operation.

Prefer:

```java
map.putIfAbsent(key, value);
```

or:

```java
map.computeIfAbsent(key, k -> createUser(k));
```

---

# 3.20 What are immutable and unmodifiable collections?

These concepts are often confused.

## Unmodifiable view

```java
List<String> source =
    new ArrayList<>(List.of("A", "B"));

List<String> view =
    Collections.unmodifiableList(source);
```

You cannot modify through `view`.

But:

```java
source.add("C");
```

causes the view to reflect the change.

So:

```text
source
  ↓
unmodifiable view
```

The underlying collection is still mutable.

---

## Immutable collection

```java
List<String> list =
    List.of("A", "B");
```

The collection itself cannot be modified.

Java's collection APIs also provide:

```java
List.copyOf(...)
Set.copyOf(...)
Map.copyOf(...)
```

These are useful when you want an immutable snapshot.

Java's collection hierarchy now also includes `SequencedCollection`, `SequencedSet`, and `SequencedMap`, providing common APIs for first/last elements and reversed views.

---

# 3.21 What is the Collections Framework hierarchy?

Conceptually:

```text
Iterable
   |
Collection
   |
   +── List
   |    +── ArrayList
   |    +── LinkedList
   |    +── CopyOnWriteArrayList
   |
   +── Set
   |    +── HashSet
   |    +── LinkedHashSet
   |    +── TreeSet
   |    +── EnumSet
   |
   +── Queue
        +── PriorityQueue
        +── Deque
             +── ArrayDeque
             +── LinkedList

Map
 |
 +── HashMap
 +── LinkedHashMap
 +── TreeMap
 +── EnumMap
 +── ConcurrentHashMap
 +── ConcurrentSkipListMap
```

Java 25 additionally has the sequenced collection abstractions.

---

# 4. JAVA STREAMS

---

# 4.1 What is a Stream?

A Stream represents a pipeline for processing data.

```java
List<String> result =
    users.stream()
        .filter(User::isActive)
        .map(User::name)
        .toList();
```

A Stream is not a collection.

It does not normally store the data itself.

---

# 4.2 Intermediate vs terminal operations

Intermediate:

```java
filter()
map()
sorted()
distinct()
limit()
```

Terminal:

```java
collect()
toList()
count()
reduce()
forEach()
```

Example:

```java
users.stream()
    .filter(User::isActive)  // intermediate
    .map(User::name)         // intermediate
    .toList();               // terminal
```

Streams are generally lazy until a terminal operation is invoked.

---

# 4.3 `map()` vs `flatMap()`

`map()` transforms one element into one result.

```java
List<String> names =
    users.stream()
        .map(User::name)
        .toList();
```

`flatMap()` is useful when each element produces multiple elements.

```java
List<Order> orders =
    users.stream()
        .flatMap(user -> user.orders().stream())
        .toList();
```

Conceptually:

```text
User → List<Order>

flatMap

User
 ↓
List<Order>
 ↓
Order
Order
Order
```

---

# 4.4 What is `reduce()`?

Used to combine many values into one.

```java
int total =
    amounts.stream()
        .reduce(0, Integer::sum);
```

Conceptually:

```text
1 + 2 + 3 + 4
        ↓
       10
```

---

# 4.5 Should streams always be used?

No.

This:

```java
users.stream()
    .filter(...)
    .map(...)
    .toList();
```

can be excellent.

But if the stream becomes:

```java
users.stream()
    .filter(...)
    .map(...)
    .flatMap(...)
    .peek(...)
    .sorted(...)
    .collect(...)
```

and is difficult to understand, a normal loop may be better.

> Readability is more important than using streams.

---

# 5. JAVA CONCURRENCY

This is a very likely senior-level topic.

---

# 5.1 Process vs thread

A process is an executing program with its own memory space.

A thread is an execution path within a process.

Multiple threads share the process's memory.

---

# 5.2 What is a race condition?

A race condition occurs when the result depends on the timing of concurrent operations.

Example:

```java
counter++;
```

is not one atomic operation.

Conceptually:

```text
read counter
add 1
write counter
```

Two threads can interfere.

---

# 5.3 What does `synchronized` do?

It provides mutual exclusion and establishes important memory-visibility guarantees.

```java
public synchronized void increment() {
    counter++;
}
```

Only one thread at a time can execute the synchronized critical section for the same monitor.

---

# 5.4 What is `volatile`?

`volatile` guarantees visibility of writes between threads and imposes ordering constraints.

```java
private volatile boolean running = true;
```

If one thread executes:

```java
running = false;
```

another thread reading `running` will see the updated value according to the volatile memory semantics.

But:

```java
volatile int counter;
counter++;
```

is still not an atomic increment.

---

# 5.5 What is AtomicInteger?

```java
AtomicInteger counter = new AtomicInteger();

counter.incrementAndGet();
```

It provides atomic operations without explicit locking for supported operations.

Useful for:

* counters;
* sequence numbers;
* atomic state transitions.

---

# 5.6 What is a deadlock?

A deadlock occurs when threads wait indefinitely for resources held by each other.

Example:

```text
Thread A:
lock A
wait for B

Thread B:
lock B
wait for A
```

Neither can continue.

Ways to reduce deadlock risk:

* consistent lock ordering;
* avoid unnecessary locks;
* use timeouts;
* minimize critical sections;
* prefer higher-level concurrency abstractions.

---

# 5.7 What is a CompletableFuture?

It represents an asynchronous computation.

```java
CompletableFuture<User> future =
    CompletableFuture.supplyAsync(() -> loadUser());
```

Then:

```java
future
    .thenApply(UserDto::from)
    .thenAccept(this::send);
```

It allows asynchronous composition.

---

# 5.8 Virtual threads

Virtual threads are lightweight `Thread` instances managed primarily by the JVM rather than being permanently tied to an OS thread. When a virtual thread blocks on supported I/O, the JVM can suspend it and free the carrier thread for another task.

Example:

```java
try (var executor =
         Executors.newVirtualThreadPerTaskExecutor()) {

    executor.submit(() -> callServiceA());
    executor.submit(() -> callServiceB());
}
```

The important use case is:

```text
many concurrent requests
        +
blocking I/O
        ↓
virtual threads
```

Not:

```text
CPU-heavy computation
        ↓
virtual threads
```

Virtual threads are not a magic CPU-performance mechanism. Oracle explicitly positions them for workloads spending significant time blocked, particularly on I/O.

---

# 5.9 Platform threads vs virtual threads

|                   | Platform thread | Virtual thread       |
| ----------------- | --------------- | -------------------- |
| Managed by        | OS/JVM          | JVM                  |
| Resource cost     | Higher          | Very low             |
| Millions possible | No              | Potentially          |
| Blocking I/O      | Expensive       | Much cheaper         |
| CPU parallelism   | Yes             | Not inherently       |
| Typical use       | CPU work        | High-concurrency I/O |

---

# 5.10 What is Structured Concurrency?

Structured concurrency treats related concurrent tasks as one structured unit.

Conceptually:

```text
Request
 ├── task A
 ├── task B
 └── task C
```

The lifetime and failure of child tasks are related to the parent task.

The goal is easier:

* cancellation;
* error handling;
* observability;
* resource management.

If asked about it in an interview, distinguish **the concept** from the exact Java release/status of the API being used in production.

---

# 6. JVM

---

# 6.1 What is the JVM memory model?

A simplified view:

```text
JVM process
│
├── Heap
│   ├── Objects
│   └── Arrays
│
├── Thread Stacks
│   ├── Local variables
│   └── Call frames
│
├── Metaspace
│   └── Class metadata
│
├── Code Cache
│   └── JIT compiled code
│
└── Native memory
```

---

# 6.2 Stack vs Heap

### Stack

Each thread has its own stack.

Contains execution frames such as:

* local variables;
* method parameters;
* return information;
* references.

### Heap

Shared memory used for objects.

```java
User user = new User();
```

Conceptually:

```text
Stack
 user ──────────┐
                ↓
Heap       User object
```

---

# 6.3 What is Metaspace?

Metaspace stores JVM class metadata.

It replaced PermGen starting with Java 8.

A common interview mistake:

> "Metaspace is where Java objects are stored."

Incorrect.

Objects generally live on the heap.

---

# 6.4 What is garbage collection?

The JVM automatically identifies objects that are no longer reachable and reclaims their memory.

Conceptually:

```text
Root
 ↓
Object A
 ↓
Object B

Object C ← unreachable
```

Object C can eventually be collected.

---

# 6.5 What are GC roots?

Typical GC roots include:

* active thread stacks;
* static references;
* JNI references;
* certain JVM internal references.

The GC determines reachability starting from these roots.

---

# 6.6 What is a memory leak in Java?

Java can still have memory leaks.

Example:

```java
static List<Object> cache = new ArrayList<>();
```

If objects remain strongly referenced forever, the GC cannot reclaim them.

Therefore:

> Garbage collection prevents some forms of manual memory management errors, but not logical memory leaks.

---

# 6.7 What is the JIT?

The Just-In-Time compiler observes frequently executed code and compiles bytecode into optimized machine code.

The JVM can therefore optimize code at runtime based on actual execution behavior.

---

# 7. JAVA 21–25 TOPICS

Java 25 documentation is the current JDK 25 API baseline.

For a modern interview, know at least:

* records;
* sealed classes;
* pattern matching;
* switch expressions;
* text blocks;
* `var`;
* virtual threads;
* structured concurrency concepts;
* scoped values concepts;
* sequenced collections;
* Foreign Function & Memory API;
* modern garbage collectors;
* modern concurrency model.

Do not memorize feature lists.

Understand why they exist.

---

# 8. SPRING CORE

---

# 8.1 What is Spring?

Spring is primarily an ecosystem around:

* dependency injection;
* inversion of control;
* web applications;
* transactions;
* data access;
* security;
* messaging;
* integration;
* testing.

The core Spring container manages application objects called beans. The `ApplicationContext` is responsible for instantiating, configuring and assembling them.

---

# 8.2 What is IoC?

Inversion of Control means that your application does not necessarily create and manage all its dependencies itself.

Instead of:

```java
class OrderService {

    private final PaymentService paymentService =
        new PaymentService();
}
```

Spring manages the dependency.

```java
@Service
class OrderService {

    private final PaymentService paymentService;

    OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

---

# 8.3 What is Dependency Injection?

Dependency Injection is the mechanism through which dependencies are provided to an object.

Preferred:

```java
@Service
class UserService {

    private final UserRepository repository;

    UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

rather than field injection:

```java
@Autowired
private UserRepository repository;
```

Constructor injection is easier to:

* test;
* reason about;
* make immutable;
* validate at construction time.

---

# 8.4 `@Component`, `@Service`, `@Repository`, `@Controller`

They are specialized stereotypes.

```text
@Component
   |
   +── @Service
   +── @Repository
   +── @Controller
```

Their semantic meaning communicates architectural intent.

---

# 8.5 What is a Spring Bean?

A bean is an object managed by the Spring IoC container.

For example:

```java
@Service
public class PaymentService {
}
```

Spring creates and manages the instance.

You can also explicitly define one:

```java
@Configuration
class Configuration {

    @Bean
    PaymentClient paymentClient() {
        return new PaymentClient();
    }
}
```

`@Bean` methods register objects with the Spring container.

---

# 8.6 Singleton vs prototype scope

Default Spring bean scope:

```text
singleton
```

One bean instance per Spring application context.

Prototype:

```java
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
```

creates a new instance when requested from the container.

Important:

> Spring singleton does not mean "one instance in the entire JVM."

It means one instance within that Spring container.

---

# 8.7 What is `@Configuration`?

It defines configuration and bean definitions.

```java
@Configuration
class AppConfig {

    @Bean
    PaymentClient paymentClient() {
        return new PaymentClient();
    }
}
```

---

# 8.8 What is auto-configuration?

Spring Boot detects available libraries and configuration and automatically configures components.

For example, if the appropriate web dependencies are present, Boot can configure a web application infrastructure.

The purpose is:

> sensible defaults with the ability to override them.

---

# 8.9 What is Spring Boot?

Spring Boot builds on Spring and provides:

* auto-configuration;
* starter dependencies;
* embedded servers;
* production features;
* externalized configuration;
* easier application bootstrap.

Current Spring Boot documentation lists 4.1.0 as stable, alongside maintained 4.0.x and 3.x lines.

For an interview, however, determine which version the actual project uses before discussing version-specific behavior.

---

# 9. SPRING AOP

---

# 9.1 What is AOP?

Aspect-Oriented Programming separates cross-cutting concerns from business logic.

Examples:

* logging;
* transactions;
* security;
* metrics;
* tracing.

Example:

```java
@Transactional
public void transfer(...) {
    ...
}
```

Transactions are a classic example of cross-cutting behavior.

---

# 9.2 What is a Spring proxy?

Spring frequently implements cross-cutting behavior through proxies.

Conceptually:

```text
Caller
  ↓
Proxy
  ↓
Actual bean
```

The proxy can perform logic before/after calling the target.

This explains many Spring behaviors and pitfalls.

---

# 9.3 Why can self-invocation break `@Transactional`?

Consider:

```java
public void methodA() {
    methodB();
}

@Transactional
public void methodB() {
}
```

If `methodA()` calls `methodB()` directly on `this`, the call may bypass the Spring proxy.

Therefore the transactional interceptor may not execute.

This is one of the classic Spring interview questions.

---

# 10. SPRING TRANSACTIONS

---

# 10.1 What is a transaction?

A transaction groups operations into a unit of work with properties commonly summarized by ACID:

```text
Atomicity
Consistency
Isolation
Durability
```

---

# 10.2 What does `@Transactional` do?

```java
@Transactional
public void transferMoney(...) {
    debit();
    credit();
}
```

Spring typically uses an interceptor/proxy to start and commit/rollback the transaction around the method execution.

---

# 10.3 What is transaction propagation?

Propagation defines how a transaction behaves when a transactional method calls another transactional method.

Important modes include:

```text
REQUIRED
REQUIRES_NEW
MANDATORY
SUPPORTS
NOT_SUPPORTED
NEVER
NESTED
```

Most commonly:

### REQUIRED

Join an existing transaction or create one.

### REQUIRES_NEW

Suspend the existing transaction and start a new one.

---

# 10.4 What is transaction isolation?

Isolation determines how concurrent transactions can observe each other's changes.

Common levels:

```text
READ_UNCOMMITTED
READ_COMMITTED
REPEATABLE_READ
SERIALIZABLE
```

The exact behavior depends on the database.

Potential anomalies include:

### Dirty read

Reading uncommitted data.

### Non-repeatable read

Reading the same row twice and getting different committed values.

### Phantom read

A repeated query returns a different set of rows.

---

# 10.5 Why is transaction isolation important in banking?

Because financial operations require strong consistency guarantees.

Imagine:

```text
Account A = CHF 10,000
```

Two concurrent withdrawals:

```text
Withdrawal 1 = CHF 8,000
Withdrawal 2 = CHF 8,000
```

Without appropriate concurrency control, both transactions might observe:

```text
10,000
```

and both approve the withdrawal.

The system could end up with an invalid balance.

This is why concurrency, transactions, locking and idempotency are particularly important in financial applications.

---

# 11. SPRING DATA / JPA

---

# 11.1 What is JPA?

JPA is a Java specification for object-relational persistence.

Hibernate is a common implementation.

Conceptually:

```text
Application
    ↓
JPA API
    ↓
Hibernate
    ↓
JDBC
    ↓
Database
```

---

# 11.2 Entity vs DTO

An entity represents persistence state.

```java
@Entity
class Client {
    ...
}
```

A DTO represents data exchanged between layers or systems.

```java
record ClientDto(
    Long id,
    String name
) {}
```

Avoid exposing entities directly through public APIs when it creates undesirable coupling.

---

# 11.3 Lazy vs eager loading

### Lazy

Associated data is loaded when accessed.

### Eager

Associated data is loaded immediately according to the persistence configuration/query behavior.

Lazy loading can help avoid unnecessary data retrieval, but can also cause problems such as:

```text
LazyInitializationException
```

or N+1 queries.

---

# 11.4 What is the N+1 query problem?

Suppose:

```java
List<Client> clients = repository.findAll();
```

Then for each client:

```java
client.getAccounts();
```

You may get:

```text
1 query for clients
+
N queries for accounts
```

Total:

```text
N + 1 queries
```

Potential solutions:

* fetch joins;
* entity graphs;
* carefully designed queries;
* projections;
* batch fetching.

---

# 11.5 What is optimistic locking?

Use a version field:

```java
@Version
private long version;
```

Conceptually:

```text
Transaction A reads version 5
Transaction B reads version 5

A updates → version 6

B updates → expected version 5
              actual version 6
              ↓
          conflict
```

This is often useful when conflicts are relatively rare.

---

# 11.6 What is pessimistic locking?

The database locks the relevant row/resource so competing transactions cannot modify it in the same way.

Useful when contention is expected and correctness requires stronger locking behavior.

Trade-off:

```text
stronger coordination
        ↓
potentially lower concurrency
        +
deadlock risk
```

---

# 12. REST APIs

---

# 12.1 What makes an API RESTful?

Common principles include:

* resources;
* HTTP methods;
* stateless requests;
* meaningful status codes;
* representations.

Example:

```http
GET /clients/42
```

```http
POST /clients
```

```http
PUT /clients/42
```

```http
DELETE /clients/42
```

---

# 12.2 PUT vs PATCH

### PUT

Usually represents replacement of a resource representation.

### PATCH

Represents partial modification.

Example:

```http
PATCH /clients/42
```

```json
{
  "email": "new@example.com"
}
```

---

# 12.3 What is idempotency?

An operation is idempotent if performing it multiple times has the same intended final effect as performing it once.

Examples:

```text
GET
PUT
DELETE
```

are generally designed to be idempotent.

`POST` generally is not inherently idempotent.

This is extremely important in financial systems.

---

# 12.4 Why is idempotency important for payments?

Imagine a client sends:

```text
POST /transfer
```

The server processes it.

The network fails before the response reaches the client.

The client retries.

Without idempotency, the transfer could execute twice.

A common solution is an idempotency key:

```http
Idempotency-Key: abc-123
```

The server records the result associated with that key.

A retry can then return the existing result instead of executing the operation again.

---

# 13. SPRING SECURITY

---

# 13.1 Authentication vs authorization

### Authentication

> Who are you?

### Authorization

> What are you allowed to do?

Example:

```text
Authentication:
User = Alice

Authorization:
Alice may read portfolio
Alice may not approve transfer
```

---

# 13.2 What is OAuth2?

OAuth 2.0 is primarily an authorization framework.

It allows a client to obtain access to protected resources using delegated authorization.

---

# 13.3 What is OpenID Connect?

OpenID Connect builds an identity layer on top of OAuth 2.0.

It is used for authentication and identity information.

---

# 13.4 What is JWT?

A JSON Web Token contains claims and is digitally signed.

Typical structure:

```text
header.payload.signature
```

Important:

> A JWT being encoded does not mean it is encrypted.

Anyone holding the token can generally decode the header and payload.

The signature provides integrity/authenticity.

---

# 13.5 Where should authentication tokens be handled?

There is no universal answer.

Consider:

* XSS;
* CSRF;
* browser architecture;
* cookie security;
* token lifetime;
* refresh strategy;
* same-site policy;
* backend architecture.

For sensitive financial applications, security trade-offs should be explicitly reasoned about rather than blindly following a pattern.

---

# 14. DATABASE QUESTIONS

---

# 14.1 SQL vs NoSQL

### SQL

Good for:

* strong relational modeling;
* transactions;
* joins;
* constraints;
* structured data.

### NoSQL

Can be useful for:

* specific scalability models;
* flexible schemas;
* specialized access patterns.

A financial application does not automatically require NoSQL because it is "modern".

Choose according to requirements.

---

# 14.2 What is an index?

An index accelerates data retrieval for certain queries.

Conceptually:

```text
Table
 ↓
Index
 ↓
row location
```

But indexes have costs:

* storage;
* slower writes;
* maintenance;
* memory/cache usage.

Therefore:

> Don't index every column.

---

# 14.3 Composite indexes

Suppose:

```sql
WHERE client_id = ?
AND status = ?
```

An index such as:

```sql
(client_id, status)
```

may be useful.

Column ordering matters.

---

# 14.4 What is normalization?

Normalization reduces unnecessary duplication and update anomalies.

Typical concepts:

* 1NF;
* 2NF;
* 3NF.

But excessive normalization can make queries unnecessarily complex.

Senior engineers understand the trade-off between:

```text
normalization
vs
read performance / query complexity
```

---

# 15. ARCHITECTURE

---

# 15.1 Layered architecture

Typical structure:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Advantages:

* simple;
* familiar;
* easy to implement.

Potential problem:

Business logic can become concentrated inside large services.

---

# 15.2 Hexagonal architecture

Also called Ports and Adapters.

```text
             REST API
                ↓
           [ Adapter ]
                ↓
          ┌───────────┐
          │   Port    │
          └─────┬─────┘
                ↓
          Domain logic
                ↓
          ┌─────┴─────┐
          │   Ports   │
          └─────┬─────┘
                ↓
        DB / external API
           [Adapters]
```

The domain should not depend directly on infrastructure.

---

# 15.3 Clean Architecture

A simplified model:

```text
        Frameworks
            ↓
     Interface adapters
            ↓
      Application logic
            ↓
       Domain entities
```

The dependency direction points toward the core.

---

# 15.4 Why use hexagonal architecture?

Benefits:

* testability;
* replaceable infrastructure;
* domain isolation;
* reduced coupling;
* easier integration testing.

But:

> Architecture has a cost.

For a tiny CRUD application, introducing ten layers may be worse than a simple design.

---

# 15.5 Microservices vs monolith

A monolith can be:

* easier to deploy;
* easier to debug;
* easier to test;
* simpler operationally.

Microservices can provide:

* independent deployment;
* team autonomy;
* independent scaling;
* bounded contexts.

But introduce:

* network failures;
* distributed transactions;
* observability complexity;
* deployment complexity;
* eventual consistency;
* operational overhead.

The best answer in an interview is rarely:

> "Microservices are better."

It is:

> "It depends on the business and organizational constraints."

---

# 15.6 Synchronous vs asynchronous communication

### Synchronous

```text
Service A
   ↓ HTTP
Service B
   ↓
response
```

Advantages:

* simple mental model;
* immediate response.

Disadvantages:

* coupling;
* latency;
* cascading failures.

### Asynchronous

```text
Service A
   ↓
Message broker
   ↓
Service B
```

Advantages:

* decoupling;
* buffering;
* resilience;
* scalability.

Disadvantages:

* eventual consistency;
* harder debugging;
* duplicate messages;
* ordering problems.

---

# 15.7 What is eventual consistency?

Different parts of a distributed system may temporarily contain different states.

Eventually:

```text
System A → updated
System B → updated
System C → updated
```

This is a fundamental distributed-systems trade-off.

---

# 15.8 What is the Saga pattern?

A Saga decomposes a distributed business transaction into a sequence of local transactions.

Example:

```text
Create transfer
     ↓
Reserve funds
     ↓
Execute transfer
     ↓
Notify client
```

If a later step fails, compensating actions may be performed.

This avoids requiring a distributed ACID transaction across multiple services.

---

# 16. RESILIENCE

---

# 16.1 What is a timeout?

Never allow an external dependency to wait indefinitely.

Bad:

```text
Service A
   ↓
Service B
   ↓
wait forever
```

Better:

```text
Service A
   ↓
timeout
   ↓
fallback/error
```

---

# 16.2 What is retry?

Retry a failed operation when failure is likely transient.

Examples:

* network timeout;
* temporary service unavailability.

Do not blindly retry every error.

---

# 16.3 What is exponential backoff?

Instead of:

```text
retry immediately
retry immediately
retry immediately
```

use increasing delays:

```text
100 ms
200 ms
400 ms
800 ms
...
```

Usually with jitter.

---

# 16.4 What is a circuit breaker?

Conceptually:

```text
CLOSED
  ↓ failures
OPEN
  ↓ timeout
HALF-OPEN
  ↓ success
CLOSED
```

The circuit breaker prevents continuously calling an unhealthy dependency.

---

# 16.5 Why is retry + circuit breaker dangerous?

Poorly configured retries can amplify traffic during an outage.

Imagine:

```text
1,000 requests
×
3 retries
=
3,000 requests
```

This can make the outage worse.

---

# 17. OBSERVABILITY

A senior developer should understand:

```text
Logs
Metrics
Traces
```

---

# 17.1 Logs

Useful for:

* detailed events;
* errors;
* debugging.

Use structured logs.

Avoid logging:

* passwords;
* authentication tokens;
* private financial information;
* unnecessary personal data.

---

# 17.2 Metrics

Examples:

```text
request_count
error_rate
latency
CPU
memory
GC pauses
thread count
```

---

# 17.3 Distributed tracing

A request can travel:

```text
Angular
 ↓
API Gateway
 ↓
Service A
 ↓
Service B
 ↓
Database
```

A trace allows you to follow the complete request.

---

# 18. TESTING

---

# 18.1 Unit test

Tests one component in isolation.

```java
@Test
void shouldCalculateFee() {
    ...
}
```

Usually fast.

---

# 18.2 Integration test

Tests multiple components together.

Example:

```text
Spring
+
Repository
+
Database
```

---

# 18.3 End-to-end test

Tests the complete user workflow.

```text
Browser
 ↓
Angular
 ↓
Backend
 ↓
Database
```

---

# 18.4 What should you mock?

Mock external boundaries when appropriate.

For example:

```text
PaymentService
     ↓
Mock PaymentProvider
```

But excessive mocking can create tests that verify implementation details rather than behavior.

---

# 18.5 What is the test pyramid?

A simplified model:

```text
        E2E
       /   \
 Integration
    /       \
   Unit tests
```

Many fast unit tests.

Fewer integration tests.

Even fewer expensive E2E tests.

---

# 19. ANGULAR

Angular's current release line is significantly newer than Angular 17: Angular 22 is the current active major line as of the current documentation, with Angular 21 in LTS.

Important modern Angular topics include:

* standalone components;
* signals;
* `computed`;
* `effect`;
* signal inputs;
* signal queries;
* modern control flow;
* lazy loading;
* RxJS;
* dependency injection;
* OnPush;
* SSR;
* testing.

---

# 19.1 What is a component?

A component combines:

```text
TypeScript
+
HTML template
+
styles
+
metadata
```

Example:

```typescript
@Component({
  selector: 'app-client',
  templateUrl: './client.html'
})
export class ClientComponent {}
```

---

# 19.2 What are standalone components?

Modern Angular allows components to declare their dependencies directly instead of relying on `NgModule` declarations. Angular provides migration tooling for converting existing applications incrementally.

Example:

```typescript
@Component({
  standalone: true,
  imports: [CommonModule],
  template: `...`
})
```

Modern Angular increasingly favors standalone architecture.

---

# 19.3 What are Signals?

A signal represents reactive state.

```typescript
count = signal(0);
```

Read:

```typescript
count()
```

Write:

```typescript
count.set(10);
```

or:

```typescript
count.update(value => value + 1);
```

Angular tracks where signals are read and can update affected consumers accordingly.

---

# 19.4 `signal` vs `computed`

Writable:

```typescript
count = signal(10);
```

Derived:

```typescript
double = computed(() => this.count() * 2);
```

`computed()` is:

* read-only;
* derived;
* lazily evaluated;
* memoized.

---

# 19.5 What is `effect()`?

Used for side effects triggered by signal changes.

```typescript
effect(() => {
    console.log(this.count());
});
```

Do not use effects as a replacement for normal state derivation.

If:

```text
B = f(A)
```

prefer:

```typescript
computed(() => ...)
```

rather than:

```typescript
effect(() => B.set(...))
```

---

# 19.6 Signals vs RxJS

They solve related but different problems.

### Signals

Excellent for:

* local state;
* derived state;
* synchronous reactive UI state.

### RxJS

Excellent for:

* asynchronous streams;
* events;
* HTTP;
* cancellation;
* composition of asynchronous operations.

A modern Angular application may use both.

---

# 19.7 What is OnPush?

`ChangeDetectionStrategy.OnPush` allows Angular to skip unnecessary component subtree checks under appropriate conditions.

Signals integrate particularly well with OnPush: when an OnPush component reads a signal in its template and the signal changes, Angular tracks that dependency and marks the component appropriately.

---

# 19.8 Angular control flow

Modern Angular supports:

```html
@if (user) {
    <p>{{ user.name }}</p>
} @else {
    <p>No user</p>
}
```

and:

```html
@for (user of users; track user.id) {
    <app-user [user]="user" />
}
```

instead of relying exclusively on older structural directive syntax.

---

# 19.9 Why is `track` important?

Consider:

```html
@for (user of users; track user.id) {
    ...
}
```

Angular can identify which logical item corresponds to which DOM element.

Without appropriate tracking, unnecessary DOM work can occur.

---

# 19.10 What is dependency injection in Angular?

Angular provides dependencies through its DI system.

```typescript
@Injectable({
    providedIn: 'root'
})
export class ClientService {
}
```

Then:

```typescript
private clientService = inject(ClientService);
```

This avoids manually constructing dependencies.

---

# 19.11 What is an Angular service?

A service encapsulates reusable logic such as:

* HTTP communication;
* state;
* business-related client logic;
* shared utilities.

Avoid turning one service into a giant "everything service."

---

# 19.12 What is an HTTP interceptor?

An interceptor can modify or observe HTTP requests/responses.

Common uses:

* authentication;
* correlation IDs;
* logging;
* error handling;
* headers.

Example conceptually:

```text
Angular
 ↓
Interceptor
 ↓
HTTP
 ↓
Backend
```

---

# 19.13 What is lazy loading?

Instead of loading the entire application immediately:

```text
Application
 ├── Dashboard
 ├── Clients
 ├── Portfolio
 └── Administration
```

load features when needed.

Benefits:

* smaller initial bundle;
* faster startup;
* better scalability.

---

# 19.14 What is a route guard?

Controls whether navigation can occur.

Examples:

```text
authenticated?
authorized?
has permission?
```

Important security point:

> A frontend guard is not a security boundary.

The backend must enforce authorization.

---

# 20. TYPESCRIPT

---

# 20.1 Interface vs type

Both can describe shapes.

```typescript
interface User {
    id: number;
    name: string;
}
```

and:

```typescript
type User = {
    id: number;
    name: string;
};
```

Interfaces support declaration merging and are often useful for object contracts.

Types are particularly powerful for unions:

```typescript
type Status =
    | 'ACTIVE'
    | 'BLOCKED'
    | 'CLOSED';
```

---

# 20.2 What is strict typing?

Avoid:

```typescript
any
```

when possible.

Prefer:

```typescript
unknown
```

when the type is genuinely unknown and narrow it safely.

---

# 20.3 What is the difference between `null` and `undefined`?

Roughly:

```text
undefined → value not provided / absent
null      → explicitly no value
```

With strict TypeScript configuration, they are distinct types.

---

# 21. FINANCIAL / BANKING QUESTIONS

This section can distinguish you from a generic software engineer.

---

# 21.1 How would you design a money transfer API?

Start with:

```http
POST /transfers
```

Request:

```json
{
  "sourceAccount": "A123",
  "destinationAccount": "B456",
  "amount": "1000.00",
  "currency": "CHF"
}
```

Important concerns:

### Validation

* amount > 0;
* valid currency;
* source account exists;
* destination account exists.

### Authorization

Can this user initiate the transfer?

### Concurrency

Can two transfers spend the same funds?

### Transactionality

Debit and credit must obey appropriate consistency rules.

### Idempotency

Retries must not create duplicate transfers.

### Auditability

Every important state transition should be traceable.

### Security

Do not expose sensitive information.

### Failure handling

What happens if an external payment system is unavailable?

---

# 21.2 Why use BigDecimal for money?

Do not use:

```java
double
```

for financial monetary calculations.

Binary floating-point cannot represent many decimal values exactly.

Use:

```java
BigDecimal
```

Example:

```java
BigDecimal amount =
    new BigDecimal("10.25");
```

Also explicitly define rounding behavior when appropriate:

```java
amount.setScale(
    2,
    RoundingMode.HALF_UP
);
```

---

# 21.3 Why is `new BigDecimal(0.1)` dangerous?

Because `0.1` is already represented as a binary floating-point approximation before being passed to `BigDecimal`.

Prefer:

```java
new BigDecimal("0.1")
```

or:

```java
BigDecimal.valueOf(0.1)
```

---

# 21.4 How would you audit a financial operation?

Record immutable business events such as:

```text
TRANSFER_CREATED
TRANSFER_AUTHORIZED
TRANSFER_EXECUTED
TRANSFER_REJECTED
```

Include appropriate metadata:

* timestamp;
* actor;
* operation ID;
* correlation ID;
* relevant business identifiers;
* previous/new state where appropriate.

Avoid putting sensitive secrets into logs.

---

# 21.5 What is the difference between logging and auditing?

### Logging

Primarily operational.

```text
Service failed to call payment provider
```

### Auditing

Business/security traceability.

```text
User X approved transfer Y
```

An audit trail should generally have stronger integrity and retention requirements.

---

# 21.6 What does "security by design" mean?

Security is considered from the architecture/design stage rather than added afterward.

Examples:

* least privilege;
* secure defaults;
* input validation;
* encryption;
* authentication;
* authorization;
* auditability;
* secret management;
* dependency scanning;
* secure logging.

---

# 22. SYSTEM DESIGN QUESTION

A common senior interview question:

> Design a portfolio management system.

Start by clarifying requirements.

### Functional

* display portfolios;
* positions;
* transactions;
* performance;
* valuations.

### Non-functional

* security;
* availability;
* consistency;
* auditability;
* performance;
* scalability.

Then design:

```text
Angular
   ↓
API
   ↓
Portfolio Service
   ↓
Database
```

Potential external dependencies:

```text
Market Data Provider
       ↓
Portfolio Service

FX Provider
       ↓
Portfolio Service
```

Then discuss:

* caching;
* timeouts;
* retries;
* authentication;
* authorization;
* auditing;
* observability.

---

# 23. SYSTEM DESIGN: HOW TO ANSWER

Use this sequence.

## Step 1 — Requirements

Ask:

```text
Who uses it?
What operations exist?
What is the expected scale?
What latency is acceptable?
What consistency is required?
What must be audited?
```

## Step 2 — Domain model

Identify:

```text
Client
Account
Portfolio
Position
Transaction
Security
Currency
```

## Step 3 — APIs

Define important endpoints.

## Step 4 — Architecture

Choose:

```text
monolith
or
modular monolith
or
microservices
```

based on actual requirements.

## Step 5 — Data

Discuss:

* schema;
* indexes;
* transactions;
* consistency;
* locking.

## Step 6 — Reliability

Discuss:

* timeout;
* retry;
* circuit breaker;
* idempotency.

## Step 7 — Security

Discuss:

* authentication;
* authorization;
* encryption;
* audit.

## Step 8 — Observability

Discuss:

* logs;
* metrics;
* traces;
* alerts.

This structure is much more impressive than immediately drawing ten microservices.

---

# 24. SOLID

---

# 24.1 Single Responsibility Principle

A class should have one coherent responsibility.

Bad:

```java
class UserService {
    createUser();
    sendEmail();
    generatePdf();
    saveToDatabase();
}
```

Better separation:

```text
UserService
EmailService
PdfService
UserRepository
```

---

# 24.2 Open/Closed Principle

Software should generally be open for extension but closed for modification.

Prefer polymorphism where appropriate over constantly modifying a giant conditional.

---

# 24.3 Liskov Substitution Principle

If:

```text
B extends A
```

code expecting `A` should be able to use `B` without violating the expected behavior of `A`.

Classic bad example:

```java
class Bird {
    void fly() {}
}

class Penguin extends Bird {
    void fly() {
        throw new UnsupportedOperationException();
    }
}
```

The abstraction is wrong.

---

# 24.4 Interface Segregation Principle

Prefer small, focused interfaces over huge interfaces.

Instead of:

```java
interface Employee {
    work();
    eat();
    fly();
}
```

split responsibilities when clients do not need all operations.

---

# 24.5 Dependency Inversion Principle

High-level business logic should depend on abstractions rather than concrete infrastructure.

Example:

```text
Application
    ↓
PaymentPort
    ↓
PaymentAdapter
    ↓
External API
```

This is fundamental to hexagonal architecture.

---

# 25. DESIGN PATTERNS

Know these particularly well:

### Strategy

```text
PaymentStrategy
 ├── CardStrategy
 ├── BankTransferStrategy
 └── CryptoStrategy
```

### Factory

Centralizes object creation.

### Builder

Useful for complex object construction.

### Adapter

Converts one interface to another.

### Decorator

Adds behavior without modifying the original object.

### Observer

One-to-many event notification.

### Template Method

Defines an algorithm skeleton.

### Command

Represents an operation as an object.

### Repository

Abstracts persistence.

### Proxy

Controls access to another object.

Spring uses proxy-based techniques extensively.

---

# 26. COMMON TRICK QUESTIONS

---

## Is HashMap thread-safe?

No.

---

## Is ConcurrentHashMap completely lock-free?

No.

It uses sophisticated concurrency mechanisms but is not equivalent to "no synchronization."

---

## Does `final` make an object immutable?

No.

```java
final List<String> list = new ArrayList<>();
```

The reference cannot change, but the list can.

---

## Is String mutable?

No.

---

## Does `volatile` make `counter++` thread-safe?

No.

---

## Is Java pass-by-reference?

No.

Java is pass-by-value.

---

## Is Spring singleton the same as GoF Singleton?

No.

Spring manages one bean instance per application context by default.

---

## Does `@Transactional` guarantee rollback for every exception?

No.

Rollback behavior depends on the exception and transaction configuration.

---

## Are REST APIs automatically secure because they use HTTPS?

No.

HTTPS protects transport.

It does not automatically provide:

* authorization;
* correct authentication;
* business validation;
* secure storage;
* protection against application-level vulnerabilities.

---

## Are frontend guards security?

No.

The backend must enforce authorization.

---

# 27. PERFORMANCE QUESTIONS

---

# 27.1 An API is slow. What do you do?

Do not immediately optimize code.

First measure.

Check:

```text
Client
 ↓
Network
 ↓
Controller
 ↓
Service
 ↓
Database
 ↓
External services
```

Look at:

* latency;
* traces;
* database execution plans;
* GC;
* CPU;
* memory;
* thread pools;
* connection pools.

Then identify the actual bottleneck.

---

# 27.2 Database query is slow. What do you inspect?

1. Execution plan.
2. Indexes.
3. Number of rows.
4. Join strategy.
5. Selectivity.
6. Network latency.
7. N+1 behavior.
8. Lock contention.

---

# 27.3 API has high CPU usage

Investigate:

```text
profiling
GC
hot methods
serialization
regex
large collections
synchronization
busy loops
```

Do not assume:

> "We need more threads."

More threads can make CPU contention worse.

---

# 28. MEMORY / JVM PRODUCTION INCIDENT

Question:

> The application is running out of memory. What do you do?

Answer structure:

### 1. Confirm

Check:

```text
heap usage
GC activity
RSS/native memory
container limits
```

### 2. Identify

Use:

* heap dump;
* JFR;
* GC logs;
* profilers.

### 3. Look for

* large caches;
* unbounded collections;
* retained objects;
* classloader leaks;
* huge responses;
* excessive buffering.

### 4. Fix the cause

Do not simply increase:

```text
-Xmx
```

Increasing heap may only delay the problem.

---

# 29. PRODUCTION INCIDENT QUESTION

> A critical banking API is returning 500 errors. What do you do?

Strong answer:

```text
1. Assess impact
2. Check monitoring
3. Check recent deployments
4. Inspect logs/traces
5. Identify failing dependency
6. Stabilize the system
7. Roll back if appropriate
8. Communicate impact
9. Preserve evidence
10. Perform root-cause analysis
11. Implement preventive action
```

The key is:

> First restore service safely, then perform deep analysis.

---

# 30. CODE REVIEW QUESTIONS

A senior developer should review:

### Correctness

Does it do what the business requires?

### Security

Could sensitive information leak?

### Concurrency

Could two requests interfere?

### Transactions

Is the transaction boundary correct?

### Performance

Any:

* N+1;
* unnecessary allocations;
* expensive loops;
* excessive database calls?

### Maintainability

Can another developer understand it?

### Testing

Are important behaviors covered?

---

# 31. BEHAVIORAL QUESTIONS

Private banks will not necessarily evaluate you purely as a coder.

Prepare answers for:

---

## Tell me about yourself.

Structure:

```text
Current profile
↓
Experience
↓
Technical specialization
↓
Interesting projects
↓
Why this opportunity
```

Avoid reciting your CV line by line.

---

## Why Lombard Odier?

A strong answer should connect:

```text
private banking
+
technical challenge
+
long-term engineering
+
international environment
```

For example:

> "I am interested in Lombard Odier because the combination of software engineering and private banking creates interesting technical constraints around security, data integrity, reliability and long-term maintainability. I am particularly interested in environments where software is directly connected to important business processes rather than being treated only as a technical product."

Lombard Odier explicitly emphasizes long-term client relationships, innovation, excellence, integrity and teamwork, which gives you concrete themes to connect to your answer.

---

# 32. WHY SWITZERLAND?

Do not make your answer primarily about:

```text
salary
taxes
quality of life
```

Even if those are personal motivations.

For the interview, emphasize:

* international environment;
* financial industry;
* engineering standards;
* proximity to major financial institutions;
* long-term career development.

---

# 33. TELL ME ABOUT A TECHNICAL DISAGREEMENT

Use:

```text
Situation
Task
Action
Result
```

Avoid:

> "I was right and the other developer was wrong."

Better:

> "We had different assumptions. I proposed comparing the approaches against measurable criteria..."

This demonstrates engineering maturity.

---

# 34. TELL ME ABOUT A FAILURE

Do not say:

> "I don't really make mistakes."

Instead:

```text
What happened
↓
Why it happened
↓
What you changed
↓
What you learned
```

The important part is the prevention mechanism afterward.

---

# 35. TELL ME ABOUT A DIFFICULT BUG

Excellent structure:

```text
Symptom
↓
Hypotheses
↓
Measurements
↓
Root cause
↓
Fix
↓
Prevention
```

This demonstrates actual engineering methodology.

---

# 36. WHAT MAKES A SENIOR DEVELOPER?

A strong answer:

> "A senior developer is not simply someone who knows more APIs. A senior developer understands trade-offs, can reason about the system as a whole, anticipates failure modes, communicates effectively, and makes decisions that improve the maintainability and reliability of the system over time."

---

# 37. QUESTIONS YOU SHOULD BE ABLE TO ANSWER WITHOUT HESITATION

## Java

* What is the JVM?
* Stack vs heap?
* What is GC?
* What are GC roots?
* What is Metaspace?
* What is JIT?
* `==` vs `equals()`?
* `equals()` / `hashCode()` contract?
* HashMap internals?
* HashSet internals?
* HashMap vs TreeMap?
* ArrayList vs LinkedList?
* List vs Set?
* mutable vs immutable?
* unmodifiable vs immutable?
* Optional?
* records?
* sealed classes?
* pattern matching?
* streams?
* map vs flatMap?
* parallel streams?
* synchronized?
* volatile?
* AtomicInteger?
* locks?
* deadlocks?
* CompletableFuture?
* virtual threads?
* structured concurrency?

---

# 38. SPRING QUESTIONS

* What is IoC?
* What is DI?
* Why constructor injection?
* What is a Bean?
* Bean lifecycle?
* Bean scopes?
* `@Component` vs `@Service`?
* `@Repository`?
* `@Configuration`?
* `@Bean`?
* auto-configuration?
* Spring Boot starters?
* Spring profiles?
* configuration properties?
* AOP?
* Spring proxies?
* `@Transactional`?
* transaction propagation?
* isolation?
* rollback?
* self-invocation?
* Spring Security?
* filters?
* OAuth2?
* JWT?
* REST controllers?
* validation?
* exception handling?
* Actuator?
* observability?

---

# 39. DATABASE QUESTIONS

* ACID?
* normalization?
* indexes?
* composite indexes?
* joins?
* isolation levels?
* optimistic locking?
* pessimistic locking?
* deadlocks?
* N+1?
* lazy loading?
* eager loading?
* pagination?
* transactions?
* connection pool?
* database migrations?
* SQL injection?

---

# 40. ANGULAR QUESTIONS

* component architecture?
* standalone components?
* dependency injection?
* signals?
* computed?
* effect?
* RxJS?
* Observable vs Promise?
* Subject?
* BehaviorSubject?
* operators?
* subscription management?
* OnPush?
* change detection?
* lazy loading?
* route guards?
* interceptors?
* forms?
* reactive forms?
* signal forms?
* SSR?
* hydration?
* testing?
* TypeScript strict mode?

Angular's current documentation also introduces Signal Forms for Angular 21+, while explicitly noting that reactive forms remain a solid choice where production stability requirements favor the established approach.

---

# 41. QUESTIONS SPECIFICALLY WORTH PREPARING FOR A BANK

These are particularly valuable:

### 1.

> How would you prevent a financial transaction from being executed twice?

Answer:

```text
Idempotency key
+
persistent operation state
+
transactional guarantees
+
appropriate concurrency control
```

### 2.

> How do you guarantee auditability?

Answer:

```text
immutable business events
+
audit records
+
correlation IDs
+
controlled access
+
appropriate retention
```

### 3.

> How do you handle concurrent transfers?

Answer:

Discuss:

```text
transaction isolation
optimistic/pessimistic locking
atomic database operations
idempotency
business invariants
```

### 4.

> What happens if the payment provider is unavailable?

Discuss:

```text
timeout
retry with backoff
circuit breaker
idempotency
asynchronous processing
dead-letter handling
monitoring
```

### 5.

> How do you secure a banking API?

Discuss:

```text
authentication
authorization
TLS
input validation
least privilege
secure secrets
audit
rate limiting
logging hygiene
dependency security
```

---

# 42. A VERY GOOD SENIOR-LEVEL ANSWER PATTERN

When asked:

> "How would you implement X?"

Don't immediately start coding.

Say:

> "Before choosing the implementation, I would clarify the functional and non-functional requirements."

Then discuss:

```text
Functional requirements
        ↓
Constraints
        ↓
Architecture
        ↓
Data model
        ↓
Concurrency
        ↓
Failure modes
        ↓
Security
        ↓
Observability
        ↓
Testing
```

This is the difference between:

```text
developer thinking
```

and:

```text
senior engineering thinking
```

---

# 43. 20 QUESTIONS I WOULD EXPECT YOU TO MASTER

If your interview is short, prioritize these:

1. Explain the JVM memory model.
2. Explain HashMap internals.
3. Explain `equals()` / `hashCode()`.
4. Explain immutability.
5. Explain concurrency and race conditions.
6. Explain `synchronized` vs `volatile`.
7. Explain virtual threads.
8. Explain Spring IoC and DI.
9. Explain Spring proxies.
10. Explain `@Transactional`.
11. Explain transaction isolation.
12. Explain JPA lazy loading and N+1.
13. Design an idempotent REST API.
14. Explain optimistic vs pessimistic locking.
15. Explain REST vs messaging.
16. Explain hexagonal architecture.
17. Explain Angular signals.
18. Explain Angular change detection / OnPush.
19. Design a secure financial API.
20. Diagnose a production performance problem.

---

# 44. QUESTIONS THAT CAN SEPARATE A STRONG SENIOR FROM A MID-LEVEL DEVELOPER

Be prepared to discuss the trade-offs rather than just definitions.

### Virtual threads

Not:

> "Virtual threads are faster."

But:

> "They improve scalability for high-concurrency workloads dominated by blocking operations, while CPU-bound work remains limited by available CPU resources."

### Microservices

Not:

> "Microservices scale better."

But:

> "They allow independent scaling and deployment, but introduce distributed-system complexity."

### Caching

Not:

> "Caching improves performance."

But:

> "Caching reduces latency and load at the cost of consistency, invalidation complexity and memory."

### Transactions

Not:

> "`@Transactional` makes the method safe."

But:

> "It defines transactional behavior around the method, but correctness still depends on transaction boundaries, propagation, isolation, database semantics and proxy invocation."

### Angular Signals

Not:

> "Signals replace RxJS."

But:

> "Signals provide reactive state and dependency tracking, while RxJS remains valuable for asynchronous streams and event composition."

---

# 45. FINAL RAPID-FIRE SECTION

Try answering these aloud in less than 60 seconds each.

```text
What is immutability?

What is the difference between == and equals?

Why override hashCode with equals?

How does HashMap work?

HashMap vs ConcurrentHashMap?

ArrayList vs LinkedList?

HashSet vs TreeSet?

HashMap vs TreeMap?

What is a Stream?

map vs flatMap?

What is a race condition?

volatile vs synchronized?

What is a deadlock?

What are virtual threads?

When should you NOT use virtual threads?

What is IoC?

What is dependency injection?

Why constructor injection?

What is a Spring Bean?

What is auto-configuration?

What is AOP?

What is a Spring proxy?

Why can self-invocation break @Transactional?

What is transaction isolation?

Optimistic vs pessimistic locking?

What is N+1?

What is idempotency?

PUT vs PATCH?

Authentication vs authorization?

OAuth2 vs OpenID Connect?

What is JWT?

What is an index?

What is eventual consistency?

What is a circuit breaker?

What is exponential backoff?

What is hexagonal architecture?

Monolith vs microservices?

Synchronous vs asynchronous communication?

What are Angular Signals?

Signal vs Observable?

computed vs effect?

What is OnPush?

What are standalone components?

What is lazy loading?

What is an HTTP interceptor?

How do you secure a banking API?

How do you guarantee a transfer isn't executed twice?

How would you debug a slow API?

How would you investigate an OutOfMemoryError?

How would you respond to a production incident?

Why Lombard Odier?

Why Switzerland?

Why should we hire you?
```

---

# 46. THE MOST IMPORTANT PRINCIPLE

For a senior interview, don't try to demonstrate that you know every API.

Demonstrate that you can **reason about a system**.

A strong answer repeatedly connects:

```text
Business requirement
        ↓
Technical choice
        ↓
Trade-off
        ↓
Failure mode
        ↓
Security
        ↓
Performance
        ↓
Maintainability
```

For a private bank, this is particularly important because software correctness is not merely about whether an endpoint returns `200 OK`.

A financial system must also be:

```text
Correct
Secure
Auditable
Consistent
Reliable
Observable
Maintainable
```

That is the mindset to demonstrate during the interview.
