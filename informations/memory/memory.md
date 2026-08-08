# JVM Memory Model

## 1. Stack / Thread Stack

The **stack** is a private memory area associated with each thread.

Every time a method is called, the JVM creates a **stack frame** for that method. The frame contains information such as:

* Local variables
* Method parameters
* Intermediate values
* References to objects
* Return information needed when the method finishes

Stack frames follow a **LIFO (Last-In, First-Out)** principle.

Because stack allocation and deallocation are very simple operations, the stack is extremely fast.

The stack has a limited size. If a program creates too many nested method calls, typically through infinite recursion, the JVM throws a:

```text
StackOverflowError
```

For example:

```java
void recursiveMethod() {
    recursiveMethod();
}
```

### Important distinction

The **reference to an object** can be stored in a stack frame, while the **object itself is normally stored on the heap**.

---

# 2. Heap Memory

The **heap** is the main memory area used by the JVM to store objects and arrays.

Unlike the stack, the heap is **shared between all threads**.

For example:

```java
User user = new User();
```

The `User` object is normally allocated on the heap.

The heap is managed by the **Garbage Collector (GC)**.

The generational model is based on the **Weak Generational Hypothesis**:

> Most objects die young, while objects that survive for a long time are more likely to remain alive.

This is why the heap can be conceptually divided into a **Young Generation** and an **Old Generation**.

---

### Young Generation

The **Young Generation** is where newly allocated objects are initially placed.

In the traditional generational model, it is divided into:

```text
Young Generation
┌─────────────────────────────────┐
│ Eden │ Survivor S0 │ Survivor S1│
└─────────────────────────────────┘
```

Most new objects are created in **Eden**.

For example:

```java
User user = new User();
```

The `User` object will typically be allocated in Eden.

When a **minor GC** occurs, objects that are still alive are copied/promoted through the Survivor spaces.

Conceptually:

```text
Eden
  │
  │ Minor GC
  ▼
Survivor S0
  │
  │ Minor GC
  ▼
Survivor S1
  │
  │ Minor GC
  ▼
...
  │
  │ reaches tenuring threshold
  ▼
Old Generation
```

The two Survivor spaces alternate roles. One is generally the **From** space and the other the **To** space during a collection.

### Important nuance

Modern garbage collectors do not all use this exact Eden/S0/S1 organization.

For example, **G1** uses regions rather than a simple contiguous Young/Old layout.

Therefore, the Eden/Survivor model is useful for understanding the **traditional generational design**, but it should not be treated as the physical layout of every modern JVM.

---

### Old Generation / Tenured Generation

The **Old Generation** contains objects that have survived enough garbage collections to be considered long-lived.

For example, an application may create millions of temporary objects:

```java
for (...) {
    User user = new User();
}
```

Most of these objects become unreachable very quickly.

By contrast, objects such as:

```text
ApplicationContext
DatabaseConnectionPool
Cache
Configuration
```

may remain alive for most or all of the application's lifetime.

Long-lived objects can eventually be promoted to the **Old Generation**.

The exact promotion mechanism depends on the garbage collector.

---

# 3. Metaspace

Since **Java 8**, **Metaspace** replaced the old **PermGen (Permanent Generation)**.

Metaspace is located in **native memory**, outside the Java heap.

It primarily contains **class metadata**, such as information about loaded classes and their structure.

For example:

```java
class User {

    private String name;

    public void login() {
        // ...
    }
}
```

When the JVM loads the `User` class, it needs metadata describing that class.

That information is associated with Metaspace.

A simplified view of JVM memory is:

```text
JVM Memory
│
├── Heap
│   ├── Young Generation
│   └── Old Generation
│
├── Thread Stacks
│
└── Native Memory
    ├── Metaspace
    ├── Code Cache
    └── Other native allocations
```

### Important nuance

It is slightly inaccurate to say that the **"Constant Pool" is simply stored in Metaspace**.

The JVM specification distinguishes the class-file constant pool from the **runtime constant pool**, and the exact implementation details depend on the JVM.

Metaspace can grow dynamically, although it can be constrained with JVM options such as:

```text
-XX:MaxMetaspaceSize
```

---

# 4. Code Cache

The **Code Cache** is another area of native memory.

It is primarily used by the **JIT (Just-In-Time) compiler** to store machine code generated from frequently executed Java bytecode.

---

# 5. Off-Heap Memory / Direct ByteBuffers

**Off-heap memory** refers broadly to memory allocated outside the Java heap.

One common example is:

```java
ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
```

With `allocateDirect()`, the actual buffer storage is allocated outside the normal Java heap.

This can be useful for high-performance I/O.

Frameworks such as **Netty** can use direct buffers to interact efficiently with operating-system and network I/O.

The idea is to reduce unnecessary copying between different memory areas.

### Important nuance

It is misleading to say that direct buffers are simply **"outside GC management"**.

The **buffer object itself is still a Java object and is subject to GC**.

What is outside the Java heap is the **native memory containing the actual buffer data**.

---

# 6. Summary

| Area                 | Shared?            | Main Purpose                              | GC?                                       |
| -------------------- | ------------------ | ----------------------------------------- | ----------------------------------------- |
| **Stack**            | No, one per thread | Method calls, local variables, references | No                                        |
| **Heap**             | Yes                | Objects and arrays                        | Yes                                       |
| **Young Generation** | Yes                | New objects                               | Yes                                       |
| **Old Generation**   | Yes                | Long-lived objects                        | Yes                                       |
| **Metaspace**        | JVM-wide           | Class metadata                            | Managed/reclaimed as classes are unloaded |
| **Code Cache**       | JVM-wide           | JIT-compiled native code                  | Not normal object GC                      |
| **Direct Memory**    | Can be shared      | Native buffers / I/O                      | Outside heap GC                           |

---

# 10. Key Interview Answer

A good way to explain the JVM memory model :

> **"Each thread has its own stack containing stack frames, local primitive values, and object references. Objects and arrays are normally allocated on the heap, which is shared between threads and managed by the garbage collector. The heap can be organized generationally, with newly created objects generally starting in the Young Generation and long-lived objects eventually moving to the Old Generation.**
>
> **Outside the Java heap, the JVM also uses native memory areas such as Metaspace for class metadata and the Code Cache for JIT-compiled machine code. Direct ByteBuffers can also allocate their actual data storage in native memory, which is particularly useful for high-performance I/O.**
>
> **The exact memory layout depends on the garbage collector and JVM implementation, so the traditional Eden/Survivor/Old Generation model should be understood as a conceptual model rather than a universal physical layout."**
