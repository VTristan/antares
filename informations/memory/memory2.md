# 1.2. Garbage Collection Mechanics and Algorithms

The **Garbage Collector (GC)** is responsible for identifying and reclaiming objects that are no longer reachable by the application.

The JVM starts from a set of special references called **GC Roots** and traverses the object graph. Any object that cannot be reached from a GC Root is considered **unreachable** and can eventually be reclaimed.

Typical GC Roots include:

* Active threads
* Local variables and references stored in thread stacks
* Static fields of loaded classes
* JNI references
* Other JVM-internal references

---

## Mark-Sweep-Compact

A traditional garbage collection process can be described using three conceptual phases:

### 1. Mark

The GC traverses the object graph starting from the GC Roots and marks all reachable objects.

```text
GC Roots
   │
   ▼
Object A ───► Object B
   │
   ▼
Object C

Object D     ← unreachable
```

Objects A, B, and C are marked as alive.

`Object D` is not reachable from any GC Root, so the garbage collector can eventually reclaim its memory.

---

### 2. Sweep

The GC identifies objects that were not marked as reachable and reclaims their memory.

```text
Before Sweep:

[A] [B] [D] [C] [D] [E]
     ↑       ↑       ↑
   alive   alive   alive

[D] = unreachable
```

The memory occupied by the unreachable objects can now be reused.

---

### 3. Compact

The GC may move surviving objects together in order to reduce memory fragmentation.

```text
Before:

[A] [B] [free] [C] [free] [E]

After Compaction:

[A] [B] [C] [E] [free] [free]
```

This makes future allocations more efficient by creating larger contiguous areas of free memory.

### Important nuance

**Mark-Sweep-Compact is a conceptual model, not a description of exactly how every modern garbage collector works.**

Modern collectors such as **G1, ZGC, and Shenandoah** use different algorithms and combinations of concurrent marking, evacuation, barriers, regions, and compaction.

---

# Stop-The-World (STW)

A **Stop-The-World pause** occurs when the JVM temporarily stops application threads so that the garbage collector can safely perform certain operations.

Conceptually:

```text
Application threads:

██████████████████   █████████████████
                   ↑
                   │
                STW pause
                   │
              GC activity
```

During the pause, application threads do not execute Java application code.

Modern garbage collectors try to minimize the duration of STW pauses by performing as much work as possible **concurrently** with application threads.

---

# Garbage Collector Comparison

| Garbage Collector | Execution Model              | Typical Use Case                                | Latency Characteristics                |
| ----------------- | ---------------------------- | ----------------------------------------------- | -------------------------------------- |
| **Serial GC**     | Single-threaded              | Small applications, lightweight containers      | Higher STW pauses                      |
| **Parallel GC**   | Multi-threaded               | Throughput-oriented workloads, batch processing | Higher pauses but high throughput      |
| **G1 GC**         | Regional / Concurrent        | General-purpose applications                    | Designed for predictable pause targets |
| **ZGC**           | Highly concurrent / Scalable | Very large heaps and low-latency applications   | Extremely short STW pauses             |
| **Shenandoah GC** | Concurrent Mark & Compact    | Low-latency applications                        | Performs most GC work concurrently     |

---

# Serial GC

The **Serial Garbage Collector** performs garbage collection using a single GC thread.

It is simple and has relatively low overhead.

It can be appropriate for:

* Small applications
* Small heaps
* Lightweight containers
* Environments where simplicity is more important than latency

However, because collection is performed by a single thread and application threads may be stopped for relatively long periods, it is generally not the preferred choice for large production workloads.

---

# Parallel GC

The **Parallel Garbage Collector** uses multiple GC threads.

Its primary goal is **high throughput** rather than extremely low latency.

It is particularly suitable for workloads such as:

* Batch processing
* CPU-intensive applications
* Large computational workloads
* Applications where maximum throughput matters more than response-time consistency

The trade-off is that STW pauses can be significant.

In other words:

