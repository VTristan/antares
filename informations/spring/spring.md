### Corrected English question

> **In Markdown and English, do the same for this section. Go further and explain the concepts in depth, especially the Spring IoC container, bean lifecycle, Spring Data JPA, Hibernate, persistence context, dirty checking, N+1 queries, transactions, proxies, and Spring Security 6.**

````markdown
# 4. Spring & Spring Boot Ecosystem

## 4.1. Spring IoC Container & Bean Lifecycle

### 4.1.1. What Is IoC?

**IoC (Inversion of Control)** means that your application does not manually create and manage every object it needs.

Without Spring:

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService() {
        this.repository = new OrderRepository();
    }
}
````

`OrderService` is responsible for creating its dependency.

With Spring:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

Spring creates both objects and injects the repository into the service.

The responsibility has been inverted:

```text
Without IoC:

OrderService
    │
    └── creates → OrderRepository


With IoC:

Spring Container
    │
    ├── creates OrderRepository
    │
    ├── creates OrderService
    │
    └── injects Repository → OrderService
```

The fundamental idea is:

> **Your classes declare what they need; the framework decides how and when those dependencies are created and connected.**

---

## 4.1.2. What Is the Spring Container?

The Spring container is responsible for managing application objects called **beans**.

The main container abstraction is:

```java
ApplicationContext
```

For example:

```java
ApplicationContext context =
        new AnnotationConfigApplicationContext(AppConfig.class);
```

You can conceptually think of the container as an object registry:

```text
ApplicationContext
│
├── OrderService
├── OrderRepository
├── PaymentService
├── PaymentGateway
├── UserService
└── ...
```

The container knows:

* Which beans exist
* How to create them
* Which dependencies they have
* Their scope
* Their lifecycle
* Which post-processors should process them
* Which proxies should be created around them

---

# 4.1.3. What Is a Bean?

A **Spring bean** is an object managed by the Spring container.

For example:

```java
@Component
public class EmailService {
}
```

Spring detects the class and creates an instance.

You can also explicitly declare a bean:

```java
@Configuration
public class AppConfig {

    @Bean
    public PaymentGateway paymentGateway() {
        return new StripePaymentGateway();
    }
}
```

Both result in an object managed by Spring.

---

# 4.1.4. `@Component`, `@Service`, `@Repository`, `@Controller`

These annotations are specialized forms of `@Component`.

```text
@Component
   │
   ├── @Service
   ├── @Repository
   └── @Controller
```

They are all detected through component scanning.

### `@Component`

Generic Spring-managed component:

```java
@Component
public class CurrencyConverter {
}
```

### `@Service`

Used to communicate that the class represents application/business logic:

```java
@Service
public class OrderService {
}
```

### `@Repository`

Used for persistence components:

```java
@Repository
public class OrderRepository {
}
```

It also participates in Spring's exception translation mechanism for persistence exceptions.

### `@Controller`

Used for MVC controllers:

```java
@Controller
public class OrderController {
}
```

For REST APIs:

```java
@RestController
public class OrderController {
}
```

`@RestController` is essentially:

```java
@Controller
@ResponseBody
```

---

# 4.1.5. Dependency Injection

Spring supports several forms of dependency injection.

## Constructor Injection

Preferred:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

With modern Spring, if there is only one constructor, `@Autowired` is unnecessary.

```java
public OrderService(OrderRepository repository) {
}
```

is enough.

### Why Constructor Injection?

It provides:

* Explicit dependencies
* Immutability
* Easier unit testing
* Guaranteed initialization
* Better design

For example:

```java
OrderRepository repository =
        mock(OrderRepository.class);

OrderService service =
        new OrderService(repository);
```

No Spring container is required for the unit test.

---

# 4.1.6. Field Injection

Avoid:

```java
@Autowired
private OrderRepository repository;
```

Problems:

* Dependencies are hidden
* Fields cannot naturally be `final`
* Harder to instantiate the class manually
* Harder to unit test
* Encourages classes with many hidden dependencies

Constructor injection makes dependencies visible:

```java
public OrderService(
        OrderRepository repository,
        PaymentGateway paymentGateway,
        OrderMapper mapper) {
}
```

If a constructor becomes huge, that can also be a design signal:

> **The class may have too many responsibilities.**

---

# 4.1.7. `@Bean` vs `@Component`

