# 3.4. Java / Spring Best Practices — Packages, DTOs, Entities, DAOs and Layers

## 1. Package Naming Convention

Java package names are conventionally written in **lowercase**, without underscores or hyphens.

The usual convention is to start with the company's reversed domain name:

```text
com.company.application
```

For example:

```text
com.lombardodier.trading
com.mycompany.order
com.example.payment
```

Avoid:

```text
com.myCompany.order
com.company.Order
com.company.my_application
```

---

# 2. How Should Packages Be Organized?

There are two common approaches.

## Approach A — Layer-Based Architecture

Everything is organized according to its technical role:

```text
com.example.order
│
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
└── exception
```

For example:

```text
com.example.order.controller
com.example.order.service
com.example.order.repository
com.example.order.entity
com.example.order.dto
```

This is simple and very common in Spring applications.

However, as the application grows, it can become difficult to understand which classes belong to which business feature.

---

# 3. Feature-Based / Domain-Based Packaging

A more scalable approach is to organize packages around **business capabilities**.

For example:

```text
com.example
│
├── order
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   ├── entity
│   └── mapper
│
├── customer
│   ├── controller
│   ├── dto
│   ├── service
│   ├── repository
│   ├── entity
│   └── mapper
│
└── payment
    ├── controller
    ├── dto
    ├── service
    ├── repository
    ├── entity
    └── mapper
```

This is often preferable for larger applications because the code related to one business capability stays together.

---

# 4. A More Advanced Structure — Hexagonal / Clean Architecture

For a more sophisticated application, the package structure can reflect architectural boundaries:

```text
com.example.order
│
├── domain
│   ├── model
│   │   ├── Order.java
│   │   ├── OrderLine.java
│   │   └── OrderStatus.java
│   │
│   └── port
│       └── OrderRepository.java
│
├── application
│   ├── port
│   │   ├── in
│   │   │   └── CreateOrderUseCase.java
│   │   │
│   │   └── out
│   │       └── LoadOrderPort.java
│   │
│   └── service
│       └── OrderService.java
│
└── infrastructure
    ├── web
    │   ├── OrderController.java
    │   └── dto
    │
    ├── persistence
    │   ├── OrderJpaEntity.java
    │   ├── OrderJpaRepository.java
    │   └── OrderPersistenceAdapter.java
    │
    └── external
        └── PaymentAdapter.java
```

The exact package names are not mandatory.

The important thing is that **the dependency direction reflects the architecture**.

---

# 5. Entity vs DTO vs DAO vs Domain Model

These terms are frequently confused.

They represent different concepts.

---

## Entity

An **Entity** represents something that has an identity and usually corresponds to a persistent object in a relational database when using JPA.

Example:

```java
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private Long id;

    private BigDecimal total;

    private String status;
}
```

The database might contain:

```text
orders
--------------------------------
id | total | status
--------------------------------
1  | 100   | CONFIRMED
2  | 250   | PENDING
```

The JPA entity represents a row in the database.

### Important

An entity is not necessarily the same thing as a **domain entity**.

In a simple application they may be the same class.

In a strict DDD / Hexagonal architecture, they are often separated.

---

# 6. Domain Entity

A **Domain Entity** represents a business concept with an identity and business behavior.

For example:

```java
public class Order {

    private final OrderId id;
    private OrderStatus status;

    public void confirm() {

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "A cancelled order cannot be confirmed"
            );
        }

        status = OrderStatus.CONFIRMED;
    }
}
```

This object is concerned with:

> **Business rules.**

It does not need to know about:

```text
JPA
Hibernate
SQL
Spring
PostgreSQL
```

---

# 7. Persistence Entity

A persistence entity is concerned with:

> **How data is stored.**

For example:

```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

It contains persistence concerns such as:

```java
@Entity
@Table
@Id
@GeneratedValue
@Column
@OneToMany
@ManyToOne
```

These annotations are infrastructure concerns.

---

# 8. Why Separate Domain Entity and JPA Entity?

Suppose your domain object contains:

```java
public class Order {

    public void confirm() {
        // Business rule
    }
}
```

while your persistence object contains:

```java
@Entity
public class OrderJpaEntity {