> **Parallel GC optimizes for throughput, while low-latency collectors optimize for pause times.**

---

# G1 GC — Garbage-First

**G1 (Garbage-First) GC** is a region-based garbage collector designed for general-purpose applications.

It has been the **default garbage collector since Java 9**.

Instead of treating the heap as a few large contiguous generations, G1 divides the heap into many equally sized **regions**.

Conceptually:

```text
Heap
┌────┬────┬────┬────┬────┬────┐
│ R1 │ R2 │ R3 │ R4 │ R5 │ R6 │
├────┼────┼────┼────┼────┼────┤
│ R7 │ R8 │ R9 │ R10│ R11│ R12│
└────┴────┴────┴────┴────┴────┘
```

Regions can be used for different purposes, including:

* Eden
* Survivor
* Old
* Humongous objects

G1 attempts to collect regions that offer the most garbage for the least amount of work.

This is where the name **Garbage-First** comes from.

---

## G1 Pause-Time Goal

G1 allows the developer to specify a target maximum pause time, for example:

```text
-XX:MaxGCPauseMillis=200
```

This means that the application is asking G1 to **try to keep GC pauses around or below 200 ms**.

It is important to say **target**, not **guarantee**.

The JVM cannot guarantee that every pause will remain below that value.

A good interview formulation is:

> **"G1 is a region-based garbage collector designed to provide predictable pause-time behavior. It uses concurrent processing and tries to meet a configurable pause-time goal."**

---

# ZGC — Z Garbage Collector

**ZGC** is designed for **very low-latency garbage collection**, even with very large heaps.

Its major characteristic is that most of its work happens **concurrently with application threads**.

The goal is to keep STW pauses extremely short.

Modern ZGC is designed to keep pauses in the **sub-millisecond range in many situations**, rather than having pauses proportional to the size of the heap.

ZGC uses techniques such as:

* Colored pointers
* Load barriers
* Concurrent marking
* Concurrent relocation
* Region-based memory management

Conceptually:

```text
Application Threads
██████████████████████████████████████

GC Threads
    ██████████████████████████████████

        Mostly concurrent
```

This makes ZGC particularly interesting for:

* Large heaps
* Low-latency services
* Large backend systems
* Applications where long GC pauses are unacceptable

---

# Colored Pointers

One of ZGC's important techniques is the use of **colored pointers**.

A pointer contains additional metadata that helps the garbage collector determine information about the referenced object.

Conceptually:

```text
64-bit reference
┌───────────────┬────────────────────────────┐
│ GC metadata   │       Object address       │
└───────────────┴────────────────────────────┘
```

This allows ZGC to perform some GC-related operations efficiently while application threads are still running.

---

# Load Barriers

ZGC also uses **load barriers**.

A load barrier is additional logic executed when an object reference is loaded.

Conceptually:

```text
Application
     │
     ▼
Load object reference
     │
     ▼
Load Barrier
     │
     ▼
Check / fix reference if necessary
     │
     ▼
Use object
```

This helps ZGC coordinate concurrent garbage collection with application execution.

---

# Shenandoah GC

**Shenandoah** is another low-latency garbage collector.

Its key characteristic is that it performs much of the garbage collection process **concurrently with application threads**.

In particular, Shenandoah can perform object relocation and compaction concurrently.

This helps reduce long STW pauses even when the heap becomes very large.

Conceptually:

```text
Application Threads
██████████████████████████████████

GC Threads
██████████████████████████████████

       Concurrent marking
       Concurrent evacuation
       Concurrent compaction
```

Shenandoah is therefore particularly suited to applications where predictable low latency is more important than maximum throughput.

---

# GC Strategy: Throughput vs Latency

A useful way to understand the main collectors is to think in terms of a trade-off:

```text
                 GC Strategy

       High Throughput (débit)   Low Latency (temps de réponse)
              │                     │
              ▼                     ▼
         Parallel GC          ZGC / Shenandoah
              │                     │
              └─────── G1 ──────────┘
                 General Purpose
```

