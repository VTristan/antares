# 3.5. Java Types & Object-Oriented Design — When to Use What?

Java gives you many ways to model relationships between objects:

* `class`
* `interface`
* `abstract class`
* `final class`
* `sealed class`
* `non-sealed class`
* `record`
* inheritance (`extends`)
* interface implementation (`implements`)
* composition
* delegation
* enum

The important question is not:

> "What can Java do?"

It is:

> **"What relationship am I trying to express?"**

---

# 1. The Big Decision Tree

When creating a new type, start with this:

```text
Do I need to represent data?
        │
        ├── YES ──► Is it mainly a data carrier?
        │                │
        │                └── YES ──► record
        │
        └── NO
             │
             ▼
      Do I need polymorphism?
             │
       ┌─────┴─────┐
       │           │
      NO          YES
       │           │
       ▼           ▼
    class      interface / abstract class
                   │
                   ▼
           Do I control the hierarchy?
                   │
          ┌────────┴────────┐
          │                 │
         YES                NO
          │                 │
          ▼                 ▼
      sealed             interface
          │
          ▼
   Is there shared state/
   implementation?
          │
      ┌───┴───┐
     YES      NO
      │        │
      ▼        ▼
 abstract    interface
 class
```

This is only a starting point. The most important distinctions are below.

---

# 2. Concrete Class

A normal `class` represents a concrete concept that can be instantiated.

```java
public class Order {

    private final OrderId id;

    public Order(OrderId id) {
        this.id = id;
    }

    public void confirm() {
        // Business logic
    }
}
```

You use a concrete class when:

* You need objects with state.
* You need behavior.
* The class can be instantiated.
* You don't need a restricted hierarchy.
* You don't need multiple implementations of an abstraction.

Example:

```java
Order order = new Order(orderId);
```

A large majority of your classes will simply be normal classes.

---

# 3. Interface

An `interface` defines a **contract or capability**.

```java
public interface PaymentGateway {

    PaymentResult charge(Money amount);
}
```

Different implementations can provide different behaviors:

```java
public class StripePaymentGateway
        implements PaymentGateway {

    @Override
    public PaymentResult charge(Money amount) {
        // Stripe implementation
    }
}
```

```java
public class AdyenPaymentGateway
        implements PaymentGateway {

    @Override
    public PaymentResult charge(Money amount) {
        // Adyen implementation
    }
}
```

The caller depends on the abstraction:

```java
private final PaymentGateway paymentGateway;
```

rather than:

```java
private final StripePaymentGateway paymentGateway;
```

---

# 4. When Should I Use an Interface?

Use an interface when you want to express:

> **"Anything implementing this must respect this contract."**

Typical cases:

### Multiple implementations

```text
PaymentGateway
    │
    ├── StripePaymentGateway
    ├── AdyenPaymentGateway
    └── MockPaymentGateway
```

### Dependency inversion

```text
Application
     │
     ▼
PaymentGateway
     ▲
     │
StripePaymentGateway
```

### Capabilities

```java
public interface Exportable {

    byte[] export();
}
```

Several unrelated classes can implement it:

```java
Invoice implements Exportable
Report implements Exportable
UserList implements Exportable
```

They do not need to share a parent class.

---

# 5. Interface Does Not Mean "No Implementation"

Modern Java interfaces can contain:

```java
default
static
private
```

methods.

For example:

```java
public interface Auditable {

    void audit();

    default String source() {
        return "SYSTEM";
    }
}
```

However, interfaces are still primarily useful for **contracts and abstractions**, not for storing object state.

---

# 6. Abstract Class

An abstract class is a class that **cannot be instantiated directly**.

It can contain:

* State
* Constructors
* Concrete methods
* Abstract methods
* Protected methods
* Shared implementation

Example:

```java
public abstract class Payment {

    protected final Money amount;

    protected Payment(Money amount) {
        this.amount = amount;
    }

    public abstract void process();

    public void log() {
        System.out.println("Processing payment");
    }
}
```