    @Id
    private Long id;
}
```

You then have a mapper:

```java
public class OrderMapper {

    public Order toDomain(OrderJpaEntity entity) {
        // Mapping
    }

    public OrderJpaEntity toEntity(Order order) {
        // Mapping
    }
}
```

The architecture becomes:

```text
Database
    │
    ▼
OrderJpaEntity
    │
    ▼
Mapper
    │
    ▼
Order Domain Entity
    │
    ▼
Business Logic
```

This gives you a strong separation between:

```text
Persistence model
        ≠
Business model
```

This is particularly useful when the domain is complex.

---

# 9. DTO — Data Transfer Object

A **DTO** is an object used to transfer data between boundaries.

For example, an HTTP request:

```json
{
    "username": "john",
    "email": "john@example.com"
}
```

can be represented by:

```java
public record CreateUserRequest(
        String username,
        String email
) {}
```

The DTO is not necessarily a business object.

Its purpose is communication.

---

# 10. Request DTO

A **request DTO** represents data coming into the application.

For example:

```java
public record CreateOrderRequest(
        Long customerId,
        List<OrderLineRequest> lines
) {}
```

Controller:

```java
@PostMapping
public OrderResponse create(
        @RequestBody CreateOrderRequest request) {

    // ...
}
```

---

# 11. Response DTO

A **response DTO** represents data returned by the application.

```java
public record OrderResponse(
        Long id,
        BigDecimal total,
        String status
) {}
```

Controller:

```java
@GetMapping("/{id}")
public OrderResponse getOrder(
        @PathVariable Long id) {

    // ...
}
```

---

# 12. Why Not Return the Entity Directly?

You could technically write:

```java
@GetMapping("/{id}")
public OrderEntity getOrder(...) {
    return repository.findById(...);
}
```

But this is usually a bad practice.

It couples your API to your persistence model.

For example, your database entity might contain:

```java
@Entity
public class UserEntity {

    private Long id;
    private String email;
    private String passwordHash;
    private String internalStatus;
}
```

Returning the entity directly could accidentally expose:

```text
passwordHash
internalStatus
internal database fields
```

A DTO gives you control:

```java
public record UserResponse(
        Long id,
        String email
) {}
```

Therefore:

```text
Database Entity
      │
      ▼
   Mapper
      │
      ▼
Response DTO
      │
      ▼
   REST API
```

---

# 13. DTO vs Entity — Simple Rule

Remember:

> **Entity = persistence / identity**
>
> **DTO = data transfer**
>
> **Domain Entity = business behavior**

For example:

```text
                   APPLICATION
                       │
          ┌────────────┴────────────┐
          │                         │
      Request DTO              Response DTO
          │                         ▲
          ▼                         │
       Use Case                 Mapper
          │                         ▲
          ▼                         │
    Domain Entity ────────────────┘
          │
          ▼
       Mapper
          │
          ▼
    JPA Entity
          │
          ▼
       Database
```

---

# 14. DAO — Data Access Object

**DAO** stands for **Data Access Object**.

Its purpose is to encapsulate access to a data source.

For example:

```java
public interface UserDao {

    Optional<UserEntity> findById(Long id);

    void save(UserEntity user);
}
```

Historically, DAOs were commonly used to encapsulate JDBC or SQL operations.

For example:

```java
public class UserDao {

    public UserEntity findById(Long id) {

        // SQL
        // Connection
        // PreparedStatement
        // ResultSet
    }
}
```

---

# 15. DAO vs Repository

These concepts overlap, but they have different origins.

### DAO

Usually focuses on:

> **Data access mechanics.**

For example:

```text
SQL
JDBC
ResultSet
Database connection
```

### Repository

Usually represents:

> **A collection of domain objects from the application's perspective.**

For example:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
```

The repository hides persistence details from the domain/application layer.

---

# 16. Spring Data Repository

In Spring Data JPA, you commonly see:

```java
public interface OrderRepository
        extends JpaRepository<OrderEntity, Long> {
}
```

Spring generates the implementation automatically.

For example:

```java
Optional<OrderEntity> findByCustomerId(Long customerId);
```