A simplified interpretation is:

* **Serial GC** → simplicity
* **Parallel GC** → throughput
* **G1** → general purpose / predictable pauses
* **ZGC** → extremely low latency
* **Shenandoah** → extremely low latency

The choice depends on the application's requirements rather than simply choosing the "fastest" collector.

---

# 1.3. Diagnosing and Troubleshooting OutOfMemoryError

An **OutOfMemoryError (OOM)** occurs when the JVM cannot satisfy a memory allocation request.

There are several different types of OOM, and the exact message is extremely useful when diagnosing the problem.

---

# `java.lang.OutOfMemoryError: Java heap space`

This means that the JVM could not allocate an object in the Java heap.

Possible causes include:

* Too many live objects
* A memory leak
* An undersized heap
* Very large objects
* Unbounded collections
* Static collections retaining objects
* Listeners that are never removed
* Caches growing indefinitely

For example:

```java
private static final List<User> users = new ArrayList<>();

public void addUser(User user) {
    users.add(user);
}
```

Because the list is static and keeps references to the `User` objects, those objects remain reachable and cannot be garbage-collected.

The heap can eventually become full.

Possible solutions include:

* Fixing the memory leak
* Limiting collection growth
* Configuring caches with eviction policies
* Reducing object retention
* Increasing the heap size when appropriate

For example:

```text
-Xmx4g
```

sets the maximum heap size to approximately 4 GB.

### Important point

Increasing `-Xmx` is **not necessarily a solution**.

If the application contains a memory leak, increasing the heap may only delay the OOM.

---

# `java.lang.OutOfMemoryError: Metaspace`

This error means that the JVM cannot allocate enough memory for class metadata in Metaspace.

Possible causes include:

* Excessive dynamic class generation
* Large numbers of generated proxies
* ClassLoader leaks
* Applications repeatedly loading classes without allowing their ClassLoaders to be unloaded
* Frameworks generating excessive numbers of classes

A possible configuration is:

```text
-XX:MaxMetaspaceSize=512m
```

However, increasing the limit does not necessarily fix the underlying problem.

If the real problem is a **ClassLoader leak**, the correct solution is to identify why classes remain reachable and cannot be unloaded.

---

# `java.lang.OutOfMemoryError: GC overhead limit exceeded`

This error indicates that the JVM is spending an excessive amount of time performing garbage collection while recovering very little memory.

The traditional threshold is approximately:

> The JVM spends more than 98% of its time performing GC while recovering less than 2% of the heap.

Conceptually:

```text
Application execution
██

Garbage Collection
████████████████████████████████████████████████

Very little memory recovered
```

This usually indicates that the application is under severe memory pressure.

Possible causes include:

* Memory leaks
* Excessive object creation
* An undersized heap
* Large numbers of long-lived objects

---

# `java.lang.OutOfMemoryError: Unable to create new native thread`

This error is different from `Java heap space`.

It means that the JVM or operating system could not create another native thread.

Possible causes include:

* Too many threads
* Excessive thread creation
* Operating-system limits
* Insufficient native memory
* Excessively large thread stacks

On Linux, operating-system limits can include:

```text
ulimit -u
```

which controls the maximum number of processes/threads available to a user, depending on the configuration.

For example, creating an unbounded number of threads is dangerous:

```java
while (true) {
    new Thread(() -> {
        // ...
    }).start();
}
```

A better approach is generally to use a bounded **ExecutorService** or another managed concurrency mechanism.

---

# Diagnosing Memory Problems

When investigating an OOM or memory-related problem, several tools are particularly useful.

---

## 1. Heap Dump

A **heap dump** is a snapshot of objects currently present in the Java heap.

The JVM can automatically generate a heap dump when an OOM occurs:

```text
-XX:+HeapDumpOnOutOfMemoryError
```

You can also specify where the dump should be written:

```text
-XX:HeapDumpPath=/path/to/dump.hprof
```

A heap dump can help answer questions such as:

* Which objects consume the most memory?
* Which collections are growing?
* Why are objects still reachable?
* Which references are preventing garbage collection?
* Is there a memory leak?

---

# 2. Eclipse MAT

**Eclipse MAT (Memory Analyzer Tool)** is a powerful tool for analyzing Java heap dumps.

It can help identify:

* Dominator trees
* Large retained objects
* Memory leaks
* Reference chains
* Objects preventing other objects from being collected

One particularly useful concept is **retained size**.

Suppose:

```text
Cache
  │
  ├── User A
  ├── User B
  ├── User C
  └── User D
```

If the cache retains 500 MB of objects, the cache may have a very large retained size.

This can help identify the root cause of a memory leak.

---

# 3. VisualVM

**VisualVM** provides a graphical interface for monitoring and analyzing Java applications.

It can be used to inspect:

* Heap usage
* CPU usage
* Threads
* Garbage collection activity
* Memory behavior
* Heap dumps

It is particularly useful for interactive investigation and development environments.

---

# 4. JDK Flight Recorder (JFR)

**JDK Flight Recorder (JFR)** is a low-overhead profiling and diagnostic framework included with the JDK.

It can collect information about:

* Garbage collection
* Allocation activity
* CPU usage
* Threads
* Locks
* I/O
* Exceptions
* JVM behavior

JFR is especially useful for investigating performance problems in production because its overhead is designed to be relatively low.

---

# 5. JDK Mission Control (JMC)

**JDK Mission Control (JMC)** is a tool used to analyze data produced by JFR.

Conceptually:

```text
Java Application
       │
       ▼
JDK Flight Recorder
       │
       │ recording
       ▼
JFR file
       │
       ▼
JDK Mission Control
       │
       ▼
Performance analysis
```

For example, you can use JFR/JMC to investigate:

```text
High CPU
   │
   ├── Which threads?
   │
   ├── Which methods?
   │
   └── Which code paths?

High GC activity
   │
   ├── Allocation rate?
   ├── GC frequency?
   ├── Pause duration?
   └── Heap occupancy?
```

---

# Memory Troubleshooting Workflow

A practical approach to an OOM problem is:

```text
          OutOfMemoryError
                 │
                 ▼
       Identify the OOM type
                 │
        ┌────────┼────────┐
        ▼        ▼        ▼
      Heap    Metaspace  Native
        │        │        │
        ▼        ▼        ▼
   Heap Dump   Class     Threads /
               Loading   Native Memory
        │
        ▼
   Analyze with
   Eclipse MAT
        │
        ▼
 Find retained objects
        │
        ▼
 Identify reference chain
        │
        ▼
 Fix root cause
```

For performance and GC problems, JFR/JMC are particularly useful:

```text
Application
     │
     ▼
    JFR
     │
     ▼
   JMC
     │
     ├── GC pauses
     ├── Allocation rate
     ├── CPU
     ├── Threads
     ├── Locks
     └── I/O
```

---

# Key Interview Answer

A strong answer to the question **"How does Java Garbage Collection work?"** would be:

> **"The Garbage Collector identifies objects that are no longer reachable from GC Roots and reclaims their memory. GC Roots include active threads, references from thread stacks, static fields of loaded classes, and JNI references.**
>
> **Traditional garbage collection can be described using mark, sweep, and compact phases, although modern collectors use more sophisticated algorithms. Some GC operations require Stop-The-World pauses, while modern collectors try to perform most of their work concurrently with application threads.**
>
> **Serial GC is simple and suitable for small applications, Parallel GC prioritizes throughput, G1 provides a good general-purpose balance with configurable pause-time goals, while ZGC and Shenandoah focus on extremely low latency and perform most GC work concurrently.**
>
> **When diagnosing memory problems, I would first identify the type of OutOfMemoryError. For heap problems, I would generate a heap dump and analyze it with Eclipse MAT. For GC and performance issues, I would use JFR and JDK Mission Control to analyze allocation rates, GC pauses, CPU usage, threads, and other JVM events."**
