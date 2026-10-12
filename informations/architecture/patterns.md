# Design Patterns & Software Design — Java / Spring Boot

---

# 1. Design Patterns — What You Must Know

A design pattern is a **reusable solution to a recurring software design problem**.

It is not a library, framework, or piece of code that you copy verbatim.

The classic patterns are usually divided into:

```text
Design Patterns
│
├── Creational
│   ├── Factory Method
│   ├── Abstract Factory
│   ├── Builder
│   ├── Prototype
│   └── Singleton
│
├── Structural
│   ├── Adapter
│   ├── Bridge
│   ├── Composite
│   ├── Decorator
│   ├── Facade
│   ├── Flyweight
│   └── Proxy
│
└── Behavioral
    ├── Chain of Responsibility
    ├── Command
    ├── Iterator
    ├── Mediator
    ├── Memento
    ├── Observer
    ├── State
    ├── Strategy
    ├── Template Method
    └── Visitor
```

For a modern Java/Spring interview, you should **not** memorize all 23 equally.

The highest-value patterns are:

```text
★★★★★ Strategy
★★★★★ Factory
★★★★★ Builder
★★★★★ Adapter
★★★★★ Decorator
★★★★★ Facade
★★★★★ Proxy
★★★★★ Observer
★★★★★ Chain of Responsibility
★★★★★ Template Method
★★★★☆ Command
★★★★☆ State
★★★☆☆ Composite
★★★☆☆ Specification
★★★☆☆ Repository
★★★☆☆ Dependency Injection
```

---

# 2. Strategy Pattern

## Problem

You have several algorithms that perform the same conceptual operation.

For example:

```text
Payment
├── BankTransfer
├── CreditCard
├── Crypto
└── Cash
```

Instead of:

```java
if (type == CARD) {
    // ...
} else if (type == TRANSFER) {
    // ...
} else if (type == CASH) {
    // ...
}
```

you encapsulate each algorithm.

## Example

```java
public interface PaymentStrategy {

    void pay(BigDecimal amount);
}
```

```java
public class CardPayment implements PaymentStrategy {

    @Override
    public void pay(BigDecimal amount) {
        System.out.println("Paying by card: " + amount);
    }
}
```

```java
public class BankTransferPayment implements PaymentStrategy {

    @Override
    public void pay(BigDecimal amount) {
        System.out.println("Paying by bank transfer: " + amount);
    }
}
```

Then:

```java
public class PaymentService {

    private final PaymentStrategy strategy;

    public PaymentService(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void pay(BigDecimal amount) {
        strategy.pay(amount);
    }
}
```

### Main advantage

You can add:

```java
CryptoPayment
```

without modifying the existing payment algorithm.

This relates directly to:

> **Open/Closed Principle**

---

# 3. Strategy + Spring

This is particularly important for Spring interviews.

```java
public interface PaymentStrategy {

    PaymentType supports();

    void pay(BigDecimal amount);
}
```

Implementations:

```java
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentType supports() {
        return PaymentType.CARD;
    }

    @Override
    public void pay(BigDecimal amount) {
        // ...
    }
}
```

```java
@Component
public class BankTransferStrategy implements PaymentStrategy {

    @Override
    public PaymentType supports() {
        return PaymentType.BANK_TRANSFER;
    }

    @Override
    public void pay(BigDecimal amount) {
        // ...
    }
}
```

Spring can inject all implementations:

```java
@Service
public class PaymentService {

    private final Map<PaymentType, PaymentStrategy> strategies;

    public PaymentService(List<PaymentStrategy> strategies) {

        this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::supports,
                        Function.identity()
                ));
    }

    public void pay(PaymentType type, BigDecimal amount) {

        PaymentStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unsupported payment type: " + type
            );
        }

        strategy.pay(amount);
    }
}
```

This is an excellent example to discuss during an interview because it combines:

```text
Strategy
+
Dependency Injection
+
Polymorphism
+
Open/Closed Principle
+
Spring
```

---

# 4. Factory Pattern

## Problem

Object creation becomes complicated.

Instead of:

```java
if (type == A) {
    return new A();
}

if (type == B) {
    return new B();
}
```

you centralize creation.

```java
public interface Notification {

    void send(String message);
}
```

```java
public class EmailNotification implements Notification {

    @Override
    public void send(String message) {
        // ...
    }
}
```