Spring Data can derive the query from the method name.

---

# 17. Service

A **service** generally contains application logic or business orchestration.

For example:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void createOrder(CreateOrderCommand command) {

        // Application orchestration
        // Business rules
        // Repository interaction
    }
}
```

However, be careful with the term **Service**.

A class called `SomethingService` should not become a dumping ground for every piece of logic.

Avoid:

```text
OrderService
 ├── createOrder()
 ├── calculatePrice()
 ├── sendEmail()
 ├── generatePdf()
 ├── callStripe()
 ├── validateCustomer()
 ├── convertCurrency()
 └── ...
```

This often indicates a **God Class**.

---

# 18. Controller

The controller belongs to the **delivery / presentation layer**.

Its responsibilities should generally be:

* Receive the request
* Validate input
* Convert the request to a command
* Call the application layer
* Convert the result into a response
* Return the HTTP response

Example:

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public OrderResponse create(
            @Valid @RequestBody CreateOrderRequest request) {

        var command = new CreateOrderCommand(
                request.customerId(),
                request.lines()
        );

        var order = createOrderUseCase.create(command);

        return OrderResponse.from(order);
    }
}
```

Avoid putting business rules directly inside controllers.

Bad:

```java
@PostMapping
public void create(...) {

    if (customer.getBalance().compareTo(total) < 0) {
        // Business rule
    }

    // More business logic...
}
```

The controller should be thin.

---

# 19. Mapper

A **Mapper** converts one representation into another.

For example:

```java
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.id().value(),
                order.total().amount(),
                order.status().name()
        );
    }
}
```

Or persistence mapping:

```java
public class OrderPersistenceMapper {

    public Order toDomain(OrderJpaEntity entity) {
        // ...
    }

    public OrderJpaEntity toEntity(Order order) {
        // ...
    }
}
```

A mapper should generally perform **transformation**, not business logic.

---

# 20. Validator

Validation is often divided into two categories.

## Structural / Input Validation

Example:

```java
public record CreateUserRequest(

        @NotBlank
        String username,

        @Email
        String email
) {}
```

This validates the input format.

---

## Business Validation

For example:

> An order cannot be confirmed if the customer's balance is insufficient.

That is a **business rule**, not simply input validation.

It belongs in the application/domain layer.

---

# 21. Exception Handling

Avoid scattering HTTP error handling throughout controllers.

Instead, use centralized exception handling:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            OrderNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(exception.getMessage()));
    }
}
```

This keeps controllers cleaner and provides consistent API responses.

---

# 22. Dependency Injection

Prefer **constructor injection**.

Good:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

Avoid field injection:

```java
@Autowired
private OrderRepository repository;
```

Constructor injection provides:

* Explicit dependencies
* Easier testing
* Immutability with `final`
* Better design visibility
* No reflection-based field injection requirement

---

# 23. Transaction Boundaries

Transactions should generally be defined around an **application use case**.

For example:

```java
@Transactional
public void createOrder(CreateOrderCommand command) {

    // Multiple operations
    // that must succeed or fail together
}
```

The important question is:

> **What business operation needs to be atomic?**

Not:

> "Which repository method should have `@Transactional`?"

For example:

```text
Create Order
    │
    ├── Create order
    ├── Save order lines
    ├── Update inventory
    └── Record transaction
```

If these operations must succeed or fail together, the transaction should generally surround the use case.

---

# 24. Common Layered Architecture

A typical Spring application might look like:

```text
                 HTTP
                  │
                  ▼
           ┌──────────────┐
           │  Controller  │
           └──────┬───────┘
                  │
                  ▼
           ┌──────────────┐
           │   Service    │
           └──────┬───────┘
                  │
                  ▼
           ┌──────────────┐
           │  Repository  │
           └──────┬───────┘
                  │
                  ▼
              Database
```

With DTOs:

```text
HTTP Request
     │
     ▼
Request DTO
     │
     ▼
Controller
     │
     ▼
Service / Use Case
     │
     ▼
Domain Model
     │
     ▼
Repository
     │
     ▼
JPA Entity
     │
     ▼
Database
```

And on the way back:

```text
Database
   │
   ▼
