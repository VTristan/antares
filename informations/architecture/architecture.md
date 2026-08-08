# 3. Clean Code, Software Architecture & Domain-Driven Design

## 3.1. SOLID Principles & Software Craftsmanship

**SOLID** is a set of five principles that help developers design software that is easier to maintain, test, extend, and understand.

---

### S — Single Responsibility Principle (SRP)

> **A class should have only one reason to change.**

A class should have **one well-defined responsibility**.

For example, this class violates SRP:

```java
public class OrderService {

    public void createOrder(Order order) {
        // Business logic
    }

    public void saveToDatabase(Order order) {
        // Database logic
    }

    public void sendConfirmationEmail(Order order) {
        // Email logic
    }
}
```

This class has several reasons to change:

```text
OrderService
     │
     ├── Business rules
     ├── Database
     └── Email
```

A better design separates these responsibilities:

```text
OrderService
     │
     └── Business logic

OrderRepository
     │
     └── Database access

EmailService
     │
     └── Email communication
```

The important point is that **SRP is about reasons to change**, not simply about having one method or one task.

---

### O — Open/Closed Principle (OCP)

> **Software entities should be open for extension but closed for modification.**

The idea is that we should be able to add new behavior without constantly modifying existing, stable code.

A common way to achieve this is through **interfaces and polymorphism**.

For example:

```java
public interface PaymentMethod {

    void pay(BigDecimal amount);
}
```

We can then have:

```java
public class CreditCardPayment implements PaymentMethod {

    @Override
    public void pay(BigDecimal amount) {
        // Credit card payment
    }
}
```

and:

```java
public class PayPalPayment implements PaymentMethod {

    @Override
    public void pay(BigDecimal amount) {
        // PayPal payment
    }
}
```

The service depends on the abstraction:

```java
public class PaymentService {

    public void process(
            PaymentMethod paymentMethod,
            BigDecimal amount) {

        paymentMethod.pay(amount);
    }
}
```

Adding a new payment method:

```java
public class CryptoPayment implements PaymentMethod {

    @Override
    public void pay(BigDecimal amount) {
        // Cryptocurrency payment
    }
}
```

does not require modifying `PaymentService`.

This is the essence of OCP:

```text
Existing code
     │
     ├── CreditCard
     ├── PayPal
     │
     └── remains unchanged

New behavior
     │
     └── CryptoPayment
```

---

### L — Liskov Substitution Principle (LSP)

> **Objects of a subclass should be replaceable with objects of their superclass without breaking the correctness of the program.**

In simpler terms:

> **If `B` is a subtype of `A`, code expecting `A` should be able to work correctly with `B`.**

For example:

```java
public interface PaymentMethod {

    void pay(BigDecimal amount);
}
```

Both implementations should respect the contract:

```java
CreditCardPayment implements PaymentMethod
PayPalPayment implements PaymentMethod
```

If the application expects:

```java
PaymentMethod payment = ...;

payment.pay(amount);
```

every valid implementation should behave consistently with the contract.

---

### A Classic LSP Violation

Consider:

```java
public class Bird {

    public void fly() {
        // Fly
    }
}
```

Then:

```java
public class Penguin extends Bird {

    @Override
    public void fly() {
        throw new UnsupportedOperationException();
    }
}
```

Now:

```java
Bird bird = new Penguin();

bird.fly();
```

breaks the expectations established by the `Bird` abstraction.

The problem is not simply inheritance.

The problem is that:

> **The subtype does not honor the behavioral contract of the parent type.**

A better design could be:

```java
public interface Bird {
}
```

and:

```java
public interface FlyingBird extends Bird {

    void fly();
}
```

Then:

```text
Bird
 │
 ├── Penguin
 │
 └── FlyingBird
       │
       ├── Eagle
       └── Sparrow
```

---

### I — Interface Segregation Principle (ISP)

> **Clients should not be forced to depend on methods they do not use.**

In other words:

> **Prefer several small, focused interfaces over one large "fat interface".**

Bad example:

```java
public interface Worker {

    void work();

    void eat();

    void sleep();
}
```

A robot may need to work but does not need to eat or sleep.

```java
public class Robot implements Worker {

    @Override
    public void work() {
    }

    @Override
    public void eat() {
        // Makes no sense
    }

    @Override
    public void sleep() {
        // Makes no sense
    }
}
```