```java
public class SmsNotification implements Notification {

    @Override
    public void send(String message) {
        // ...
    }
}
```

Factory:

```java
public class NotificationFactory {

    public static Notification create(NotificationType type) {

        return switch (type) {
            case EMAIL -> new EmailNotification();
            case SMS -> new SmsNotification();
        };
    }
}
```

---

# 5. Factory vs Strategy

This is a common interview question.

The difference is:

```text
Factory
    → decides WHICH object to create

Strategy
    → decides WHICH algorithm/behavior to use
```

Example:

```text
Factory
    "Which PaymentStrategy should I create?"

Strategy
    "Which payment algorithm should I execute?"
```

They are frequently used together.

---

# 6. Abstract Factory

Useful when you need to create **families of related objects**.

For example:

```text
EuropeanBankingFactory
    ├── createAccount()
    ├── createTransfer()
    └── createStatement()

SwissBankingFactory
    ├── createAccount()
    ├── createTransfer()
    └── createStatement()
```

The important idea is:

> Factory → creates one type of object.

> Abstract Factory → creates a family of related objects.

---

# 7. Builder Pattern

Useful when an object has many optional parameters.

Without Builder:

```java
new Client(
    "John",
    "Smith",
    "john@example.com",
    null,
    null,
    "Geneva",
    true,
    false
);
```

This becomes difficult to read.

Builder:

```java
Client client = Client.builder()
        .firstName("John")
        .lastName("Smith")
        .email("john@example.com")
        .city("Geneva")
        .active(true)
        .build();
```

Modern Java applications use this pattern extensively.

For example, Lombok:

```java
@Builder
public class Client {
    private String firstName;
    private String lastName;
    private String email;
}
```

---

# 8. Immutable Objects + Builder

Builder is particularly useful for immutable objects.

```java
public final class Client {

    private final String name;
    private final String email;

    private Client(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private String email;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Client build() {
            return new Client(this);
        }
    }
}
```

---

# 9. Adapter Pattern

## Problem

Two interfaces don't match.

Imagine your application expects:

```java
interface PaymentProvider {

    void transfer(BigDecimal amount);
}
```

But an external library provides:

```java
class LegacyPaymentApi {

    void makeTransaction(double amount) {
        // ...
    }
}
```

Adapter:

```java
public class PaymentAdapter implements PaymentProvider {

    private final LegacyPaymentApi api;

    public PaymentAdapter(LegacyPaymentApi api) {
        this.api = api;
    }

    @Override
    public void transfer(BigDecimal amount) {
        api.makeTransaction(amount.doubleValue());
    }
}
```

Your application now sees:

```text
PaymentProvider
       ↓
PaymentAdapter
       ↓
LegacyPaymentApi
```

This is extremely common in enterprise applications.

---

# 10. Adapter vs Facade

Another common interview question.

### Adapter

Changes an **interface**.

```text
Interface A
     ↓
 Adapter
     ↓
Interface B
```

### Facade

Simplifies a **complex subsystem**.

```text
Client
  ↓
Facade
  ↓
 ┌───────────────┐
 │ Service A     │
 │ Service B     │
 │ Service C     │
 │ Repository    │
 └───────────────┘
```

---

# 11. Facade Pattern

Suppose transferring money requires:

```text
validate account
      ↓
check balance
      ↓
check fraud
      ↓
reserve funds
      ↓
execute transfer
      ↓
write audit
```

Instead of exposing everything:

```java
fraudService.check();
accountService.validate();
balanceService.check();
reservationService.reserve();
transferService.execute();
auditService.record();
```

you provide:

```java
transferFacade.transfer(request);
```

The facade hides subsystem complexity.

---

# 12. Decorator Pattern

Decorator dynamically adds behavior.

Example:

```text
Payment
   ↓
LoggingPayment
   ↓
MetricsPayment
   ↓
RetryPayment
   ↓
RealPayment
```

Interface:

```java
public interface Payment {

    void execute();
}
```

Decorator:

```java
public class LoggingPayment implements Payment {

    private final Payment delegate;

    public LoggingPayment(Payment delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute() {
        System.out.println("Starting payment");

        delegate.execute();

        System.out.println("Payment completed");
    }
}
```

You can compose:

```java
Payment payment =
        new LoggingPayment(
            new MetricsPayment(
                new RealPayment()
            )
        );
```

---

# 13. Decorator vs Inheritance