Use component scanning when you own the class:

```java
@Service
public class OrderService {
}
```

Use `@Bean` when you need to explicitly construct/configure an object:

```java
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }
}
```

`@Bean` is especially useful for:

* Third-party classes
* Complex configuration
* Multiple implementations
* Explicit customization

---

# 4.1.8. Dependency Resolution

Suppose you have:

```java
public interface PaymentGateway {
}
```

and:

```java
@Component
public class StripePaymentGateway
        implements PaymentGateway {
}
```

Then:

```java
@Service
public class PaymentService {

    private final PaymentGateway gateway;

    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

Spring sees:

```text
PaymentService
      │
      │ requires
      ▼
PaymentGateway
      ▲
      │ implements
StripePaymentGateway
```

Spring injects the matching bean.

---

# 4.1.9. What If There Are Multiple Implementations?

Suppose:

```java
@Component
public class StripePaymentGateway
        implements PaymentGateway {
}
```

and:

```java
@Component
public class AdyenPaymentGateway
        implements PaymentGateway {
}
```

Now Spring cannot determine which implementation to inject.

You can use `@Primary`:

```java
@Primary
@Component
public class StripePaymentGateway
        implements PaymentGateway {
}
```

Or `@Qualifier`:

```java
public PaymentService(
        @Qualifier("stripePaymentGateway")
        PaymentGateway gateway) {
}
```

This is an important dependency injection concept.

---

# 4.1.10. Bean Scopes

The default scope is:

```text
singleton
```

This means:

> One bean instance per Spring ApplicationContext.

Other important scopes include:

```text
singleton
prototype
request
session
application
websocket
```

For example:

```java
@Scope("prototype")
@Component
public class MyBean {
}
```

A prototype bean is created each time the container is asked for one.

---

# 4.1.11. Singleton Does Not Mean "One Instance in the Entire JVM"

A Spring singleton means:

> **One instance per Spring ApplicationContext.**

It does not mean:

> One instance globally across all JVMs or all applications.

In a distributed system:

```text
JVM 1 → ApplicationContext → OrderService #1

JVM 2 → ApplicationContext → OrderService #2

JVM 3 → ApplicationContext → OrderService #3
```

Each application instance has its own Spring singleton.

---

# 4.1.12. Bean Lifecycle

A simplified lifecycle is:

```text
Bean Definition
      ↓
Instantiation
      ↓
Dependency Injection
      ↓
Aware callbacks
      ↓
BeanPostProcessor
(before initialization)
      ↓
@PostConstruct
      ↓
InitializingBean
      ↓
Custom init method
      ↓
BeanPostProcessor
(after initialization)
      ↓
READY
      ↓
@PreDestroy
      ↓
DisposableBean
      ↓
Destroyed
```

The exact internal lifecycle is more complex, but this is the useful mental model.

---

# 4.1.13. `@PostConstruct`

Use `@PostConstruct` when initialization requires injected dependencies.

```java
@Service
public class CacheService {

    private final Configuration configuration;