Then:

```java
public class CreditCardPayment extends Payment {

    public CreditCardPayment(Money amount) {
        super(amount);
    }

    @Override
    public void process() {
        // Credit card logic
    }
}
```

---

# 7. Interface vs Abstract Class

This is a classic interview question.

|                                  | Interface                  | Abstract Class              |
| -------------------------------- | -------------------------- | --------------------------- |
| Main purpose                     | Contract / capability      | Shared base abstraction     |
| State                            | No instance fields         | Yes                         |
| Constructor                      | No                         | Yes                         |
| Multiple inheritance             | A class can implement many | A class can extend only one |
| Concrete methods                 | Yes                        | Yes                         |
| Abstract methods                 | Yes                        | Yes                         |
| Strong parent-child relationship | Not necessarily            | Usually                     |
| Shared implementation            | Possible, but limited      | Strong                      |
| `private` fields                 | No instance state          | Yes                         |

The easiest rule:

> **Use an interface when you want to define what an object can do.**

> **Use an abstract class when you want to provide a common foundation for closely related classes.**

---

# 8. A Useful Example

Imagine different types of vehicles.

An interface:

```java
public interface Electric {

    void charge();
}
```

An abstract class:

```java
public abstract class Vehicle {

    protected final String brand;

    protected Vehicle(String brand) {
        this.brand = brand;
    }

    public abstract void move();

    public void stop() {
        // Common implementation
    }
}
```

Now:

```java
public class ElectricCar
        extends Vehicle
        implements Electric {

    public ElectricCar(String brand) {
        super(brand);
    }

    @Override
    public void move() {
    }

    @Override
    public void charge() {
    }
}
```

This expresses two different relationships:

```text
Vehicle
   │
   └── ElectricCar

Electric
   ▲
   │
ElectricCar
```

`Vehicle` represents **what the object fundamentally is**.

`Electric` represents **a capability**.

---

# 9. `extends`

`extends` represents inheritance.

```java
public class Dog extends Animal {
}
```

Conceptually:

> **Dog is an Animal.**

This is an **is-a relationship**.

```text
Animal
   ▲
   │
  Dog
```

Use inheritance when the subtype genuinely satisfies the semantic contract of the parent.

This connects directly to the **Liskov Substitution Principle**.

If you cannot honestly say:

> "A `Dog` can be used anywhere an `Animal` is expected."

then inheritance may be the wrong design.

---

# 10. `implements`

`implements` means that a class agrees to satisfy an interface contract.

```java
public class StripePaymentGateway
        implements PaymentGateway {
}
```

Conceptually:

> **StripePaymentGateway provides the PaymentGateway capability.**

A class can implement several interfaces:

```java
public class SmartDevice
        implements Connectable, Rechargeable, Trackable {
}
```

This is one reason interfaces are useful for modeling independent capabilities.

---

# 11. Composition

Composition means an object **contains** another object rather than inheriting from it.

```java
public class OrderService {

    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

This expresses:

> **OrderService uses a PaymentGateway.**

Not:

> OrderService is a PaymentGateway.

---

# 12. Composition vs Inheritance

This is one of the most important design decisions.

Inheritance:

```java
class Car extends Vehicle
```

means:

```text
Car IS-A Vehicle
```

Composition:

```java
class Car {

    private Engine engine;
}
```

means:

```text
Car HAS-A Engine
```

Therefore:

```text
Inheritance → IS-A
Composition → HAS-A
```

---

# 13. Prefer Composition Over Inheritance

A common object-oriented design principle is:

> **Favor composition over inheritance.**

Inheritance creates strong coupling between parent and child.

For example:

```java
public class Car extends Vehicle {
}
```

`Car` is strongly coupled to the behavior and implementation of `Vehicle`.

With composition:

```java
public class Car {

    private final Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }
}
```

You can replace the engine without changing the inheritance hierarchy.

This is particularly useful with dependency injection:

```java
public class OrderService {