JPA Entity
   │
   ▼
Mapper
   │
   ▼
Domain Model
   │
   ▼
Mapper
   │
   ▼
Response DTO
   │
   ▼
HTTP Response
```

---

# 25. What Should Depend on What?

A very useful rule is:

```text
Controller
    │
    ▼
Application / Service
    │
    ▼
Domain
```

The dependency should generally point **towards the business logic**.

Infrastructure should implement abstractions defined by the inner layers.

In Hexagonal Architecture:

```text
             ┌─────────────────┐
             │   Controller    │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │   Application   │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │     Domain      │
             └─────────────────┘
                      ▲
                      │
             ┌────────┴────────┐
             │                 │
        JPA Adapter       Stripe Adapter
```

The database and external APIs are **details**.

The business domain is the core.

---

# 26. Common Naming Conventions

Use names that clearly communicate the role of the class.

```text
CreateOrderRequest
CreateOrderResponse
CreateOrderCommand
CreateOrderUseCase
CreateOrderService

Order
OrderId
OrderLine
OrderStatus

OrderJpaEntity
OrderRepository
OrderPersistenceAdapter
OrderMapper

OrderController
OrderException
OrderNotFoundException
```

Avoid vague names:

```text
Data
Helper
Utils
Manager
Processor
Handler
CommonService
GenericService
```

unless their responsibility is genuinely clear.

---

# 27. `Utils` Classes — Use With Caution

A class like:

```java
StringUtils
DateUtils
OrderUtils
```

can easily become a dumping ground.

Bad:

```java
OrderUtils
 ├── calculatePrice()
 ├── validateOrder()
 ├── sendEmail()
 ├── convertCurrency()
 └── formatAddress()
```

Prefer cohesive abstractions:

```text
PricingService
OrderValidator
CurrencyConverter
AddressFormatter
```

Each abstraction should have a clear purpose.

---

# 28. Immutability

Prefer immutable objects where practical.

For example:

```java
public record Money(
        BigDecimal amount,
        Currency currency
) {}
```

Instead of:

```java
public class Money {

    private BigDecimal amount;

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
```

Benefits include:

* Thread safety
* Easier reasoning
* Fewer side effects
* Easier testing
* Safer concurrent code

---

# 29. Avoid Primitive Obsession

Instead of:

```java
public void transfer(
        Long customerId,
        BigDecimal amount,
        String currency) {
}
```

a domain model can use meaningful types:

```java
public void transfer(
        CustomerId customerId,
        Money amount) {
}
```

For example:

```java
public record CustomerId(Long value) {}
```

and:

```java
public record Money(
        BigDecimal amount,
        Currency currency) {}
```

This makes invalid states harder to represent and gives the types domain meaning.

---

# 30. Avoid Anemic Domain Models When the Domain Is Complex

An **anemic domain model** is essentially a collection of data with all business logic placed elsewhere.

For example:

```java
public class Order {

    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
```

Then:

```java
orderService.confirm(order);
```

contains all the business rules.

For a complex domain, it may be better to put behavior inside the domain object:

```java
public class Order {

    private OrderStatus status;

    public void confirm() {

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot confirm a cancelled order"
            );
        }

        status = OrderStatus.CONFIRMED;
    }
}
```

Now:

```java
order.confirm();
```

The object protects its own invariants.

However, an anemic model is not automatically bad. For simple CRUD applications, a simpler model may be perfectly appropriate.

---

# 31. CRUD vs Rich Domain

Do not over-engineer every application.

For a simple CRUD application:

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Database
```

may be completely sufficient.

For a complex business system:

```text
Controller
    │
    ▼
Application Layer
    │
    ▼
Domain Model
    │
    ├── Entities
    ├── Value Objects
    ├── Aggregates
    └── Domain Services
    │
    ▼
Ports
    │
    ▼
Adapters
    │
    ▼
Infrastructure
```

may be justified.

A senior developer should know **when not to use an abstraction**.

---

# 32. The Most Important Distinctions