    public CacheService(Configuration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    void initialize() {
        // Initialization
    }
}
```

At this point, dependencies have already been injected.

---

# 4.1.14. `@PreDestroy`

Called when the Spring context is shutting down.

```java
@PreDestroy
void shutdown() {
    // Cleanup
}
```

Typical use cases:

* Closing resources
* Stopping background processes
* Cleaning up connections

However, external infrastructure should generally be managed by dedicated libraries/frameworks when possible.

---

# 4.1.15. BeanPostProcessor

A `BeanPostProcessor` allows Spring to intercept bean creation.

Conceptually:

```java
Object postProcessBeforeInitialization(
        Object bean,
        String beanName);
```

and:

```java
Object postProcessAfterInitialization(
        Object bean,
        String beanName);
```

Spring uses this mechanism extensively.

For example:

```text
Original Bean
     ↓
BeanPostProcessor
     ↓
Proxy
```

This is one of the mechanisms behind Spring's:

* AOP
* `@Transactional`
* `@Async`
* Security
* Caching

---

# 4.1.16. Spring Proxies

A major concept to understand is that Spring often does not call your object directly.

It may call a **proxy**.

For example:

```java
@Transactional
public void createOrder() {
}
```

Conceptually:

```text
Caller
   │
   ▼
Spring Proxy
   │
   ├── Start transaction
   │
   ├── Call actual method
   │
   ├── Commit transaction
   │
   └── Rollback if necessary
```

The actual object contains:

```java
createOrder()
```

but the proxy adds behavior around it.

This explains several Spring mechanisms.

---

# 4.2. Spring Data JPA & Hibernate

## 4.2.1. JPA vs Hibernate vs Spring Data JPA

These are different things.

### JPA

JPA is a **Java specification**.

It defines APIs and concepts such as:

```text
@Entity
@Id
@OneToMany
EntityManager
Persistence Context
```

### Hibernate

Hibernate is an **implementation of JPA**.

```text
JPA
 ↓
Hibernate
 ↓
SQL
 ↓
Database
```

### Spring Data JPA

Spring Data JPA provides abstractions on top of JPA.

For example:

```java
public interface OrderRepository
        extends JpaRepository<OrderEntity, Long> {
}
```

Spring Data creates much of the repository implementation for you.

Therefore:

```text
Spring Data JPA
       ↓
     JPA
       ↓
   Hibernate
       ↓
      JDBC
       ↓
    Database
```

---

# 4.2.2. EntityManager

The `EntityManager` is a central JPA abstraction.

It manages entities inside the **persistence context**.

For example:

```java
EntityManager entityManager;
```

You can think of the persistence context as a managed set of entities:

```text
Persistence Context
│
├── Order #1
├── Customer #5
└── Product #42
```

---

# 4.2.3. Persistence Context

The persistence context is essentially the set of entities currently managed by an `EntityManager`.

An entity can be:

```text
NEW
 │
 ▼
MANAGED
 │
 ├── modified
 │
 ▼
REMOVED
```

There is also:

```text
DETACHED
```

A managed entity is tracked by Hibernate.

---

# 4.2.4. First-Level Cache

The persistence context also acts as a **first-level cache**.

Suppose:

```java
Order order1 =
        entityManager.find(Order.class, 1L);

Order order2 =
        entityManager.find(Order.class, 1L);
```

Within the same persistence context, Hibernate can return the same managed entity rather than issuing another SQL query.

Conceptually:

```text
find(Order, 1)
      ↓
Persistence Context
      ↓
Order #1

find(Order, 1)
      ↓
Persistence Context
      ↓
Same managed entity
```

This provides identity guarantees within the persistence context.

---

# 4.2.5. Dirty Checking

Dirty checking is one of Hibernate's most important features.

Suppose:

```java
@Transactional
public void renameOrder(Long id) {

    OrderEntity order =
            repository.findById(id).orElseThrow();

    order.setName("New name");
}
```

Notice:

```java
repository.save(order);
```

may not be necessary.

Why?

Because `order` is managed.

Hibernate tracks changes.

At flush time:

```text
Initial state:
name = "Old name"

        ↓

Java code:
name = "New name"

        ↓

Hibernate detects difference