    private final PaymentGateway paymentGateway;
}
```

The service is composed with a payment implementation.

---

# 14. When Is Inheritance Appropriate?

Inheritance is appropriate when there is a genuine **type hierarchy** and the parent defines a meaningful contract.

Good example:

```text
Shape
 │
 ├── Circle
 ├── Rectangle
 └── Triangle
```

If:

```java
Shape shape = new Circle();
```

makes semantic sense, inheritance can be appropriate.

Another example:

```text
Exception
 │
 ├── IOException
 ├── SQLException
 └── RuntimeException
```

The hierarchy represents meaningful semantic relationships.

---

# 15. When Should I Avoid Inheritance?

Avoid inheritance when you're using it merely to:

* Reuse a few methods
* Share utility code
* Avoid duplication
* Access protected fields

For example, this can be problematic:

```java
class UserService extends LoggingService {
}
```

If `UserService` is not actually a `LoggingService`, inheritance is expressing the wrong relationship.

Prefer:

```java
class UserService {

    private final Logger logger;
}
```

or dependency injection.

---

# 16. `final class`

A `final` class cannot be extended.

```java
public final class Money {
}
```

Use `final` when you want to explicitly prevent inheritance.

Typical reasons:

* The class is designed not to be subclassed.
* You want to protect invariants.
* You want a stable API.
* You want to prevent unexpected overriding.

For example:

```java
public final class Money {

    private final BigDecimal amount;

    // ...
}
```

A `record` is implicitly `final`.

---

# 17. Sealed Classes

A `sealed` class or interface allows you to explicitly control **which classes can extend or implement it**.

Example:

```java
public sealed interface PaymentMethod
        permits CreditCard, PayPal, Crypto {
}
```

Only these types are allowed:

```java
public final class CreditCard
        implements PaymentMethod {
}

public final class PayPal
        implements PaymentMethod {
}

public non-sealed class Crypto
        implements PaymentMethod {
}
```

This creates a **closed hierarchy**.

---

# 18. Why Use a Sealed Type?

Use a sealed hierarchy when:

> **You know all valid variants and want the compiler to enforce that closed set.**

For example:

```text
PaymentResult
    │
    ├── Success
    ├── Declined
    └── Error
```

You might write:

```java
public sealed interface PaymentResult
        permits Success, Declined, Error {
}
```

Now the compiler knows the complete hierarchy.

This becomes especially powerful with pattern matching:

```java
return switch (result) {

    case Success s -> "Payment successful";

    case Declined d -> "Payment declined";

    case Error e -> "Payment error";
};
```

The compiler can detect whether you have handled all permitted variants.

---

# 19. Sealed vs Interface

An ordinary interface says:

> "Anyone can implement this interface."

A sealed interface says:

> **"Only these specific types can implement this interface."**

Compare:

```java
public interface PaymentResult {
}
```

versus:

```java
public sealed interface PaymentResult
        permits Success, Declined, Error {
}
```

Use:

```text
interface
    → open hierarchy

sealed interface
    → closed / controlled hierarchy
```

---

# 20. `non-sealed`

A subclass of a sealed type can be declared `non-sealed`.

Example:

```java
public sealed interface PaymentMethod
        permits Card, Crypto {
}
```

Then:

```java
public final class Card
        implements PaymentMethod {
}
```

But:

```java
public non-sealed class Crypto
        implements PaymentMethod {
}
```

Now other classes can extend `Crypto`:

```java
public class Bitcoin
        extends Crypto {
}
```

The hierarchy becomes:

```text
PaymentMethod
    │
    ├── Card
    │
    └── Crypto (non-sealed)
          │
          ├── Bitcoin
          └── Ethereum
```

---

# 21. `record`

A `record` is designed primarily for **immutable data carriers**.

Example:

```java
public record UserDto(
        String username,
        String email
) {}
```

Java automatically provides:

* Constructor
* Accessors
* `equals()`
* `hashCode()`
* `toString()`

Instead of writing:

```java
public final class UserDto {