Inheritance:

```text
Payment
  ↑
LoggingPayment
```

Decorator:

```text
LoggingPayment
      ↓
    Payment
      ↓
 RealPayment
```

Decorator favors **composition over inheritance**.

This is a very important design principle.

---

# 14. Proxy Pattern

A proxy controls access to another object.

```text
Client
  ↓
Proxy
  ↓
Real Object
```

Typical use cases:

```text
Security
Logging
Caching
Transactions
Lazy loading
Remote calls
```

And this is extremely relevant to Spring.

For example:

```java
@Transactional
public void transfer() {
    // ...
}
```

Spring can create a proxy around your bean:

```text
Client
  ↓
Spring Proxy
  ↓
Transaction handling
  ↓
Your Service
```

This is one of the most important connections between **design patterns and Spring internals**.

---

# 15. Observer Pattern

One object changes state and notifies observers.

```text
                ┌── Observer A
Subject ────────┼── Observer B
                └── Observer C
```

Example:

```java
public interface EventListener {

    void onEvent(Event event);
}
```

Then:

```java
public class AuditListener implements EventListener {
    // ...
}
```

```java
public class NotificationListener implements EventListener {
    // ...
}
```

Spring provides event mechanisms:

```java
applicationEventPublisher.publishEvent(
        new TransferCompletedEvent(transferId)
);
```

Listeners can react independently.

---

# 16. Chain of Responsibility

A request passes through a chain of handlers.

```text
Request
   ↓
Authentication
   ↓
Authorization
   ↓
Validation
   ↓
Fraud Check
   ↓
Processing
```

Each handler decides whether to:

```text
handle
or
pass to next
```

This pattern appears heavily in:

* HTTP filters
* Spring Security
* servlet filters
* validation pipelines
* processing pipelines

---

# 17. Template Method

Defines the algorithm's skeleton while allowing subclasses to customize specific steps.

```java
public abstract class PaymentProcessor {

    public final void process() {
        validate();
        authenticate();
        execute();
        audit();
    }

    protected abstract void execute();

    protected void validate() {
        // default
    }

    protected void authenticate() {
        // default
    }

    protected void audit() {
        // default
    }
}
```

The algorithm is fixed:

```text
validate
   ↓
authenticate
   ↓
execute
   ↓
audit
```

but subclasses customize `execute()`.

---

# 18. Strategy vs Template Method

Important interview question.

### Strategy

Uses **composition**:

```text
Service
   ↓
Strategy
```

Behavior is injected.

### Template Method

Uses **inheritance**:

```text
Base class
    ↑
Subclass
```

Behavior is overridden.

Generally:

> Strategy gives more flexibility because behavior can be composed at runtime.

---

# 19. State Pattern

State allows an object to change behavior depending on its current state.

For a bank transfer:

```text
CREATED
   ↓
VALIDATED
   ↓
PROCESSING
   ↓
COMPLETED
```

Potentially:

```text
FAILED
CANCELLED
```

Instead of:

```java
if (state == CREATED) {
   ...
} else if (state == PROCESSING) {
   ...
}
```

each state encapsulates its behavior.

This becomes particularly useful when state-dependent behavior becomes complex.

---

# 20. Command Pattern

Encapsulates an action as an object.

```java
public interface Command {

    void execute();
}
```

Example:

```java
public class TransferCommand implements Command {

    private final TransferService service;
    private final Transfer transfer;

    @Override
    public void execute() {
        service.transfer(transfer);
    }
}
```

Useful for:

```text
Undo/redo
Queues
Job execution
Scheduling
Auditing
Transactions
Commands in CQRS
```

---

# 21. Specification Pattern

Very useful in enterprise applications.

Instead of:

```java
if (client.isActive()
        && client.getAge() >= 18
        && client.getCountry().equals("CH")) {
    ...
}
```

you compose specifications:

```text
ActiveClient
    AND
AdultClient
    AND
SwissClient
```

Conceptually:

```java
Specification<Client> active =
        client -> client.isActive();

Specification<Client> adult =
        client -> client.getAge() >= 18;

Specification<Client> swiss =
        client -> client.getCountry().equals("CH");
```

Then:

```java
Specification<Client> eligible =
        active.and(adult).and(swiss);
```

This pattern is particularly useful for complex business rules.

---

# 22. Repository Pattern

Very important for Spring interviews.