        ↓

UPDATE orders
SET name = 'New name'
WHERE id = ?
```

This is **dirty checking**.

---

# 4.2.6. Snapshot Concept

Hibernate can conceptually maintain an original state:

```text
Snapshot
name = "Old name"
```

Current state:

```text
Entity
name = "New name"
```

Hibernate detects:

```text
Old != New
```

and generates an SQL `UPDATE`.

The implementation details are more sophisticated than simply comparing two Java objects, but this is the correct mental model.

---

# 4.2.7. Flush vs Commit

These concepts are often confused.

### Flush

Synchronizes changes from the persistence context to the database.

```text
Java entity
    ↓
Persistence Context
    ↓
FLUSH
    ↓
SQL
    ↓
Database
```

### Commit

Makes the transaction permanent.

```text
Transaction
    ↓
Commit
    ↓
Changes become permanent
```

A flush does not necessarily mean the transaction has committed.

---

# 4.2.8. N+1 Query Problem

Suppose:

```java
List<Order> orders =
        orderRepository.findAll();
```

and each order has:

```java
@OneToMany
private List<OrderItem> items;
```

If `items` is lazy:

```java
order.getItems()
```

may trigger another SQL query.

If you have 100 orders:

```text
1 query → load orders

+ 100 queries → load items

= 101 queries
```

This is the classic **N+1 problem**.

```text
1 + N queries
```

---

# 4.2.9. Example of N+1

Conceptually:

```java
List<Order> orders = repository.findAll();

for (Order order : orders) {
    System.out.println(order.getItems());
}
```

SQL might become:

```sql
SELECT * FROM orders;

SELECT * FROM order_items WHERE order_id = 1;
SELECT * FROM order_items WHERE order_id = 2;
SELECT * FROM order_items WHERE order_id = 3;
...
```

This can destroy performance.

---

# 4.2.10. Solving N+1 with `JOIN FETCH`

You can explicitly fetch the relationship:

```java
@Query("""
    SELECT o
    FROM OrderEntity o
    JOIN FETCH o.items
    WHERE o.id = :id
""")
Optional<OrderEntity> findByIdWithItems(Long id);
```

The idea is:

```text
Orders + Items
      ↓
One SQL query
```

instead of:

```text
Orders
  +
one query per order
```

However, `JOIN FETCH` should not be treated as a universal solution.

Fetching large relationships can produce huge result sets.

---

# 4.2.11. `@EntityGraph`

Another option is:

```java
@EntityGraph(attributePaths = {"items"})
List<OrderEntity> findAll();
```

This tells Spring Data / JPA that the association should be fetched as part of this query.

It can be cleaner than embedding `JOIN FETCH` into every JPQL query.

---

# 4.2.12. `@BatchSize`

Another strategy is batching.

```java
@BatchSize(size = 20)
@OneToMany
private List<OrderItem> items;
```

Instead of:

```text
SELECT items WHERE order_id = 1
SELECT items WHERE order_id = 2
SELECT items WHERE order_id = 3
...
```

Hibernate can group identifiers into batches.

Conceptually:

```sql
SELECT *
FROM order_items
WHERE order_id IN (?, ?, ?, ..., ?);
```

This reduces the number of queries.

---

# 4.2.13. N+1 Is Not Always About `@OneToMany`

It can happen with:

```text
@ManyToOne
@OneToOne
@OneToMany
@ManyToMany
```

The important pattern is:

```text
1 query
+
loop
+
lazy access
+
N additional queries
```

Always inspect the generated SQL when diagnosing it.

---

# 4.2.14. Pagination + `JOIN FETCH`

This is a particularly important Hibernate trap.

Suppose:

```java
Page<OrderEntity> findAll(Pageable pageable);
```

and you try to fetch a collection:

```java
SELECT o
FROM OrderEntity o
JOIN FETCH o.items
```

A collection join can multiply rows:

```text
Order 1 → Item 1
Order 1 → Item 2
Order 1 → Item 3
Order 2 → Item 4
...
```

Database-level pagination becomes problematic because the rows are no longer one-to-one with orders.

Hibernate may have to perform pagination in memory in some situations.

This can produce:

```text
Huge SQL result
      ↓
Loaded into JVM
      ↓
Hibernate applies pagination
      ↓
Potential memory problem
```

This is especially dangerous with large datasets.

---

# 4.2.15. Better Pagination Strategies

For complex relationships, consider:

### Strategy 1 — Page IDs first

First query:

```sql
SELECT id
FROM orders
ORDER BY id
LIMIT 20 OFFSET 100;
```

Then fetch those orders and their relationships:

```sql
SELECT ...
FROM orders
JOIN order_items ...
WHERE orders.id IN (...);
```

### Strategy 2 — EntityGraph / batch fetching

Depending on the relationship and use case.

### Strategy 3 — DTO projections

Sometimes you don't need full entities at all.

```java
public record OrderSummary(
        Long id,
        BigDecimal total
) {}
```

Query only what you need.

---

# 4.2.16. DTO Projection

Instead of:

```text
Database
   ↓
Huge Entity Graph
   ↓
Java objects
   ↓
DTO
```

you can directly retrieve the required data.

For example:

```java
@Query("""
    SELECT new com.example.order.OrderSummary(
        o.id,
        o.total
    )
    FROM OrderEntity o
""")
List<OrderSummary> findSummaries();
```

This can be much more efficient for read-heavy operations.

---

# 4.2.17. `LAZY` vs `EAGER`

Associations can have fetching strategies.

```java
@OneToMany(fetch = FetchType.LAZY)
```

means:

> Load the relationship when needed.

```java
@ManyToOne(fetch = FetchType.EAGER)
```

means:

> Load it immediately.

A common best practice is:

> **Prefer LAZY fetching and explicitly fetch what a particular use case needs.**

Do not blindly make everything `EAGER`.

EAGER associations can cause:

* Unexpected queries
* Large object graphs
* Performance problems
* More difficult query control

---

# 4.2.18. Transactions

A transaction groups database operations into an atomic unit.

Example:

```java
@Transactional
public void transferMoney(
        Long sourceId,
        Long targetId,
        BigDecimal amount) {

    withdraw(sourceId, amount);
    deposit(targetId, amount);
}
```

The desired property is:

```text
Everything succeeds
        OR
Everything rolls back
```

You don't want:

```text
Withdraw → SUCCESS
Deposit  → FAILURE

Result:
Money disappeared
```

---

# 4.2.19. ACID

Transactions are commonly described using ACID:

### Atomicity

All operations succeed or none do.

### Consistency

The database remains in a valid state according to its constraints.

### Isolation

Concurrent transactions should not interfere in invalid ways.

### Durability

Committed data survives failures according to the database's guarantees.

---

# 4.2.20. `@Transactional`

Example:

```java
@Service
public class TransferService {