| Concept               | Main Responsibility                                 |
| --------------------- | --------------------------------------------------- |
| **Controller**        | HTTP / external request handling                    |
| **Request DTO**       | Input data transfer                                 |
| **Response DTO**      | Output data transfer                                |
| **Use Case**          | Application operation                               |
| **Service**           | Application orchestration and/or domain service     |
| **Domain Entity**     | Business object with identity and behavior          |
| **Value Object**      | Immutable concept defined by its value              |
| **JPA Entity**        | Persistence representation                          |
| **Repository**        | Abstraction for accessing domain/persistent objects |
| **DAO**               | Encapsulation of low-level data access              |
| **Mapper**            | Converts between representations                    |
| **Adapter**           | Connects the application to an external technology  |
| **Port**              | Abstraction defining an interaction boundary        |
| **Validator**         | Validates input or domain constraints               |
| **Exception Handler** | Centralized error-to-response mapping               |

---

# 33. A Good Package Structure for a Senior Java Application

A practical structure combining these principles could be:

```text
com.company.order
│
├── domain
│   ├── model
│   │   ├── Order.java
│   │   ├── OrderId.java
│   │   ├── OrderLine.java
│   │   ├── OrderStatus.java
│   │   └── Money.java
│   │
│   └── service
│       └── OrderPricingService.java
│
├── application
│   ├── port
│   │   ├── in
│   │   │   └── CreateOrderUseCase.java
│   │   │
│   │   └── out
│   │       ├── OrderRepositoryPort.java
│   │       └── PaymentGatewayPort.java
│   │
│   └── service
│       └── CreateOrderService.java
│
└── infrastructure
    ├── web
    │   ├── OrderController.java
    │   ├── CreateOrderRequest.java
    │   └── OrderResponse.java
    │
    ├── persistence
    │   ├── OrderJpaEntity.java
    │   ├── SpringDataOrderRepository.java
    │   ├── OrderPersistenceAdapter.java
    │   └── OrderPersistenceMapper.java
    │
    └── payment
        └── StripePaymentAdapter.java
```

This structure makes the architecture visible directly from the package hierarchy.

---

# 34. What to Remember for an Interview

If you only remember ten things, remember these:

### 1. Packages

> **Use lowercase package names and organize by business capability or architectural boundary.**

### 2. DTO

> **A DTO transfers data across a boundary. It is not your domain model.**

### 3. Entity

> **An entity has identity. A JPA entity is primarily a persistence concern.**

### 4. Value Object

> **A Value Object is defined by its values, not by an identity, and should generally be immutable.**

### 5. Repository

> **A repository abstracts access to persisted/domain objects.**

### 6. DAO

> **A DAO focuses on low-level data access; Repository is generally a higher-level domain/application abstraction.**

### 7. Mapper

> **A mapper transforms one representation into another. It should not contain business rules.**

### 8. Controller

> **Keep controllers thin. They should delegate business logic to the application/domain layer.**

### 9. Dependency direction

> **Business logic should not depend on infrastructure. Infrastructure should depend on abstractions defined by the core.**

### 10. Don't over-engineer

> **Architecture should match the complexity of the problem. A simple CRUD application does not necessarily need full DDD and Hexagonal Architecture.**

---

# The Mental Model

The easiest way to remember the whole structure is:

```text
                    EXTERNAL WORLD
                          │
                          ▼
                 ┌─────────────────┐
                 │    Controller    │
                 │   Request DTO    │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │   Application   │
                 │    Use Case     │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │     Domain      │
                 │                 │
                 │ Entities        │
                 │ Value Objects   │
                 │ Business Rules  │
                 │ Aggregates      │
                 └────────┬────────┘
                          │
                    Driven Ports
                          │
                          ▼
                 ┌─────────────────┐
                 │    Adapters     │
                 │                 │
                 │ JPA             │
                 │ PostgreSQL      │
                 │ Kafka           │
                 │ REST APIs       │
                 └─────────────────┘
```

The fundamental principle is:

> **DTOs communicate across boundaries. Entities and Value Objects model the domain. Repositories abstract persistence. Adapters connect the application to technical infrastructure. Controllers translate external requests into application operations.**

And the most important architectural rule is:

> **Business logic should not know or care whether the data comes from PostgreSQL, Kafka, REST, Redis, or another external system.**