    private final String username;
    private final String email;

    public UserDto(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    // equals()
    // hashCode()
    // toString()
}
```

you can simply use:

```java
public record UserDto(
        String username,
        String email
) {}
```

---

# 22. When Should I Use a Record?

Excellent use cases:

### DTOs

```java
public record CreateUserRequest(
        String username,
        String email
) {}
```

### API responses

```java
public record UserResponse(
        Long id,
        String username
) {}
```

### Value Objects

```java
public record CustomerId(Long value) {
}
```

### Small immutable data structures

```java
public record Point(
        double x,
        double y
) {}
```

---

# 23. When Should I NOT Use a Record?

A record is not simply:

> "A shorter class."

It is specifically designed around **data-oriented, immutable semantics**.

Avoid records when you need:

* Mutable state
* Complex lifecycle
* JPA entities in many common designs
* Identity-based domain behavior
* Inheritance from another class
* A class whose primary purpose is rich mutable behavior

For example:

```java
@Entity
public class Order {
    // Often better as a normal class
}
```

rather than automatically making everything a record.

Records can implement interfaces, but they cannot extend another class.

---

# 24. Record and Immutability

A record is **shallowly immutable**.

For example:

```java
public record Order(
        List<String> items
) {}
```

The reference to the list cannot be changed:

```java
order.items() = anotherList; // impossible
```

But the list itself may still be mutable:

```java
order.items().add("Something");
```

Therefore, if true immutability matters, you must also ensure the contained objects are immutable or use defensive copies.

---

# 25. Enum

An `enum` represents a **fixed set of constants**.

Example:

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
```

Use an enum when the set of values is known and finite.

Good examples:

```text
OrderStatus
PaymentStatus
Currency
UserRole
Environment
```

Enums can also contain behavior:

```java
public enum OrderStatus {

    PENDING,
    CONFIRMED,
    CANCELLED;

    public boolean canBeCancelled() {
        return this == PENDING;
    }
}
```

---

# 26. Interface vs Enum vs Sealed Hierarchy

Suppose you need to represent payment methods.

### Enum

Use if the variants are simply fixed values:

```java
public enum PaymentMethod {
    CREDIT_CARD,
    PAYPAL,
    BANK_TRANSFER
}
```

Good when:

> The variants have little or no distinct data/behavior.

---

### Sealed hierarchy

Use if each variant has different data or behavior:

```java
public sealed interface PaymentMethod
        permits CreditCard, PayPal, BankTransfer {
}
```

```java
public record CreditCard(String number)
        implements PaymentMethod {
}

public record PayPal(String email)
        implements PaymentMethod {
}
```

Now each type can carry different data.

---

### Interface

Use if you want an **open set of implementations**:

```java
public interface PaymentMethod {
    PaymentResult pay(Money amount);
}
```

Third-party or future implementations can be added without modifying the interface declaration.

---

# 27. Abstract Class vs Sealed Class

These solve different problems.

An abstract class:

```java
public abstract class Payment {
}
```

says:

> "You cannot instantiate `Payment` directly."

It does **not** restrict who can extend it.

A sealed abstract class:

```java
public sealed abstract class Payment
        permits CreditCardPayment, PayPalPayment {
}
```

says:

> "You cannot instantiate `Payment`, and only these specific classes may extend it."

So:

```text
abstract
    → cannot instantiate

sealed
    → restricts inheritance

abstract + sealed
    → cannot instantiate + restricts inheritance
```

---

# 28. A Practical Comparison

| Type               | Main Purpose           | Can Instantiate? | Inheritance              | Typical Use                |
| ------------------ | ---------------------- | ---------------: | ------------------------ | -------------------------- |
| `class`            | Concrete object        |              Yes | Open by default          | Business/service objects   |
| `final class`      | Non-extendable object  |              Yes | No                       | Immutable/domain utilities |
| `abstract class`   | Shared base            |               No | Open                     | Common state/behavior      |
| `sealed class`     | Restricted hierarchy   |          Depends | Restricted               | Closed domain hierarchy    |
| `interface`        | Contract/capability    |               No | Multiple implementations | Ports, abstractions        |
| `sealed interface` | Restricted contract    |               No | Restricted               | Known variants             |
| `record`           | Immutable data carrier |              Yes | Cannot extend class      | DTOs, VOs                  |
| `enum`             | Fixed values           |  No direct `new` | Fixed constants          | Statuses, categories       |

---

# 29. Inheritance vs Composition — Interview Rule

When you have:

```text
A
B
```

ask:

> **Is B really an A?**

If yes:

```java
class B extends A
```

may be appropriate.

If instead:

> **Does A use/have B?**

prefer composition:

```java
class A {

    private final B b;
}
```

Example:

```text
Car IS-A Vehicle
```

→ inheritance may make sense.

```text
Car HAS-A Engine
```

→ composition.

---

# 30. Inheritance vs Interface

Ask:

> **Do I need shared implementation/state, or just a contract?**

If only a contract:

```java
interface PaymentGateway {
    PaymentResult charge(Money amount);
}
```

If several closely related classes share state and implementation:

```java
abstract class Payment {

    protected final Money amount;

    public void log() {
        // shared behavior
    }

    public abstract void process();
}
```

---

# 31. Interface vs Sealed Interface

Ask:

> **Can new implementations appear outside this hierarchy?**

If yes:

```java
interface PaymentResult
```

If no:

```java
sealed interface PaymentResult
        permits Success, Declined, Error
```

This is particularly useful when exhaustive `switch` expressions matter.

---

# 32. Class vs Record

Ask:

> **Is this primarily an immutable data carrier?**

If yes:

```java
record UserDto(...)
```

If it has a complex lifecycle, mutable state, identity, or rich behavior:

```java
class User
```

---

# 33. Record vs Value Object

A record is a **Java language feature**.

A Value Object is a **DDD concept**.

They are not the same thing.

A record can be an excellent implementation of a Value Object:

```java
public record Money(
        BigDecimal amount,
        Currency currency
) {}
```

But not every record is a Value Object:

```java
public record CreateUserRequest(
        String username,
        String email
) {}
```

This is a DTO.

Therefore:

```text
Record
 ├── DTO
 ├── Value Object
 ├── Data structure
 └── Other immutable carriers
```

---

# 34. Abstract Class vs Composition

Suppose you have:

```java
abstract class Notification {

    protected String recipient;

    public abstract void send();
}
```

and:

```java
class EmailNotification extends Notification {
}
```

This can be appropriate if every notification genuinely shares the same conceptual identity and behavior.

But if you're simply trying to reuse functionality:

```java
class Notification {

    private final MessageFormatter formatter;
}
```

composition may be better.

The general rule:

> **Use inheritance for specialization. Use composition for collaboration/reuse.**

---

# 35. Delegation

Delegation means an object forwards work to another object.

```java
public class OrderService {

    private final PricingService pricingService;

    public Money calculatePrice(Order order) {
        return pricingService.calculate(order);
    }
}
```

`OrderService` does not inherit from `PricingService`.

It delegates the responsibility.

This is often preferable to inheritance because responsibilities remain explicit.

---

# 36. A Real-World Example

Imagine you're building a payment system.

You could model it like this:

```text
PaymentMethod
     │
     │ sealed
     ▼
┌───────────────┬───────────────┬───────────────┐
│               │               │
CreditCard      PayPal          BankTransfer
record          record          record
```

For example:

```java
public sealed interface PaymentMethod
        permits CreditCard, PayPal, BankTransfer {
}
```

```java
public record CreditCard(
        String token
) implements PaymentMethod {
}
```

```java
public record PayPal(
        String accountId
) implements PaymentMethod {
}
```

```java
public record BankTransfer(
        String iban
) implements PaymentMethod {
}
```

Then the processing behavior could be defined through another abstraction:

```java
public interface PaymentProcessor {

    PaymentResult process(
            PaymentMethod paymentMethod,
            Money amount);
}
```

A service can depend on:

```java
PaymentProcessor
```

rather than a specific implementation.

This combines several concepts:

```text
sealed interface
    → controlled hierarchy

record
    → immutable data

interface
    → processing contract

composition
    → services collaborate

DTO
    → API communication

Domain model
    → business concepts
```

---

# 37. The Most Useful Mental Model

Think in terms of **four questions**.

### Question 1 — "Is this primarily data?"

```text
YES → record
```

Example:

```java
record UserDto(String name, String email)
```

---

### Question 2 — "Is this a capability/contract?"

```text
YES → interface
```

Example:

```java
interface PaymentGateway
```

---

### Question 3 — "Do related classes share state and implementation?"

```text
YES → abstract class may be appropriate
```

Example:

```java
abstract class BaseMessage
```

---

### Question 4 — "Do I know and control all possible subtypes?"

```text
YES → sealed class/interface
NO  → normal class/interface
```

---

# 38. The Senior-Level Principle

Don't choose a Java construct because it is technically available.

Choose it because it communicates a **design intention**.

For example:

```java
final class Money
```

communicates:

> "This type must not be extended."

```java
record Money(...)
```

communicates:

> "This is primarily immutable data."

```java
interface PaymentGateway
```

communicates:

> "This is a contract."

```java
sealed interface PaymentResult
```

communicates:

> "These are the only valid kinds of payment result."

```java
abstract class Payment
```

communicates:

> "This is a common base abstraction that should not be instantiated directly."

```java
class StripePaymentGateway
        implements PaymentGateway
```

communicates:

> "This is one concrete implementation of that contract."

```java
class OrderService {

    private final PaymentGateway paymentGateway;
}
```

communicates:

> "OrderService collaborates with a PaymentGateway."

That last point is important: **good code makes relationships explicit.**

---

# 39. Quick Decision Table

| Situation                                           | Prefer                   |
| --------------------------------------------------- | ------------------------ |
| Immutable DTO                                       | `record`                 |
| Small immutable Value Object                        | `record`                 |
| Fixed set of constants                              | `enum`                   |
| Public abstraction / contract                       | `interface`              |
| Multiple unrelated implementations                  | `interface`              |
| Dependency injection port                           | `interface`              |
| Known, closed set of variants                       | `sealed interface`       |
| Known, closed hierarchy + shared implementation     | `sealed abstract class`  |
| Shared state + implementation among related classes | `abstract class`         |
| Normal business object                              | `class`                  |
| Class should not be extended                        | `final class`            |
| True "is-a" relationship                            | Inheritance              |
| "has-a" / collaboration relationship                | Composition              |
| Reuse behavior without a type relationship          | Composition / delegation |
| Complex business object with identity               | Domain `class`           |
| API request/response                                | DTO, often `record`      |

---

# 40. The Rules Worth Memorizing

For a Java interview, these are the rules I would memorize:

> **Interface = contract/capability.**

> **Abstract class = shared foundation.**

> **Sealed type = controlled hierarchy.**

> **Record = immutable data carrier.**

> **Enum = fixed set of values.**

> **Final class = no inheritance.**

> **Inheritance = "is-a".**

> **Composition = "has-a" / "uses-a".**

> **Prefer composition over inheritance when there is no genuine subtype relationship.**

> **Use sealed types when the set of valid variants is known and should be enforced by the compiler.**

> **Use records when the primary purpose of the type is carrying immutable data, not managing a complex mutable lifecycle.**

> **Use interfaces for dependency inversion and replaceable implementations.**

> **Do not create abstractions just because you can.**

The final principle is the most important one:

> **Choose the construct that best communicates the intended relationship between your types.**