    @Transactional
    public void transfer(...) {
        // database operations
    }
}
```

Spring's transaction infrastructure typically works through a proxy.

Conceptually:

```text
Caller
   ↓
Spring Proxy
   ↓
BEGIN TRANSACTION
   ↓
transfer()
   ↓
COMMIT
```

If an appropriate exception occurs:

```text
BEGIN
  ↓
transfer()
  ↓
Exception
  ↓
ROLLBACK
```

---

# 4.2.21. `Propagation.REQUIRED`

Default propagation:

```java
@Transactional(
    propagation = Propagation.REQUIRED
)
```

Meaning:

> Join an existing transaction if one exists; otherwise create one.

Example:

```text
OrderService.createOrder()
       │
       ▼
Transaction A
       │
       ├── OrderRepository.save()
       │
       └── PaymentService.pay()
```

If `PaymentService.pay()` uses `REQUIRED`, it normally participates in the same transaction.

---

# 4.2.22. `REQUIRES_NEW`

```java
@Transactional(
    propagation = Propagation.REQUIRES_NEW
)
```

means:

> Suspend the current transaction and start a new one.

Conceptually:

```text
Transaction A
     │
     ├── operation
     │
     ├── suspend
     │
     ▼
Transaction B
     │
     ├── operation
     │
     └── COMMIT
     │
     ▼
Resume Transaction A
```

This is useful in specific cases such as independent audit records, but it should not be used casually.

---

# 4.2.23. A Common `REQUIRES_NEW` Trap

Suppose:

```java
@Transactional
public void processOrder() {

    saveOrder();

    auditService.record();

    throw new RuntimeException();
}
```

and:

```java
@Transactional(propagation = REQUIRES_NEW)
public void record() {
}
```

The audit transaction can commit independently even though the outer transaction later rolls back.

That can be intentional:

```text
Order → ROLLBACK
Audit → COMMIT
```

But it can also surprise developers.

---

# 4.2.24. Self-Invocation Trap

Consider:

```java
@Service
public class OrderService {

    public void methodA() {
        methodB();
    }

    @Transactional
    public void methodB() {
        // ...
    }
}
```

You might expect Spring to apply the transaction to `methodB()`.

But:

```java
methodB();
```

is an internal call on `this`.

It does not pass through the Spring proxy.

Conceptually:

```text
External caller
      ↓
Spring Proxy
      ↓
methodA()
      ↓
this.methodB()
      ↓
Actual object
```

The proxy is bypassed for `methodB()`.

Therefore the `@Transactional` interception does not occur as you might expect.

---

# 4.2.25. Better Solution

Move the transactional operation to another bean:

```java
@Service
public class OrderService {

    private final TransactionalOrderService transactionalService;