A better design separates the interfaces:

```java
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}
```

Now:

```java
public class Robot implements Workable {

    @Override
    public void work() {
    }
}
```

And:

```java
public class Human
        implements Workable, Eatable, Sleepable {

    // ...
}
```

This keeps interfaces focused on specific capabilities.

---

### D — Dependency Inversion Principle (DIP)

> **High-level modules should not depend directly on low-level modules. Both should depend on abstractions.**

And:

> **Abstractions should not depend on details. Details should depend on abstractions.**

Consider:

```java
public class OrderService {

    private final MySqlOrderRepository repository =
            new MySqlOrderRepository();
}
```

`OrderService` is directly coupled to MySQL.

A better design is:

```java
public interface OrderRepository {

    void save(Order order);
}
```

Then:

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

And the concrete implementation:

```java
public class MySqlOrderRepository
        implements OrderRepository {

    @Override
    public void save(Order order) {
        // MySQL implementation
    }
}
```

Now:

```text
              OrderService
                   │
                   ▼
            OrderRepository
              (interface)
                   ▲
                   │
        ┌──────────┴──────────┐
        │                     │
 MySqlOrderRepository   InMemoryRepository
```

The business logic does not care about the database implementation.

This principle is particularly important in **Hexagonal Architecture**.

---

# 3.2. Hexagonal Architecture — Ports & Adapters

**Hexagonal Architecture**, also known as **Ports and Adapters**, aims to isolate the application's core business logic from technical infrastructure.

The central idea is:

> **The business domain should not depend on technical details such as Spring, JPA, databases, HTTP, or Kafka.**

Conceptually:

```text
                ┌─────────────────────────┐
                │     Driving Adapters    │
                │                         │
                │ REST Controller         │
                │ Kafka Consumer           │
                │ CLI                      │
                └────────────┬────────────┘
                             │
                       Driving Ports
                             │
                             ▼
                 ┌──────────────────────┐
                 │                      │
                 │    DOMAIN CORE       │
                 │                      │
                 │ Entities             │
                 │ Value Objects        │
                 │ Business Rules       │
                 │ Use Cases             │
                 │                      │
                 └──────────────────────┘
                             ▲
                       Driven Ports
                             │
                ┌────────────┴────────────┐
                │                         │
        Driven Adapters            Driven Adapters
                │                         │
          PostgreSQL                 Stripe API
          JPA Repository             HTTP Client
```

The domain is at the center.

---

## Domain Core

The **Domain Core** contains the business logic.

It typically contains:

* Entities
* Value Objects
* Domain Services
* Domain rules
* Use Cases / Application Services

The core should ideally have no dependency on:

```text
Spring
JPA
Hibernate
Kafka
REST
PostgreSQL
AWS
```

For example:

```java
public class Order {

    private final OrderId id;
    private final Money total;

    public void confirm() {
        // Business rule
    }
}
```

The domain should not need:

```java
@Entity
@Service
@Repository
@RestController
```

to express its business rules.

---

# Driving Ports

**Driving ports** define how the outside world can ask the application to perform an operation.

They are often represented by **use-case interfaces**.

For example:

```java
public interface CreateOrderUseCase {

    OrderId createOrder(CreateOrderCommand command);
}
```

The interface belongs to the application core.

It says:

> "The application supports the use case of creating an order."

It does not care whether the request comes from:

* REST
* Kafka
* CLI
* Another application

---

# Driving Adapters

A **driving adapter** receives an external request and calls a driving port.

For example, a REST controller:

```java
@RestController
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping("/orders")
    public OrderId createOrder(
            @RequestBody CreateOrderRequest request) {

        return createOrderUseCase.createOrder(
                request.toCommand()
        );
    }
}
```

The controller is therefore an **adapter**.

It adapts:

```text
HTTP request
     │
     ▼
REST Controller
     │
     ▼
CreateOrderUseCase
     │
     ▼
Application
```

Other driving adapters could be:

```text
REST Controller
Kafka Consumer
CLI
GraphQL Resolver
Scheduled Job
```

---

# Driven Ports

A **driven port** defines something the application needs from the outside world.

For example:

```java
public interface OrderRepositoryPort {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
```

The domain/application layer says:

> "I need something capable of saving and retrieving orders."

It does not specify how this is done.

Another example:

```java
public interface PaymentGatewayPort {

    PaymentResult charge(
            Money amount,
            PaymentMethod paymentMethod);
}
```

The application does not know whether payment is processed through:

```text
Stripe
PayPal
Adyen
A bank API
A mock
```

---

# Driven Adapters

A **driven adapter** provides the concrete implementation of a driven port.

For example:

```java
@Component
public class JpaOrderRepository
        implements OrderRepositoryPort {

    @Override
    public Optional<Order> findById(OrderId id) {
        // JPA implementation
    }

    @Override
    public void save(Order order) {
        // JPA implementation
    }
}
```

Or:

```java
@Component
public class StripePaymentAdapter
        implements PaymentGatewayPort {

    @Override
    public PaymentResult charge(
            Money amount,
            PaymentMethod paymentMethod) {

        // Stripe API call
    }
}
```

The architecture therefore looks like:

```text
                    APPLICATION
                         │
             ┌───────────┴───────────┐
             │                       │
       Driving Ports           Driven Ports
             ▲                       ▲
             │                       │
       Driving Adapters        Driven Adapters
             │                       │
             ▼                       ▼
          REST API              PostgreSQL
          Kafka                 Stripe
          CLI                   External APIs
```

---

# 3.3. Domain-Driven Design — DDD Fundamentals

**Domain-Driven Design (DDD)** is an approach to designing software around the **business domain** and its rules.

The central idea is:

> **The software model should reflect the business domain and the way domain experts understand it.**

DDD is particularly useful for complex business applications.

---

# Ubiquitous Language

**Ubiquitous Language** is a shared vocabulary used by:

* Developers
* Business experts
* Product owners
* Domain experts
* Documentation
* Code

For example, suppose we are building a banking application.

The business uses terms such as:

```text
Account
Transaction
Beneficiary
Transfer
Settlement
Available Balance
```

These exact concepts should appear consistently in the code.

Instead of:

```java
processThing()
```

we should prefer:

```java
settleTransfer()
```

if "settlement" is the actual business concept.

The goal is to reduce the gap between:

```text
Business language
       ↕
Software model
```

---

# Bounded Context

A **Bounded Context** is an explicit boundary within which a particular domain model and vocabulary apply.

This is one of the most important concepts in DDD.

Imagine a large e-commerce company:

```text
                 E-Commerce System
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
       ▼                 ▼                 ▼
    Catalog           Ordering          Shipping
    Context            Context           Context
```

The word **"Product"** might mean different things in different contexts.

### Catalog Context

```text
Product
 ├── name
 ├── description
 ├── category
 └── price
```

### Shipping Context

The same real-world product might be represented as:

```text
ShippableItem
 ├── weight
 ├── dimensions
 └── warehouseLocation
```

The models are different because the business concerns are different.

A Bounded Context therefore prevents one enormous, universal domain model from being shared everywhere.

---

# Entity vs Value Object

DDD makes an important distinction between **Entities** and **Value Objects**.

---

## Entity

An **Entity** is defined primarily by its **identity**, rather than by its attributes.

For example:

```java
public class Customer {

    private final CustomerId id;

    private String name;
}
```

Even if the customer's name changes:

```text
Customer #123
Name: John
      ↓
Name: Robert
```

it is still the same customer.

The identity remains:

```text
CustomerId = 123
```

Therefore:

> **An Entity has a stable identity over time.**

---

## Value Object

A **Value Object** has no independent identity.

It is defined entirely by its values.

For example:

```java
public record Money(
        BigDecimal amount,
        Currency currency
) {}
```

Consider:

```text
Money(100, EUR)
```

The important thing is the value:

```text
amount = 100
currency = EUR
```

There is no need for:

```text
MoneyId = 123
```

Two instances with the same values are conceptually equal:

```text
Money(100, EUR)
        =
Money(100, EUR)
```

Value Objects are generally designed to be **immutable**.

Other examples include:

```text
Money
Address
EmailAddress
Coordinates
DateRange
PhoneNumber
```

---

# Entity vs Value Object — Simple Example

Imagine a bank.

```text
Customer
    │
    │ Entity
    │
    ├── CustomerId = 123
    ├── Name = John
    │
    └── Address
          │
          │ Value Object
          │
          ├── Street
          ├── City
          └── ZipCode
```

The customer has an identity.