The repository abstracts data access.

```text
Business Logic
      ↓
Repository
      ↓
Database
```

Example:

```java
public interface ClientRepository {

    Optional<Client> findById(Long id);

    void save(Client client);
}
```

The service does not need to know:

```text
SQL
JDBC
JPA
Hibernate
MongoDB
```

---

# 23. Dependency Injection

Dependency Injection is arguably more important in Spring than many GoF patterns.

Instead of:

```java
public class TransferService {

    private final TransferRepository repository =
            new JpaTransferRepository();
}
```

you inject the dependency:

```java
public class TransferService {

    private final TransferRepository repository;

    public TransferService(TransferRepository repository) {
        this.repository = repository;
    }
}
```

Spring supplies it.

This enables:

```text
Loose coupling
Testability
Dependency inversion
Configuration flexibility
```

---

# 24. Dependency Inversion Principle

The high-level component should not depend directly on implementation details.

Bad:

```text
TransferService
      ↓
JpaTransferRepository
```

Better:

```text
TransferService
      ↓
TransferRepository
      ↑
JpaTransferRepository
```

The abstraction is owned by the business/application layer.

This connects directly to:

> **SOLID**

---

# 25. SOLID — Interview Essentials

## S — Single Responsibility

A class should have **one reason to change**.

Bad:

```java
ClientService
    ├── validate()
    ├── save()
    ├── sendEmail()
    ├── generatePdf()
    └── writeAudit()
```

Better:

```text
ClientValidator
ClientRepository
EmailService
PdfGenerator
AuditService
```

---

## O — Open/Closed

Open for extension.

Closed for modification.

Prefer:

```java
interface PaymentStrategy
```

over continually modifying:

```java
PaymentService
```

with more `if/else`.

---

## L — Liskov Substitution

If:

```text
B extends A
```

then B should be usable wherever A is expected **without breaking the expected behavior**.

Classic bad example:

```java
class Bird {
    void fly() {}
}

class Penguin extends Bird {
    @Override
    void fly() {
        throw new UnsupportedOperationException();
    }
}
```

The abstraction is wrong.

Better:

```text
Bird
├── FlyingBird
│   ├── Eagle
│   └── Sparrow
│
└── Penguin
```

---

## I — Interface Segregation

Don't force classes to implement methods they don't need.

Bad:

```java
interface Employee {
    void work();
    void eat();
    void manage();
}
```

A simple worker may not need `manage()`.

Prefer smaller interfaces:

```java
interface Worker {
    void work();
}

interface Manager {
    void manage();
}
```

---

## D — Dependency Inversion

Depend on abstractions rather than concrete implementations.

```java
private final PaymentProvider provider;
```

rather than:

```java
private final StripePaymentProvider provider;
```

---

# 26. Composition Over Inheritance

This is one of the most important concepts to understand beyond individual patterns.

Inheritance:

```text
A
↑
B
```

Composition:

```text
B
↓
A
```

Instead of:

```java
class PremiumPaymentService extends PaymentService
```

you might use:

```java
class PaymentService {

    private final PaymentValidator validator;
    private final PaymentProcessor processor;
}
```

Composition generally provides:

```text
Lower coupling
Greater flexibility
Easier testing
Runtime configuration
```

Patterns such as:

```text
Strategy
Decorator
Adapter
Composite
```

make extensive use of composition.

---

# 27. Coupling vs Cohesion

### Coupling

How strongly components depend on each other.

You generally want:

```text
LOW COUPLING
```

### Cohesion

How closely related the responsibilities inside a component are.

You generally want:

```text
HIGH COHESION
```

Ideal:

```text
High cohesion
+
Low coupling
```

---

# 28. Anemic Domain Model vs Rich Domain Model

### Anemic

```java
public class Account {

    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
```

Business logic lives elsewhere:

```java
accountService.withdraw(account, amount);
```

### Rich domain model

```java
public class Account {

    private BigDecimal balance;

    public void withdraw(BigDecimal amount) {

        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException();
        }

        balance = balance.subtract(amount);
    }
}
```

The domain object protects its invariants.

---

# 29. DTO vs Entity vs Domain Model

A strong interview answer should distinguish them.

```text
DTO
↓
Data transferred between boundaries

Entity
↓
Persistence representation

Domain Model
↓
Business concepts + rules
```

Do not automatically expose JPA entities through REST APIs.

