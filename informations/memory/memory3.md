# The Trade-off Between Throughput and Latency

The **throughput vs. latency trade-off** is fundamental to understanding how to choose a Garbage Collector and, more broadly, how to design a performant system.

## 1. The Two Concepts

### Throughput

**Throughput** measures how much useful work an application can perform over a given period of time.

For example:

> "My application processes 100,000 requests per second."

The higher the throughput, the more work the system can accomplish.

For a Java server, we can measure throughput using:

* Requests per second
* Transactions per second
* Messages processed per second
* Tasks executed per second

---

### Latency

**Latency** measures how long it takes for an operation to produce a result.

For example:

```text
Request → Processing → Response

           15 ms
```

The request therefore has a latency of 15 ms.

For a web application, we often look at **percentiles**:

```text
p50 = 10 ms
p95 = 25 ms
p99 = 80 ms
```

The **p99** means that 99% of requests have a latency lower than or equal to 80 ms.

This is particularly important because an application can have an excellent average latency but still experience **occasional very long pauses**.

---

# 2. Where Does the Garbage Collector Come In?

The GC consumes CPU resources and can sometimes temporarily stop application threads through a **Stop-The-World pause**.

Imagine:

```text
Application

██████████████████      ███████████████████
                    ↑
                 GC pause
                    │
                   200 ms
```

During those 200 ms, application threads may be stopped.

This therefore increases **latency**.

However, there is another problem: if we ask the GC to be extremely fast and minimize pauses, it generally needs to use more resources and perform more work concurrently.

This can reduce the **throughput** available to the application.

This is where the trade-off comes in.

---

# 3. A Simple Example

Imagine two Garbage Collectors.

## GC A — Throughput-Oriented

It lets the application run for a long time and then performs a large collection:

```text
Application
████████████████████████████████
                              │
                              ▼
                           GC 500 ms
```

Result:

* Excellent overall throughput
* But a request may be blocked for 500 ms
* Poor latency
* Particularly bad for interactive applications

---

## GC B — Latency-Oriented

It continuously performs GC work in the background:

```text
Application
████████████████████████████████████████

GC
  ███  ███  ███  ███  ███  ███  ███
```

Result:

* Better latency
* More consistent response times
* But more CPU consumed by the GC
* Potentially less CPU available for the application
* Therefore potentially lower throughput

---

# 4. Why Can't We Maximize Both?

Because resources are limited.

Suppose your server has **8 CPU cores**.

You might have something like:

```text
                    8 CPU cores
                         │
             ┌───────────┴───────────┐
             │                       │
       Application                  GC
             │                       │
          6 cores                  2 cores
```

If you want to drastically reduce GC pauses, you can increase the amount of concurrent GC work:

```text
             8 CPU cores
                  │
        ┌─────────┴─────────┐
        │                   │
   Application             GC
      4 cores             4 cores
```

The GC can now work more aggressively in parallel.

However, only **4 cores remain available for the application**.

Therefore:

> **Reducing GC latency can come at the cost of throughput.**

---

# 5. Parallel GC vs. G1 vs. ZGC

This is one of the reasons why different garbage collectors exist.

## Parallel GC

Parallel GC generally prioritizes **throughput**.

```text
Throughput       ██████████
Latency          █████
```

It accepts relatively long STW pauses in order to maximize the amount of useful application work performed over a given period.

It can therefore be suitable for:

* Batch processing
* Scientific computing
* Data processing
* Applications where pauses of a few hundred milliseconds are not critical

---

## G1

G1 aims for more of a **balance**.

```text
Throughput       █████████
Latency          ████████
```

It allows you to define a pause-time target such as:

```text
-XX:MaxGCPauseMillis=200
```

This tells the GC:

> "Try to keep GC pauses around 200 ms."

However, this is **not a guarantee**.

G1 therefore tries to balance:

**throughput ↔ latency**

This is one of the reasons why it is considered a general-purpose garbage collector.

---

## ZGC / Shenandoah

ZGC and Shenandoah strongly prioritize **latency**.

```text
Throughput       ████████
Latency          ██████████
```

They perform a large part of their GC work **concurrently with application threads**.

The goal is to avoid situations such as:

```text
Application
██████████████████████

              █████████████
              GC 500 ms
```

and move closer to:

```text
Application
████████████████████████████████

GC
  ███ ███ ███ ███ ███ ███ ███
```

GC pauses become extremely short, but the GC consumes more resources while running concurrently.

---

# 6. The Real Issue: Consistency

For a modern application, **the average latency is not always the most important metric**.

Imagine two servers.

### Server A

```text
Average latency = 10 ms
p99 = 500 ms
```

### Server B

```text
Average latency = 15 ms
p99 = 30 ms
```

Server A has a better average latency.

However, Server B may provide a much better user experience.

Why?

Because Server A occasionally experiences very large latency spikes.

This is why low-latency systems pay close attention to:

* p95
* p99
* p99.9
* Maximum latency

These are often referred to as **tail latencies**.

---

# 7. The Trade-off in a Nutshell

```text
                 THROUGHPUT
                     ▲
                     │
                     │     Parallel GC
                     │        ●
                     │
                     │          G1
                     │           ●
                     │
                     │
                     │                ZGC
                     │                 ●
                     │
                     └────────────────────────►
                                           LATENCY
```

This graph is intentionally conceptual. It does not mean that these collectors always have exactly these performance characteristics.

The key idea is:

> **Throughput = how much work the system can accomplish.**
>
> **Latency = how quickly an individual operation gets a response.**

For Garbage Collection:

> **A throughput-oriented GC tries to maximize the CPU time available to the application, even if that means accepting longer pauses.**
>
> **A latency-oriented GC tries to minimize pauses and make response times more predictable, even if that means consuming more CPU resources to perform GC work concurrently.**

---

# 8. The Architectural Question

The question is therefore not:

> **"Which Garbage Collector is the fastest?"**

The better question is:

> **"Does my application prioritize maximum overall throughput, or short and predictable response times?"**

A **batch processing application** will often prioritize throughput.

An **interactive banking API**, a trading system, or a real-time service will generally place much more importance on latency and, especially, on **tail latency** such as `p99` and `p99.9`.

---

# Key Interview Answer

A strong way to explain this in a Java interview is:

> **"Throughput measures how much work an application can perform over a given period of time, while latency measures how long an individual operation takes to complete.**
>
> **Garbage collection creates a trade-off between the two. A throughput-oriented collector can use more aggressive or longer GC phases to maximize the CPU time available for application work. A latency-oriented collector performs more GC work concurrently in order to minimize Stop-The-World pauses, but this consumes CPU resources that could otherwise be used by the application.**
>
> **For example, Parallel GC generally prioritizes throughput, G1 provides a balance between throughput and predictable pause times, while ZGC and Shenandoah prioritize very low latency.**
>
> **In latency-sensitive systems, average latency is not enough. We also care about tail latency, such as p99 or p99.9, because occasional long GC pauses can have a significant impact on user experience and system reliability."**