The address is simply a value describing where the customer lives.

If the customer moves:

```text
Customer #123
Address A
     │
     ▼
Address B
```

the customer is still the same entity.

---

# Aggregate

An **Aggregate** is a group of related domain objects that are treated as a single unit for consistency and transactional purposes.

For example:

```text
Order
 │
 ├── OrderLine
 │     ├── Product
 │     └── Quantity
 │
 ├── OrderLine
 │
 └── ShippingAddress
```

The aggregate defines a boundary around these objects.

---

# Aggregate Root

The **Aggregate Root** is the main entity through which the aggregate is accessed and modified.

For example:

```text
             Order
          Aggregate Root
               │
       ┌───────┼────────┐
       │       │        │
       ▼       ▼        ▼
   OrderLine OrderLine Address
```

External code should interact with the aggregate through the root:

```java
order.addLine(product, quantity);
order.confirm();
order.cancel();
```

Rather than directly modifying internal objects:

```java
order.getLines().get(0).setQuantity(42);
```

The aggregate root protects the **business invariants**.

---

# Why Does the Aggregate Root Matter?

Suppose an order has a business rule:

> **An order cannot be modified after it has been confirmed.**

We want the aggregate to protect this rule:

```java
public void addLine(Product product, int quantity) {

    if (status == OrderStatus.CONFIRMED) {
        throw new IllegalStateException(
                "A confirmed order cannot be modified"
        );
    }

    lines.add(new OrderLine(product, quantity));
}
```

The business rule lives inside the aggregate.

External code should not be able to bypass it.

Conceptually:

```text
Outside World
      │
      ▼
   Order
     │
     ├── Business rules
     ├── Invariants
     ├── OrderLines
     └── State
```

The aggregate root acts as a **consistency boundary**.

---

# The Relationship Between SOLID, Hexagonal Architecture and DDD

These concepts complement each other.

```text
                    DDD
                     │
                     ▼
             Business Domain
                     │
                     ▼
           Hexagonal Architecture
                     │
                     ▼
        Isolate the Domain from
        Technical Infrastructure
                     │
                     ▼
                   SOLID
                     │
                     ▼
        Design maintainable classes
        and dependencies
```

For example:

```text
                    REST
                     │
               Driving Adapter
                     │
                     ▼
             CreateOrderUseCase
                     │
                     ▼
              Order Aggregate
                     │
             ┌───────┴───────┐
             │               │
             ▼               ▼
       Domain Rules      Value Objects
             │
             ▼
       Driven Port
    OrderRepositoryPort
             │
             ▼
       Driven Adapter
      JpaOrderRepository
             │
             ▼
          Database
```

The resulting dependency direction is important:

```text
Infrastructure
      │
      ▼
Application / Domain
```

not:

```text
Domain
  │
  ▼
Spring / JPA / Database
```

The **business logic remains independent of infrastructure**.

---

# Key Interview Answer

If an interviewer asks:

> **"Can you explain the relationship between SOLID, Hexagonal Architecture and DDD?"**

A strong answer would be:

> **"SOLID provides principles for designing maintainable and loosely coupled classes. Hexagonal Architecture applies similar dependency principles at the application architecture level by isolating the business core from infrastructure through ports and adapters. DDD focuses on modeling the business domain itself, using concepts such as Entities, Value Objects, Aggregates, Bounded Contexts and a Ubiquitous Language.**
>
> **In practice, these approaches complement each other. I can use DDD to model an Order aggregate, Hexagonal Architecture to keep that domain model independent from Spring, JPA and external APIs, and SOLID principles to keep the individual classes and dependencies clean and maintainable."**

---

# The Big Picture

The easiest way to remember the three subjects is:

```text
SOLID
"What makes my code maintainable?"
        │
        ▼
Hexagonal Architecture
"How do I protect my business logic
from technical infrastructure?"
        │
        ▼
DDD
"How do I model the business domain correctly?"
```

Or even more simply:

| Concept                    | Main Question                                        |
| -------------------------- | ---------------------------------------------------- |
| **SOLID**                  | How should I design my classes and dependencies?     |
| **Hexagonal Architecture** | How do I isolate business logic from infrastructure? |
| **DDD**                    | How should I model complex business concepts?        |

The common objective is:

> **Keep business rules explicit, isolated, understandable, and independent from technical details.**