Instead:

```text
HTTP
 ↓
Request DTO
 ↓
Service
 ↓
Domain
 ↓
Repository
 ↓
Entity
```

and:

```text
Entity
 ↓
Domain
 ↓
Response DTO
 ↓
HTTP
```

---

# 30. Layered Architecture

Classic Spring application:

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

```text
Simple
Familiar
Easy to understand
```

Potential problem:

```text
Business logic becomes coupled
to infrastructure.
```

---

# 31. Hexagonal Architecture

Also called:

> Ports and Adapters Architecture

Core:

```text
              ┌───────────────┐
              │   Domain      │
              │               │
              │ Business      │
              │ Logic         │
              └───────────────┘
                 ↑         ↑
              Ports       Ports
                 ↑         ↑
          ┌──────┘         └──────┐
          │                       │
      REST API                 Database
      Adapter                  Adapter
```

The domain doesn't know about:

```text
Spring
JPA
PostgreSQL
Kafka
REST
AWS
```

Those are external details.

---

# 32. Clean Architecture

Conceptually:

```text
        Frameworks
             ↓
      Interface Adapters
             ↓
       Application
             ↓
          Domain
```

The dependency direction points inward.

The central business logic should not depend on infrastructure.

---

# 33. Hexagonal vs Clean Architecture

They are very similar philosophically.

Both emphasize:

```text
Business logic
       ↑
Infrastructure
```

The vocabulary differs.

### Hexagonal

Focuses on:

```text
Ports
Adapters
```

### Clean Architecture

Focuses on:

```text
Entities
Use Cases
Interface Adapters
Frameworks
```

---

# 34. CQRS

Command Query Responsibility Segregation.

Instead of one model doing everything:

```text
                 Application
                     ↓
              ┌──────┴──────┐
              ↓             ↓
           Commands       Queries
              ↓             ↓
           Write DB       Read DB
```

Commands modify state.

Queries read state.

Important:

> CQRS does **not necessarily require** separate databases.

---

# 35. Event-Driven Architecture

Instead of:

```text
Service A
   ↓
Service B
   ↓
Service C
```

you can have:

```text
Service A
   ↓
Event
   ↓
Message Broker
   ├── Service B
   ├── Service C
   └── Service D
```

Examples:

```text
Kafka
RabbitMQ
AWS SNS/SQS
```

Typical events:

```text
ClientCreated
TransferCompleted
PaymentRejected
AccountClosed
```

---

# 36. Event vs Command

Important distinction.

### Command

An instruction:

```text
ProcessTransfer
```

Usually:

```text
imperative
```

### Event

A statement about something that already happened:

```text
TransferProcessed
```

Usually:

```text
past tense
```

---

# 37. Idempotency

Extremely important in banking.

An operation is idempotent if executing it multiple times has the same effect as executing it once.

For example:

```text
POST /transfers
```

Network failure occurs.

Client doesn't know whether the transfer succeeded.

Client retries.

Without idempotency:

```text
€1,000
+
€1,000
=
€2,000 transferred
```

With an idempotency key:

```text
Idempotency-Key: abc123
```

the server recognizes the duplicate request.

This is a very strong topic to discuss for a private bank.

---

# 38. Transactional Outbox Pattern

A common distributed-system problem:

```text
Database transaction
       +
Kafka event
```

What happens if:

```text
DB commit succeeds
Kafka publish fails
```

You have inconsistent state.

The Outbox pattern solves this by writing the event to the same database transaction:

```text
Transaction
   │
   ├── Update business data
   │
   └── Insert event into OUTBOX
```

Then:

```text
Outbox
  ↓
Publisher
  ↓
Kafka
```

This gives much stronger reliability.

---

# 39. Saga Pattern

Useful for distributed transactions.

Example:

```text
Create Transfer
      ↓
Reserve Funds
      ↓
Execute Transfer
      ↓
Update Account
```

If something fails, compensating actions are executed.

```text
Reserve Funds
      ↓
Execute Transfer FAILED
      ↓
Release Funds
```

Two common approaches:

```text
Choreography
Orchestration
```

---

# 40. Anti-Corruption Layer

Very useful when integrating legacy systems.

```text
Modern Application
       ↓
Anti-Corruption Layer
       ↓
Legacy System
```

The ACL prevents legacy concepts from contaminating the modern domain model.

This is especially relevant in banking because large institutions often have:

```text
legacy systems
+
new systems
+
external providers
+
multiple protocols
```

---

# 41. Interview Questions You Should Be Able to Answer

### Basic

**What is a design pattern?**

> A reusable design solution to a recurring software design problem.

**What are the three main categories?**

> Creational, structural and behavioral.

**What is Strategy?**

> It encapsulates interchangeable algorithms behind a common interface.

**What is Factory?**

> It centralizes and abstracts object creation.

**What is Builder?**

> It provides a readable way to construct complex objects, particularly when many parameters are optional.

---

### Intermediate

**Strategy vs Factory?**

> Factory handles object creation; Strategy handles interchangeable behavior.

**Adapter vs Facade?**

> Adapter changes an interface; Facade simplifies a subsystem.

**Decorator vs Proxy?**

> Both wrap another object, but Decorator primarily adds behavior while Proxy controls access to the underlying object.

**Composition vs inheritance?**

> Composition builds behavior by combining objects; inheritance derives behavior through class hierarchies. Composition generally produces lower coupling and greater flexibility.

---

### Senior

**Why can too many design patterns be harmful?**

Because patterns introduce abstractions and complexity.

A simple:

```java
if (...)
```

may be better than creating:

```text
Factory
Strategy
FactoryProvider
StrategyResolver
AbstractStrategyFactory
```

for two trivial cases.

The pattern should solve a real design problem.

---

# 42. The Most Important Senior-Level Question

A strong interviewer may ask:

> **"How do you decide which design pattern to use?"**

A good answer:

```text
1. Identify the design problem.
2. Identify what is likely to change.
3. Identify the dependencies.
4. Look for excessive coupling.
5. Look for duplicated behavior.
6. Choose the simplest abstraction that solves the problem.
7. Avoid introducing a pattern merely because it exists.
8. Consider maintainability and testability.
```

The key principle is:

> **Don't start with a design pattern. Start with the problem.**

---

# 43. Patterns You Should Recognize Inside Spring

This is particularly important for a **Spring Boot interview**.

| Spring feature                       | Pattern / concept                  |
| ------------------------------------ | ---------------------------------- |
| `@Autowired` / constructor injection | Dependency Injection               |
| `@Transactional`                     | Proxy                              |
| Spring AOP                           | Proxy                              |
| `ApplicationEventPublisher`          | Observer                           |
| `@EventListener`                     | Observer                           |
| `BeanFactory`                        | Factory                            |
| `FactoryBean`                        | Factory                            |
| `RestTemplate` / HTTP clients        | Facade-like abstraction            |
| Spring Security filters              | Chain of Responsibility            |
| `HandlerInterceptor`                 | Chain / interceptor                |
| `BeanPostProcessor`                  | Template/extension mechanism       |
| `JdbcTemplate`                       | Template Method / Template pattern |
| `RestClient`                         | Facade                             |
| Spring Data repositories             | Repository                         |
| `WebClient` filters                  | Chain / interceptor                |
| Spring caching                       | Proxy                              |
| Spring transactions                  | Proxy                              |
| Dependency injection                 | Dependency Inversion               |

---

# 44. A Banking Example Combining Everything

Imagine:

```text
POST /transfers
```

Architecture:

```text
                         REST
                          │
                          ▼
                    TransferController
                          │
                          ▼
                    TransferService
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
       TransferValidator       TransferStrategy
                                      │
                         ┌────────────┼────────────┐
                         ▼            ▼            ▼
                       SEPA         SWIFT       INTERNAL
                         │            │            │
                         └────────────┼────────────┘
                                      ▼
                              TransferRepository
                                      │
                                      ▼
                                  Database
```

Around this:

```text
Spring Proxy
     │
     ├── Transaction
     ├── Security
     └── Metrics
```

Events:

```text
Transfer completed
        ↓
TransferCompletedEvent
        ↓
       Kafka
        ├── Audit Service
        ├── Notification Service
        └── Reporting Service
```

Reliability:

```text
Idempotency Key
       +
Transactional Outbox
       +
Retry
       +
Monitoring
```

Architecture:

```text
Hexagonal / Clean Architecture
```

Principles:

```text
SOLID
+
Composition
+
Dependency Inversion
+
High Cohesion
+
Low Coupling
```

This is the level at which design patterns become useful: **not as isolated definitions, but as tools that solve concrete architectural problems.**