    public void methodA() {
        transactionalService.methodB();
    }
}
```

Now:

```text
OrderService
     ↓
Spring Proxy
     ↓
TransactionalOrderService.methodB()
```

The proxy is involved.

---

# 4.2.26. Rollback Rules

By default, Spring rolls back transactions for:

```text
RuntimeException
Error
```

but not necessarily for checked exceptions.

For example:

```java
@Transactional
public void process() throws IOException {
    ...
}
```

An `IOException` does not automatically imply rollback under the default rules.

You can explicitly configure:

```java
@Transactional(
    rollbackFor = Exception.class
)
```

Now checked exceptions are included.

The important interview answer is:

> **By default, Spring rolls back on unchecked exceptions (`RuntimeException`) and `Error`, not checked exceptions.**

---

# 4.2.27. Transaction Boundaries

A useful architectural rule is:

> **Put transaction boundaries around business use cases.**

For example:

```java
@Transactional
public void createOrder(CreateOrderCommand command) {
    ...
}
```

rather than blindly putting `@Transactional` everywhere.

The question should be:

> **Which operations must succeed or fail together?**

---

# 4.2.28. Isolation Levels

Transaction isolation controls how one transaction sees another transaction's changes.

Common levels:

```text
READ_UNCOMMITTED
READ_COMMITTED
REPEATABLE_READ
SERIALIZABLE
```

Conceptually:

```text
More concurrency
      ↑
READ_UNCOMMITTED
READ_COMMITTED
REPEATABLE_READ
SERIALIZABLE
      ↓
More isolation
```

Higher isolation can reduce concurrency because more locking or coordination may be required.

The exact behavior depends on the database.

---

# 4.2.29. Optimistic vs Pessimistic Locking

### Optimistic Locking

Assumes conflicts are relatively rare.

JPA:

```java
@Version
private Long version;
```

Suppose:

```text
User A → version 5
User B → version 5
```

A updates:

```text
version 5 → 6
```

B then tries to update version 5.

Hibernate detects the conflict.

This prevents silent overwrites.

---

### Pessimistic Locking

Assumes conflicts may occur and locks the database row.

Conceptually:

```sql
SELECT ...
FROM orders
WHERE id = ?
FOR UPDATE;
```

Other transactions may have to wait.

Use it carefully because excessive locking can reduce concurrency and cause deadlocks.

---

# 4.3. Spring Security 6 & Stateless REST APIs

Spring Security is responsible for:

* Authentication
* Authorization
* Security filters
* Password handling
* CSRF protection
* Session management
* OAuth2 / OIDC
* JWT resource servers
* Method security

Two concepts must be clearly separated:

> **Authentication = Who are you?**

> **Authorization = What are you allowed to do?**

---

# 4.3.1. Authentication

Example:

```text
Username + Password
        ↓
Authentication
        ↓
Authenticated User
```

Spring can represent the authenticated user using:

```java
Authentication
```

which is available through the security context.

---

# 4.3.2. Authorization

After authentication:

```text
User = Alice

Roles:
ADMIN
USER
```

An endpoint might require:

```text
ADMIN
```

Then:

```text
Alice → ADMIN → allowed
Bob   → USER  → denied
```

This is authorization.

---

# 4.3.3. Security Filter Chain

For a REST application, requests pass through Spring Security filters before reaching your controller.

Conceptually:

```text
HTTP Request
     ↓
Security Filters
     ↓
Authentication
     ↓
Authorization
     ↓
Controller
     ↓
Service
```

For example:

```text
GET /orders/123
Authorization: Bearer <JWT>
```

The security layer validates the token before the request reaches:

```java
@GetMapping("/orders/{id}")
```

---

# 4.3.4. Stateless REST

A stateless API does not maintain server-side session state for each client.

A common architecture is:

```text
Client
   │
   │ Authorization: Bearer JWT
   ▼
Spring Security
   │
   ├── Validate JWT
   ├── Extract identity
   ├── Extract authorities
   └── Authorize request
   │
   ▼
Controller
```

The server does not need to remember the user's authentication state in an HTTP session.

---

# 4.3.5. JWT

JWT stands for **JSON Web Token**.

A simplified JWT contains:

```text
HEADER.PAYLOAD.SIGNATURE
```

The payload can contain claims such as:

```json
{
  "sub": "12345",
  "roles": ["USER"],
  "exp": 1780000000
}
```

The signature allows the receiver to verify that the token was issued by a trusted party and has not been modified.

Important:

> **A signed JWT is not automatically encrypted.**

Do not put sensitive secrets into a normal JWT payload assuming the contents are hidden.

---

# 4.3.6. OAuth2 / OpenID Connect

These concepts are often confused.

### OAuth 2.0

Primarily an **authorization framework**.

It answers:

> "What can this application access?"

### OpenID Connect

An identity layer built on OAuth 2.0.

It answers:

> "Who is the user?"

For modern applications you may have:

```text
Client
  ↓
Identity Provider
  ↓
Access Token / ID Token
  ↓
Spring Security
```

---

# 4.3.7. Password Hashing

Never store passwords directly.

Bad:

```text
password = "MyPassword123"
```

Instead use a password hashing algorithm such as:

```java
PasswordEncoder
```

For example:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

The database stores a password hash rather than the original password.

---

# 4.3.8. Method Security

You can secure methods:

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {
}
```

Then authorization occurs at the method boundary.

Enable it with:

```java
@EnableMethodSecurity
```

This can be useful when authorization depends on business operations rather than only URL patterns.

---

# 4.3.9. CSRF and REST APIs

CSRF is mainly a concern when authentication credentials are automatically attached by the browser, such as cookies.

A typical stateless REST API using:

```text
Authorization: Bearer <token>
```

has a different threat model.

Therefore many JWT-based APIs disable CSRF:

```java
http.csrf(csrf -> csrf.disable());
```

But this should not be interpreted as:

> "CSRF is always unnecessary for REST."

The correct question is:

> **How are credentials transported and automatically attached to requests?**

If authentication uses cookies, CSRF can still be relevant.

---

# 4.3.10. A Modern Security Configuration

A simplified Spring Security configuration might look like:

```java
@Bean
SecurityFilterChain securityFilterChain(
        HttpSecurity http) throws Exception {

    return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/public/**")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(Customizer.withDefaults()))
            .build();
}
```

The flow is:

```text
HTTP Request
      ↓
JWT
      ↓
Spring Security
      ↓
JWT validation
      ↓
Authentication
      ↓
Authorization
      ↓
Controller
```

---

# 4.4. How All These Spring Concepts Connect

A typical Spring Boot request can be understood as:

```text
                    HTTP REQUEST
                         │
                         ▼
               ┌──────────────────┐
               │ Spring Security  │
               │ Filter Chain     │
               └────────┬─────────┘
                        │
                 Authentication
                 + Authorization
                        │
                        ▼
               ┌──────────────────┐
               │   Controller     │
               └────────┬─────────┘
                        │
                    Request DTO
                        │
                        ▼
               ┌──────────────────┐
               │ Application      │
               │ Service / UseCase│
               └────────┬─────────┘
                        │
                 @Transactional
                        │
                        ▼
               ┌──────────────────┐
               │ Repository       │
               └────────┬─────────┘
                        │
                        ▼
                  Spring Data JPA
                        │
                        ▼
                     Hibernate
                        │
                Persistence Context
                        │
                  Dirty Checking
                        │
                        ▼
                       JDBC
                        │
                        ▼
                    Database
```

And Spring itself is managing the objects:

```text
                    ApplicationContext
                           │
          ┌────────────────┼─────────────────┐
          ▼                ▼                 ▼
    Controller         Service          Repository
          │                │                 │
          └────────────────┼─────────────────┘
                           │
                      Dependencies
```

Spring may additionally wrap beans with proxies:

```text
                     Caller
                       │
                       ▼
                Spring Proxy
                       │
              ┌────────┼────────┐
              │        │        │
        Transaction   Security   Cache
              │        │        │
              └────────┼────────┘
                       ▼
                  Real Bean
```

---

# 4.5. Important Senior-Level Pitfalls

## Pitfall 1 — `@Transactional` does not magically work everywhere

It usually relies on Spring proxy interception.

Therefore:

```java
this.someTransactionalMethod();
```

can bypass the proxy.

---

## Pitfall 2 — `@Transactional` does not make everything atomic

It only covers operations participating in that transaction.

External systems such as:

```text
Kafka
Stripe
REST APIs
Email
```

do not automatically participate in your database transaction.

This is a major distributed-systems concept.

---

## Pitfall 3 — JPA does not mean "one SQL query"

One Java method can generate:

```text
1 query
10 queries
101 queries
1000 queries
```

depending on mappings and access patterns.

Always understand the SQL Hibernate generates.

---

## Pitfall 4 — Lazy loading is not inherently bad

`LAZY` is generally useful.

The problem is uncontrolled lazy loading.

Good:

```text
Use case
   ↓
Explicitly fetch required data
```

Bad:

```text
Loop
 ↓
Lazy access
 ↓
Query
 ↓
Loop
 ↓
Query
 ↓
...
```

---

## Pitfall 5 — `save()` is not always required after modifying a managed entity

Inside a transaction:

```java
Order order = repository.findById(id).orElseThrow();

order.setStatus(CONFIRMED);
```

Hibernate can detect the change automatically.

Calling:

```java
repository.save(order);
```

may be unnecessary depending on the context.

The important concept is **managed entity + dirty checking**.

---

## Pitfall 6 — `EAGER` is not a performance optimization

Making everything eager does not solve N+1.

It can simply move the problem somewhere else and load too much data.

Prefer:

```text
LAZY by default
+
explicit fetching per use case
```

---

## Pitfall 7 — Don't return JPA entities from REST APIs

Prefer:

```text
JPA Entity
    ↓
Mapper
    ↓
Response DTO
    ↓
JSON
```

This prevents your persistence model from becoming your public API.

---

## Pitfall 8 — Don't put business logic in controllers

Avoid:

```text
Controller
 ├── validation
 ├── business rules
 ├── persistence
 ├── calculations
 └── external API calls
```

Prefer:

```text
Controller
    ↓
Use Case / Service
    ↓
Domain
    ↓
Ports
    ↓
Adapters
```

---

# 4.6. What You Should Be Able to Explain in an Interview

### IoC

> Spring manages object creation and dependency wiring instead of application classes manually creating their dependencies.

### Dependency Injection

> Dependencies are provided to a class, preferably through its constructor, instead of being created inside the class.

### Bean

> A bean is an object managed by the Spring ApplicationContext.

### Bean lifecycle

> Spring instantiates the bean, injects dependencies, runs initialization callbacks and post-processors, potentially creates a proxy, and eventually invokes destruction callbacks.

### Proxy

> Spring proxies allow framework behavior such as transactions, security, caching and async execution to be applied around method calls.

### JPA

> JPA is a specification for object-relational persistence.

### Hibernate

> Hibernate is a major implementation of JPA.

### Spring Data JPA

> Spring Data JPA provides repository abstractions and reduces persistence boilerplate on top of JPA.

### Persistence Context

> It is the set of managed entities associated with an EntityManager and provides identity management, dirty checking and first-level caching.

### Dirty Checking

> Hibernate detects changes made to managed entities and generates the required SQL during flush.

### N+1

> One query loads the parent objects, then additional queries are executed when a relationship is accessed for each parent.

### `@Transactional`

> Defines a transaction boundary, allowing a group of database operations to commit or roll back as a unit.

### `REQUIRED`

> Join the existing transaction or create one if none exists.

### `REQUIRES_NEW`

> Suspend the existing transaction and execute inside a new independent transaction.

### Self-invocation

> An internal method call can bypass the Spring proxy, preventing proxy-based features such as `@Transactional` from being applied.

### Stateless REST

> Each request contains the authentication information required to process it, commonly through a Bearer token, without relying on a server-side HTTP session.

### Authentication vs Authorization

> Authentication establishes who the user is; authorization determines what the user is allowed to do.

---

# 4.7. The Most Important Mental Model

For Spring, think in four layers of responsibility:

```text
1. SPRING
   "How are my objects created and connected?"

2. SECURITY
   "Who is calling me and are they allowed?"

3. APPLICATION
   "What operation does the user want to perform?"

4. PERSISTENCE
   "How do I load and store the required data?"
```

Then connect them:

```text
                    SPRING IoC
                        │
                        ▼
HTTP
 │
 ▼
Security Filter Chain
 │
 ├── Authentication
 └── Authorization
 │
 ▼
Controller
 │
 ▼
Request DTO
 │
 ▼
Application Service / Use Case
 │
 ├── @Transactional
 │
 ▼
Domain
 │
 ▼
Repository
 │
 ▼
Spring Data JPA
 │
 ▼
JPA / EntityManager
 │
 ▼
Hibernate
 │
 ├── Persistence Context
 ├── First-Level Cache
 ├── Dirty Checking
 ├── Flush
 └── SQL Generation
 │
 ▼
JDBC
 │
 ▼
Database
```

If you understand this flow, most of the Spring Boot concepts stop being isolated annotations and become parts of one coherent system.

```
```
