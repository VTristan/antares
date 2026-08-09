Below is a **Java 25-focused reference**, including the modern `Sequenced*` interfaces introduced in Java 21, `Navigable*`, concurrent collections, immutable/unmodifiable collections, legacy collections, implementation choices, complexity, and practical examples. Oracle's Java 25 documentation confirms the current Collections Framework hierarchy and implementations. ([Oracle Documentation][1])

# Java 25 Collections Framework — Complete Guide

> A practical and conceptual guide to `Collection`, `List`, `Set`, `Map`, `Queue`, `Deque`, `Tree`, `Hash`, `Sequenced*`, `Navigable*`, concurrent collections, immutable collections, and the main Java 25 implementations.

---

# 1. The Big Picture

The Java Collections Framework provides standard interfaces and implementations for representing groups of objects.

The most important interfaces are:

```text
                           Iterable
                              │
                              ▼
                         Collection
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
         List                Set                Queue
          │                   │                   │
          │              ┌────┴────┐              │
          │              │         │              │
          │         SortedSet   SequencedSet      │
          │              │         │              │
          │         NavigableSet  │              Deque
          │                        │              │
          │                        │              │
          └──────────────┐         │              │
                         │         │              │
                  SequencedCollection ◄───────────┘
```

`Map` is separate:

```text
                            Map
                             │
                 ┌───────────┴───────────┐
                 │                       │
             SortedMap               SequencedMap
                 │                       │
           NavigableMap              LinkedHashMap
                 │
          TreeMap / ...
```

More precisely, in Java 25:

```text
Collection
├── SequencedCollection
│   ├── List
│   └── Deque
│
├── Set
│   ├── SequencedSet
│   │   └── LinkedHashSet
│   │
│   └── SortedSet
│       └── NavigableSet
│
└── Queue
    └── Deque
```

And:

```text
Map
├── SequencedMap
│   └── LinkedHashMap
│
└── SortedMap
    └── NavigableMap
```

Java 21 introduced `SequencedCollection`, `SequencedSet`, and `SequencedMap`; these remain part of the Java 25 Collections Framework. `List` and `Deque` are `SequencedCollection`s, `LinkedHashSet` is a `SequencedSet`, and `LinkedHashMap` is a `SequencedMap`. ([Oracle Documentation][2])

---

# 2. `Collection`

```java
Collection<String> collection;
```

`Collection` represents a group of objects.

It does **not** specify:

* whether elements are ordered
* whether duplicates are allowed
* how elements are stored
* whether the collection is mutable

For example:

```java
Collection<String> names = new ArrayList<>();

names.add("Alice");
names.add("Bob");
names.add("Alice");
```

The `Collection` interface provides operations such as:

```java
add()
addAll()
remove()
removeAll()
contains()
containsAll()
size()
isEmpty()
clear()
iterator()
stream()
toArray()
```

The interface is intentionally generic.

If you need more specific guarantees, use a more specific interface:

```text
Need...

ordered sequence
    → List

unique elements
    → Set

FIFO / processing order
    → Queue

double-ended access
    → Deque

key → value
    → Map
```

---

# 3. `List`

A `List` is an **ordered collection** with positional/index-based access.

```java
List<String> names = new ArrayList<>();

names.add("Alice");
names.add("Bob");
names.add("Charlie");
```

You can access elements by index:

```java
names.get(0);       // Alice
names.get(1);       // Bob

names.set(1, "John");
```

Duplicates are normally allowed:

```java
List<String> names =
    List.of("Alice", "Bob", "Alice");
```

Result:

```text
[ Alice, Bob, Alice ]
          ↑
      duplicate
```

### Main properties

| Property              | `List`                             |
| --------------------- | ---------------------------------- |
| Ordered               | Yes                                |
| Index-based access    | Yes                                |
| Duplicates            | Usually yes                        |
| `null`                | Implementation-dependent           |
| First/last operations | Yes, through `SequencedCollection` |
| Reverse view          | Yes                                |

Since Java 21, `List` extends `SequencedCollection`. ([Oracle Documentation][3])

---

# 4. `ArrayList`

`ArrayList` is the general-purpose `List` implementation.

```java
List<String> names = new ArrayList<>();
```

Conceptually:

```text
index
  0       1       2       3
┌───────┬───────┬───────┬───────┐
│ Alice │  Bob  │  Tom  │ John  │
└───────┴───────┴───────┴───────┘
```

Internally it uses a dynamically resized array.

## Complexity

| Operation             | Typical complexity |
| --------------------- | -----------------: |
| `get(index)`          |               O(1) |
| `set(index)`          |               O(1) |
| `add(element)`        |     O(1) amortized |
| `add(index, element)` |               O(n) |
| `remove(index)`       |               O(n) |
| `contains()`          |               O(n) |

Why is `add(index, element)` O(n)?

Because elements may need to be shifted:

```text
Before:

[A] [B] [C] [D]

insert X at index 1

[A] [X] [B] [C] [D]
      ↑
   elements shifted
```

## Default choice

In most cases:

```java
List<T> list = new ArrayList<>();
```

is the right starting point.

Use `ArrayList` when you need:

* fast random access
* iteration
* a general-purpose list
* frequent reads
* occasional structural modifications

---

# 5. `LinkedList`

`LinkedList` is a doubly linked list.

Conceptually:

```text
[A] ⇄ [B] ⇄ [C] ⇄ [D]
```

Each node contains references to neighboring nodes.

Unlike `ArrayList`:

```java
list.get(500_000);
```

requires traversal and is O(n).

## Complexity

| Operation       | Typical complexity |
| --------------- | -----------------: |
| `get(index)`    |               O(n) |
| `set(index)`    |               O(n) |
| `addFirst()`    |               O(1) |
| `addLast()`     |               O(1) |
| `removeFirst()` |               O(1) |
| `removeLast()`  |               O(1) |
| `contains()`    |               O(n) |

`LinkedList` also implements `Deque`.

However:

> `LinkedList` is usually **not** the best choice for a queue or stack.

For queue/stack use cases, prefer:

```java
ArrayDeque
```

because it generally has better memory locality and lower per-element overhead.

---

# 6. `List` — `ArrayList` vs `LinkedList`

|                         | `ArrayList`     | `LinkedList`       |
| ----------------------- | --------------- | ------------------ |
| Internal structure      | Resizable array | Doubly linked list |
| `get(index)`            | O(1)            | O(n)               |
| First insertion/removal | O(n)            | O(1)               |
| Last insertion/removal  | O(1) amortized  | O(1)               |
| Memory locality         | Excellent       | Poorer             |
| Memory overhead         | Lower           | Higher             |
| General-purpose choice  | **Yes**         | Rarely             |

A common mistake is:

> "I have lots of insertions, therefore I should use `LinkedList`."

Not necessarily.

The location of the insertion matters, and real-world performance is also affected by CPU caches and memory locality.

---

# 7. `Set`

A `Set` contains **no duplicate elements**.

```java
Set<String> names = new HashSet<>();

names.add("Alice");
names.add("Bob");
names.add("Alice");
```

Result:

```text
Alice
Bob
```

The second `"Alice"` is ignored.

The `Set` interface itself does not necessarily define ordering.

There are three major families:

```text
HashSet
LinkedHashSet
TreeSet
```

---

# 8. `HashSet`

`HashSet` is based on hashing.

```java
Set<String> ids = new HashSet<>();

ids.add("A");
ids.add("B");
ids.add("C");
```

It does **not** guarantee iteration order.

### Typical complexity

| Operation    | Average |
| ------------ | ------: |
| `add()`      |    O(1) |
| `remove()`   |    O(1) |
| `contains()` |    O(1) |

Use it when you primarily need:

> **Uniqueness + fast lookup**

Example:

```java
Set<Long> blockedUserIds = new HashSet<>();

if (blockedUserIds.contains(userId)) {
    // blocked
}
```

---

# 9. `LinkedHashSet`

`LinkedHashSet` combines:

```text
HashSet
+
insertion/encounter order
```

```java
Set<String> names = new LinkedHashSet<>();

names.add("Alice");
names.add("Bob");
names.add("Charlie");
```

Iteration:

```text
Alice
Bob
Charlie
```

It preserves encounter order.

Since Java 21, `LinkedHashSet` implements `SequencedSet`, giving it APIs such as:

```java
set.getFirst();
set.getLast();
set.addFirst(...);
set.addLast(...);
set.reversed();
```

The sequenced APIs were introduced to make order-aware operations consistent across the Collections Framework. ([Oracle Documentation][4])

Use it when you need:

> **Uniqueness + predictable insertion order**

---

# 10. `TreeSet`

`TreeSet` stores unique elements in sorted order.

```java
Set<Integer> numbers = new TreeSet<>();

numbers.add(50);
numbers.add(10);
numbers.add(30);
```

Iteration:

```text
10
30
50
```

It is based on a balanced tree.

Typical implementation characteristics are those of a Red-Black tree.

### Complexity

| Operation    |                                               Complexity |
| ------------ | -------------------------------------------------------: |
| `add()`      |                                                 O(log n) |
| `remove()`   |                                                 O(log n) |
| `contains()` |                                                 O(log n) |
| first/last   | O(log n) or better depending on operation/implementation |

Example:

```java
NavigableSet<Integer> numbers =
    new TreeSet<>();

numbers.add(10);
numbers.add(20);
numbers.add(30);
numbers.add(40);
```

Now you can perform navigation:

```java
numbers.lower(25);   // 20
numbers.floor(20);   // 20
numbers.ceiling(25); // 30
numbers.higher(30);  // 40
```

---

# 11. `SortedSet`

`SortedSet` is an interface expressing that elements have a total ordering.

```java
SortedSet<Integer> numbers =
    new TreeSet<>();
```

It provides:

```java
first()
last()
comparator()
headSet()
tailSet()
subSet()
```

Conceptually:

```text
SortedSet

10 ── 20 ── 30 ── 40 ── 50
```

---

# 12. `NavigableSet`

`NavigableSet` extends `SortedSet`.

It adds navigation operations:

```java
lower()
floor()
ceiling()
higher()
```

and reverse/range operations:

```java
descendingSet()
headSet()
tailSet()
subSet()
```

Example:

```java
NavigableSet<Integer> numbers =
    new TreeSet<>(List.of(10, 20, 30, 40, 50));

numbers.lower(30);    // 20
numbers.floor(30);    // 30
numbers.ceiling(35);  // 40
numbers.higher(40);   // 50
```

The mental model is:

```text
SortedSet
    ↓
"Give me sorted elements"

NavigableSet
    ↓
"Give me sorted elements
 and help me navigate around them"
```

---

# 13. Comparing the Sets

|                 | `HashSet` | `LinkedHashSet` | `TreeSet`                |
| --------------- | --------- | --------------- | ------------------------ |
| Unique          | Yes       | Yes             | Yes                      |
| Hash-based      | Yes       | Yes             | No                       |
| Sorted          | No        | No              | Yes                      |
| Insertion order | No        | Yes             | No                       |
| Average lookup  | O(1)      | O(1)            | O(log n)                 |
| Navigation      | No        | No              | Yes                      |
| `SequencedSet`  | No        | Yes             | Yes, through `SortedSet` |

---

# 14. `Map`

A `Map` stores:

```text
key → value
```

Example:

```java
Map<String, Integer> ages =
    new HashMap<>();

ages.put("Alice", 30);
ages.put("Bob", 25);
```

Conceptually:

```text
Alice → 30
Bob   → 25
```

Keys are unique.

```java
ages.put("Alice", 30);
ages.put("Alice", 31);
```

Result:

```text
Alice → 31
```

The second `put()` replaces the old value.

Values may be duplicated:

```text
Alice → 30
Bob   → 30
```

is valid.

---

# 15. `HashMap`

`HashMap` is the general-purpose `Map` implementation.

```java
Map<String, User> users =
    new HashMap<>();
```

Typical complexity:

| Operation       | Average |
| --------------- | ------: |
| `get()`         |    O(1) |
| `put()`         |    O(1) |
| `remove()`      |    O(1) |
| `containsKey()` |    O(1) |

It does not guarantee iteration order.

---

# 16. How `HashMap` Works

Suppose:

```java
map.put("Alice", 42);
```

Conceptually:

```text
"Alice"
   │
   ▼
hashCode()
   │
   ▼
  hash
   │
   ▼
 bucket
   │
   ▼
"Alice" → 42
```

Later:

```java
map.get("Alice");
```

Java uses the key's hash to identify the relevant bucket.

This avoids scanning the entire map in the normal case.

---

# 17. Hash Collisions

Different keys can have the same hash.

```text
Key A ──→ hash X
Key B ──→ hash X
```

This is a **collision**.

A `HashMap` must therefore compare keys using `equals()` as well.

Conceptually:

```text
bucket
   │
   ├── Key A → Value A
   │
   └── Key B → Value B
```

Modern JDK implementations can use tree structures within heavily-collided buckets to improve worst-case behavior.

---

# 18. `equals()` and `hashCode()`

This is essential when using custom objects as keys.

If:

```java
a.equals(b)
```

is `true`, then:

```java
a.hashCode() == b.hashCode()
```

must also be true.

The reverse is not required:

```java
a.hashCode() == b.hashCode()
```

does **not** imply:

```java
a.equals(b)
```

Two unequal objects can have the same hash.

Example:

```java
record UserId(String value) {}
```

Records automatically provide appropriate `equals()` and `hashCode()` implementations based on their components, making them convenient as map keys.

---

# 19. Mutable Keys — Important Trap

Avoid mutable fields involved in `equals()` / `hashCode()` as map keys.

Bad example:

```java
class User {

    String id;

    // equals/hashCode use id
}
```

Suppose:

```java
Map<User, String> map =
    new HashMap<>();

User user = new User("123");

map.put(user, "Alice");
```

Then:

```java
user.id = "456";
```

The object may now effectively belong to a different hash bucket.

You can end up with:

```java
map.containsKey(user)
```

unexpectedly returning:

```text
false
```

General rule:

> Prefer immutable keys.

---

# 20. `LinkedHashMap`

`LinkedHashMap` combines:

```text
HashMap
+
predictable encounter order
```

```java
Map<String, Integer> map =
    new LinkedHashMap<>();

map.put("A", 1);
map.put("B", 2);
map.put("C", 3);
```

Iteration:

```text
A → 1
B → 2
C → 3
```

It is useful when you need:

> **Fast lookup + predictable order**

It can also maintain **access order**, which makes it useful for implementing LRU-style caches.

---

# 21. `SequencedMap`

Java 21 introduced `SequencedMap`.

Java 25 includes it.

```java
SequencedMap<K, V>
```

It represents a map with a well-defined encounter order and operations at both ends.

Important methods include:

```java
firstEntry()
lastEntry()

pollFirstEntry()
pollLastEntry()

putFirst()
putLast()

reversed()

sequencedKeySet()
sequencedValues()
sequencedEntrySet()
```

Example:

```java
SequencedMap<String, Integer> map =
    new LinkedHashMap<>();

map.put("A", 1);
map.put("B", 2);
map.put("C", 3);

map.firstEntry();  // A=1
map.lastEntry();   // C=3
```

Reverse view:

```java
map.reversed();
```

produces the reverse encounter-order view.

`SequencedMap` was added to solve a long-standing inconsistency in the Collections Framework around ordered collections and maps. ([Oracle Documentation][2])

---

# 22. `TreeMap`

`TreeMap` maintains its keys in sorted order.

```java
Map<String, Integer> map =
    new TreeMap<>();

map.put("Charlie", 30);
map.put("Alice", 20);
map.put("Bob", 25);
```

Iteration:

```text
Alice   → 20
Bob     → 25
Charlie → 30
```

Typical operations:

| Operation  | Complexity |
| ---------- | ---------: |
| `get()`    |   O(log n) |
| `put()`    |   O(log n) |
| `remove()` |   O(log n) |

Use it when you need:

> **Map + sorted keys + navigation/range operations**

---

# 23. `SortedMap`

`SortedMap` expresses a map whose keys have a total ordering.

```java
SortedMap<String, Integer> map =
    new TreeMap<>();
```

It provides:

```java
firstKey()
lastKey()

headMap()
tailMap()
subMap()

comparator()
```

---

# 24. `NavigableMap`

`NavigableMap` extends `SortedMap`.

It provides navigation around keys:

```java
lowerKey()
floorKey()
ceilingKey()
higherKey()
```

and entry equivalents:

```java
lowerEntry()
floorEntry()
ceilingEntry()
higherEntry()
```

Example:

```java
NavigableMap<Integer, String> map =
    new TreeMap<>();

map.put(10, "A");
map.put(20, "B");
map.put(30, "C");
map.put(40, "D");
```

Then:

```java
map.lowerKey(25);    // 20
map.floorKey(20);    // 20
map.ceilingKey(25);  // 30
map.higherKey(30);   // 40
```

---

# 25. `TreeMap` Range Queries

One of the main reasons to use a tree-based map is range navigation.

```java
map.subMap(20, 40);
```

Conceptually:

```text
10 ── 20 ── 30 ── 40
     └────────┘
        range
```

Other operations:

```java
map.headMap(30);
map.tailMap(30);
map.subMap(20, 40);
```

This makes `TreeMap` useful for:

* ranges
* ordered data
* nearest-key queries
* time-based indexes
* sorted configuration
* interval-like navigation

---

# 26. Comparing the Maps

|                 | `HashMap` | `LinkedHashMap` | `TreeMap`                |
| --------------- | --------- | --------------- | ------------------------ |
| Key/value       | Yes       | Yes             | Yes                      |
| Unique keys     | Yes       | Yes             | Yes                      |
| Hash-based      | Yes       | Yes             | No                       |
| Sorted keys     | No        | No              | Yes                      |
| Insertion order | No        | Yes             | No                       |
| Average lookup  | O(1)      | O(1)            | O(log n)                 |
| Range queries   | No        | No              | Yes                      |
| Navigation      | No        | Limited         | Yes                      |
| `SequencedMap`  | No        | Yes             | Yes, through `SortedMap` |

---

# 27. `Queue`

A `Queue` represents elements waiting to be processed.

The classic model is FIFO:

```text
First In
   ↓
[A] [B] [C]
 ↑
First Out
```

Example:

```java
Queue<String> queue =
    new ArrayDeque<>();

queue.offer("A");
queue.offer("B");
queue.offer("C");

queue.poll(); // A
```

---

# 28. Queue Methods

There are three pairs of methods:

| Operation | Throws exception | Special return value |
| --------- | ---------------- | -------------------- |
| Insert    | `add()`          | `offer()`            |
| Remove    | `remove()`       | `poll()`             |
| Inspect   | `element()`      | `peek()`             |

For example:

```java
queue.poll();
```

returns:

```text
null
```

if the queue is empty.

Whereas:

```java
queue.remove();
```

throws an exception when the queue is empty.

---

# 29. `Deque`

`Deque` means:

> Double Ended Queue

It supports operations at both ends.

```java
Deque<String> deque =
    new ArrayDeque<>();

deque.addFirst("A");
deque.addLast("B");

deque.removeFirst();
deque.removeLast();
```

Conceptually:

```text
        first                 last
          ↓                     ↓
       ┌──────────────────────────┐
       │ A │ B │ C │ D │ E │ F │
       └──────────────────────────┘
          ↑                     ↑
      removeFirst          removeLast
```

---

# 30. `ArrayDeque`

`ArrayDeque` is usually the best general-purpose implementation for:

* queues
* stacks
* double-ended queues

Queue:

```java
Queue<String> queue =
    new ArrayDeque<>();
```

Stack:

```java
Deque<String> stack =
    new ArrayDeque<>();

stack.push("A");
stack.push("B");

stack.pop(); // B
```

Prefer:

```java
ArrayDeque
```

over the legacy:

```java
Stack
```

for new code.

---

# 31. Queue vs Deque

```text
Queue
  │
  │ FIFO-oriented abstraction
  ▼
[A] [B] [C]


Deque
  │
  ├── add/remove first
  │
  └── add/remove last
```

A `Deque` can therefore implement both:

```text
Queue
```

and:

```text
Stack
```

---

# 32. `PriorityQueue`

`PriorityQueue` does not use FIFO ordering.

Elements are returned according to their priority/order.

```java
Queue<Integer> queue =
    new PriorityQueue<>();

queue.offer(50);
queue.offer(10);
queue.offer(30);

queue.poll(); // 10
queue.poll(); // 30
queue.poll(); // 50
```

Internally, it uses a heap-based structure.

### Complexity

| Operation    | Complexity |
| ------------ | ---------: |
| `offer()`    |   O(log n) |
| `poll()`     |   O(log n) |
| `peek()`     |       O(1) |
| `contains()` |       O(n) |

Important:

> Iterating over a `PriorityQueue` does **not** mean you get elements in sorted order.

The priority guarantee applies to operations such as `peek()` and `poll()`.

---

# 33. `SortedSet` vs `PriorityQueue`

These are easy to confuse.

### `TreeSet`

```java
Set<Integer> set =
    new TreeSet<>();
```

Guarantees:

```text
all elements are maintained in sorted order
```

and:

```text
duplicates are removed
```

### `PriorityQueue`

```java
Queue<Integer> queue =
    new PriorityQueue<>();
```

Guarantees:

```text
the highest/lowest-priority element
is available at the head
```

but does not provide a globally sorted iteration.

---

# 34. `SequencedCollection`

This is one of the most important modern Java additions.

Introduced in Java 21:

```java
SequencedCollection<E>
```

It represents a collection with a defined encounter order.

It provides common operations such as:

```java
getFirst()
getLast()

addFirst()
addLast()

removeFirst()
removeLast()

reversed()
```

Conceptually:

```text
first                         last
 ↓                             ↓
[A] [B] [C] [D] [E] [F]
```

And:

```java
collection.reversed()
```

provides the reverse-order view.

The API was introduced by JEP 431 and is part of Java 25. ([Oracle Documentation][2])

---

# 35. `reversed()` Is a View

This is important.

For a sequenced collection:

```java
List<String> list =
    new ArrayList<>(List.of("A", "B", "C"));

List<String> reversed =
    list.reversed();
```

Conceptually:

```text
list
[A] [B] [C]

reversed
[C] [B] [A]
```

It is a **reverse-ordered view**, not necessarily a new copied collection.

Therefore, modifications to the underlying collection can be reflected in the view.

This is conceptually similar to other collection views.

---

# 36. `SequencedSet`

`SequencedSet` combines:

```text
Set
+
SequencedCollection
```

It means:

> Unique elements + defined encounter order + reversible order.

Example:

```java
SequencedSet<String> set =
    new LinkedHashSet<>();

set.add("A");
set.add("B");
set.add("C");
```

Then:

```java
set.getFirst(); // A
set.getLast();  // C
```

And:

```java
set.reversed();
```

returns the reverse-ordered view.

`LinkedHashSet` implements `SequencedSet`.

`SortedSet` also fits into the sequenced hierarchy in Java 25. ([Oracle Documentation][2])

---

# 37. `SequencedMap`

`SequencedMap` provides the same conceptual idea for mappings.

```java
SequencedMap<String, Integer> map =
    new LinkedHashMap<>();
```

Important operations:

```java
firstEntry()
lastEntry()

pollFirstEntry()
pollLastEntry()

putFirst()
putLast()

reversed()
```

Example:

```java
map.put("A", 1);
map.put("B", 2);
map.put("C", 3);

map.firstEntry(); // A=1
map.lastEntry();  // C=3
```

Reverse:

```java
map.reversed();
```

---

# 38. The `Sequenced*` Mental Model

Before Java 21, APIs were inconsistent.

You might have:

```java
List
```

with an order, and:

```java
LinkedHashSet
```

with an order, but no common interface expressing:

> "This collection has an encounter order."

Java 21 introduced:

```text
SequencedCollection
       │
       ├── List
       │
       └── Deque


SequencedSet
       │
       └── LinkedHashSet
       │
       └── SortedSet


SequencedMap
       │
       └── LinkedHashMap
       │
       └── SortedMap
```

This is an important Java 21+ concept and should be included in a Java 25 mental model. ([Oracle Documentation][2])

---

# 39. Mutable Collections

The standard general-purpose implementations are mutable:

```java
new ArrayList<>()
new LinkedList<>()

new HashSet<>()
new LinkedHashSet<>()
new TreeSet<>()

new HashMap<>()
new LinkedHashMap<>()
new TreeMap<>()

new ArrayDeque<>()
new PriorityQueue<>()
```

For example:

```java
List<String> list =
    new ArrayList<>();

list.add("A");
list.remove("A");
```

---

# 40. Unmodifiable Collections

An unmodifiable collection rejects modification operations.

Modern factory methods include:

```java
List.of()
Set.of()
Map.of()
```

Example:

```java
List<String> names =
    List.of("Alice", "Bob");
```

Then:

```java
names.add("Charlie");
```

throws:

```text
UnsupportedOperationException
```

These factory collections also reject `null` elements/keys/values where applicable.

---

# 41. `List.of()`

```java
List<String> list =
    List.of("A", "B", "C");
```

The list is unmodifiable.

You cannot:

```java
add()
remove()
set()
clear()
```

Use it when the collection should not be modified.

---

# 42. `Set.of()`

```java
Set<String> set =
    Set.of("A", "B", "C");
```

The set is unmodifiable.

Duplicates are not allowed:

```java
Set.of("A", "A");
```

throws an exception.

---

# 43. `Map.of()`

```java
Map<String, Integer> map =
    Map.of(
        "Alice", 30,
        "Bob", 25
    );
```

The map is unmodifiable.

Keys are unique.

---

# 44. `copyOf()`

Modern Java also provides:

```java
List.copyOf()
Set.copyOf()
Map.copyOf()
```

Example:

```java
List<String> original =
    new ArrayList<>();

original.add("A");

List<String> copy =
    List.copyOf(original);
```

Then:

```java
original.add("B");
```

does not change:

```java
copy
```

So:

```text
original
[A] [B]

copy
[A]
```

The copy is independent.

---

# 45. `Collections.unmodifiableList()`

This is different.

```java
List<String> original =
    new ArrayList<>();

original.add("A");

List<String> view =
    Collections.unmodifiableList(original);
```

Now:

```java
view.add("B");
```

fails.

But:

```java
original.add("B");
```

succeeds.

The view sees the modification:

```text
original
[A] [B]
   ↑
   │
view ────────────┘
```

Therefore:

> `unmodifiableList()` = **unmodifiable view**

while:

> `List.copyOf()` = **independent unmodifiable copy**

---

# 46. Unmodifiable vs Immutable

These terms are related but should not be blindly treated as identical.

### Unmodifiable view

```java
Collections.unmodifiableList(original)
```

The view cannot be modified through that reference.

But the underlying collection can change.

### Immutable collection

```java
List.of("A", "B")
```

The collection itself has no supported mutation operations.

### Important

Neither concept automatically makes the elements themselves immutable.

```java
List<Person>
```

can be unmodifiable while the `Person` objects remain mutable.

This is **shallow immutability**.

---

# 47. `Arrays.asList()`

A common legacy/interop API:

```java
List<String> list =
    Arrays.asList("A", "B", "C");
```

The size is fixed.

Therefore:

```java
list.add("D");     // ❌
list.remove("A");  // ❌
```

But:

```java
list.set(0, "X");  // ✅
```

So:

```text
Arrays.asList()

size       → fixed
set()      → supported
add()      → unsupported
remove()   → unsupported
```

It is important not to confuse this with `List.of()`.

---

# 48. `Arrays.asList()` vs `List.of()`

|                 | `Arrays.asList()` | `List.of()`              |
| --------------- | ----------------- | ------------------------ |
| Size            | Fixed             | Fixed                    |
| `set()`         | Yes               | No                       |
| `add()`         | No                | No                       |
| `remove()`      | No                | No                       |
| Unmodifiable    | No                | Yes                      |
| Null            | Allowed           | Not allowed              |
| Backed by array | Yes               | Implementation-dependent |

---

# 49. `Collections` Utility Class

`Collections` is a utility class.

It provides algorithms and wrappers.

Examples:

```java
Collections.sort(list);
Collections.reverse(list);
Collections.shuffle(list);
Collections.min(collection);
Collections.max(collection);
```

It also provides wrappers:

```java
Collections.unmodifiableList(list);
Collections.synchronizedList(list);
```

And Java 25 includes unmodifiable wrappers for the sequenced interfaces:

```java
Collections.unmodifiableSequencedCollection(...)
Collections.unmodifiableSequencedSet(...)
Collections.unmodifiableSequencedMap(...)
```

([Oracle Documentation][5])

---

# 50. `Comparable` vs `Comparator`

Sorting collections requires an ordering.

There are two main approaches.

## `Comparable`

The class defines its natural ordering:

```java
class Person implements Comparable<Person> {

    @Override
    public int compareTo(Person other) {
        return this.age - other.age;
    }
}
```

Then:

```java
Collections.sort(people);
```

or:

```java
people.sort(null);
```

---

## `Comparator`

The ordering is external:

```java
Comparator<Person> byAge =
    Comparator.comparing(Person::age);
```

Then:

```java
people.sort(byAge);
```

This is often preferable because one class can have multiple possible orderings:

```java
byName
byAge
bySalary
byDate
```

---

# 51. `TreeSet` and `TreeMap` Need Ordering

For:

```java
TreeSet<Person>
```

or:

```java
TreeMap<Person, Value>
```

Java needs to know how objects are ordered.

Either:

```java
Comparable
```

or:

```java
Comparator
```

Example:

```java
TreeSet<Person> people =
    new TreeSet<>(
        Comparator.comparing(Person::name)
    );
```

---

# 52. `Comparator` Can Be Composed

Modern Java provides a fluent API.

```java
Comparator<Person> comparator =
    Comparator.comparing(Person::lastName)
              .thenComparing(Person::firstName)
              .thenComparingInt(Person::age);
```

Reverse:

```java
comparator.reversed();
```

Null handling:

```java
Comparator.nullsFirst(comparator);
```

This is particularly useful with `TreeSet`, `TreeMap`, and sorting.

---

# 53. Concurrent Collections

The normal collections:

```text
ArrayList
HashMap
HashSet
TreeMap
TreeSet
```

are not generally safe for concurrent mutation by multiple threads.

Java provides specialized concurrent collections.

The major ones are:

```text
ConcurrentHashMap
ConcurrentSkipListMap
ConcurrentSkipListSet

CopyOnWriteArrayList
CopyOnWriteArraySet

ConcurrentLinkedQueue
ConcurrentLinkedDeque

BlockingQueue implementations
BlockingDeque implementations
```

Oracle's Java 25 Collections Framework documentation explicitly distinguishes concurrent-aware interfaces and implementations from ordinary collections. ([Oracle Documentation][6])

---

# 54. `ConcurrentHashMap`

```java
Map<String, Integer> map =
    new ConcurrentHashMap<>();
```

Designed for concurrent access.

It is generally preferable to:

```java
Collections.synchronizedMap(...)
```

when you need a high-concurrency map.

Example:

```java
ConcurrentHashMap<String, Long> counters =
    new ConcurrentHashMap<>();

counters.merge(
    "requests",
    1L,
    Long::sum
);
```

---

# 55. `ConcurrentHashMap` vs `HashMap`

```text
HashMap
    ↓
general-purpose
    ↓
not thread-safe for concurrent mutation


ConcurrentHashMap
    ↓
concurrent access
    ↓
designed for multiple threads
```

Important:

> Thread-safe collection does not mean every compound operation on your application's state is automatically thread-safe.

For example:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

is not necessarily the atomic operation you intended.

Prefer atomic map APIs when appropriate:

```java
putIfAbsent()
computeIfAbsent()
compute()
merge()
```

---

# 56. `CopyOnWriteArrayList`

```java
List<String> listeners =
    new CopyOnWriteArrayList<>();
```

When the collection changes, a new copy of the underlying array is created.

Therefore it is designed for:

```text
Many reads
Few writes
```

Typical use cases:

* event listeners
* observer lists
* configuration snapshots
* read-heavy concurrent data

It is a poor choice for:

```text
Many writes
```

because every mutation can involve copying.

---

# 57. `CopyOnWriteArraySet`

Similar idea:

```java
Set<String> listeners =
    new CopyOnWriteArraySet<>();
```

Useful when:

```text
Many reads
Few writes
+
Uniqueness
```

---

# 58. `ConcurrentLinkedQueue`

A non-blocking concurrent queue:

```java
Queue<Task> queue =
    new ConcurrentLinkedQueue<>();
```

Useful when multiple threads need to add/remove elements concurrently without using a blocking queue.

---

# 59. Blocking Queues

A `BlockingQueue` is useful for producer/consumer systems.

```text
Producer
   │
   ▼
┌────────────────┐
│ BlockingQueue  │
└────────────────┘
   │
   ▼
Consumer
```

Example:

```java
BlockingQueue<Task> queue =
    new LinkedBlockingQueue<>();
```

Producer:

```java
queue.put(task);
```

Consumer:

```java
Task task =
    queue.take();
```

`take()` can block until an element becomes available.

This is extremely useful for worker architectures.

---

# 60. Important `BlockingQueue` Implementations

### `ArrayBlockingQueue`

Bounded queue backed by an array.

```java
new ArrayBlockingQueue<>(100);
```

Useful when you want explicit capacity.

---

### `LinkedBlockingQueue`

Linked-node blocking queue.

Can be bounded or effectively unbounded depending on construction.

```java
new LinkedBlockingQueue<>(100);
```

---

### `PriorityBlockingQueue`

Concurrent priority queue.

```java
new PriorityBlockingQueue<>();
```

Elements are processed according to priority.

---

### `DelayQueue`

Elements become available only after their delay expires.

Useful for:

* delayed tasks
* expiration systems
* scheduling-like mechanisms

---

### `SynchronousQueue`

Has no actual storage capacity.

Each insertion waits for a corresponding removal.

Conceptually:

```text
Producer ───────► Consumer
          handoff
```

Useful for direct handoff designs.

---

# 61. Concurrent Sorted Collections

Java also provides:

```java
ConcurrentSkipListMap
ConcurrentSkipListSet
```

They provide concurrent sorted collections.

Conceptually:

```text
ConcurrentHashMap
        ↓
fast concurrent lookup
unordered


ConcurrentSkipListMap
        ↓
concurrent lookup
+
sorted keys
```

They are based on skip-list structures rather than the tree used by `TreeMap`.

---

# 62. `ConcurrentSkipListMap`

```java
ConcurrentNavigableMap<Long, Event> events =
    new ConcurrentSkipListMap<>();
```

You get:

* concurrency
* sorted keys
* navigation
* range queries

This can be useful when you need both:

```text
concurrency
+
ordering
```

---

# 63. `ConcurrentSkipListSet`

```java
NavigableSet<Long> values =
    new ConcurrentSkipListSet<>();
```

You get:

```text
unique elements
+
sorted order
+
concurrent access
```

---

# 64. Legacy Collections

Java still contains several old collection classes.

Important ones:

```text
Vector
Stack
Hashtable
```

They are part of Java for compatibility.

---

# 65. `Vector`

```java
Vector<String> vector =
    new Vector<>();
```

`Vector` predates the modern Collections Framework.

It is synchronized by default.

For new code, generally prefer:

```java
ArrayList
```

or an appropriate concurrent collection.

---

# 66. `Stack`

```java
Stack<String> stack =
    new Stack<>();
```

Legacy stack abstraction.

For new code:

```java
Deque<String> stack =
    new ArrayDeque<>();
```

Then:

```java
stack.push("A");
stack.push("B");

stack.pop(); // B
```

Prefer `ArrayDeque`.

---

# 67. `Hashtable`

```java
Hashtable<String, Integer> map =
    new Hashtable<>();
```

Legacy synchronized map.

For modern code, normally prefer:

```java
HashMap
```

or:

```java
ConcurrentHashMap
```

depending on concurrency requirements.

---

# 68. `IdentityHashMap`

This is a specialized map.

Normal maps compare keys using logical equality:

```java
equals()
```

`IdentityHashMap` compares keys using reference identity:

```java
==
```

Example:

```java
String a = new String("hello");
String b = new String("hello");
```

Normally:

```java
a.equals(b) // true
```

But:

```java
a == b // false
```

`IdentityHashMap` treats them as different keys.

Use it only for specialized identity-based algorithms.

---

# 69. `WeakHashMap`

`WeakHashMap` uses weak references for its keys.

Conceptually:

```text
Normal HashMap

Map → Key
      ↓
Key remains strongly reachable
```

With:

```text
WeakHashMap

Map ──weak──→ Key
```

If no strong references to the key remain elsewhere, the entry can become eligible for removal by garbage collection.

Useful for certain cache/metadata scenarios.

But:

> `WeakHashMap` is not a general-purpose cache replacement.

---

# 70. `EnumSet`

`EnumSet` is specialized for enum values.

```java
enum Permission {
    READ,
    WRITE,
    DELETE
}
```

Then:

```java
EnumSet<Permission> permissions =
    EnumSet.of(
        Permission.READ,
        Permission.WRITE
    );
```

It is highly efficient for enum sets and should generally be preferred over a generic `HashSet<Permission>` when the domain is an enum.

---

# 71. `EnumMap`

Similarly:

```java
EnumMap<Permission, Boolean> permissions =
    new EnumMap<>(Permission.class);
```

This is specialized for enum keys.

Use it when:

```text
Map<Enum, Value>
```

is the problem.

---

# 72. `BitSet`

`BitSet` is not a normal `Collection`, but is often grouped with collection-like data structures.

It represents a variable-size sequence of bits.

```java
BitSet bits =
    new BitSet();

bits.set(10);
bits.set(100);
```

Conceptually:

```text
0 1 2 3 4 5 ... 10 ... 100
0 0 0 0 0 0 ...  1 ...  1
```

Useful for:

* compact boolean flags
* bit operations
* mathematical/set-like operations
* efficient membership representation

It can be much more memory-efficient than `Set<Integer>` for dense integer domains.

---

# 73. Special-Purpose Collections

Java provides several specialized collection implementations.

Examples include:

```text
EnumSet
EnumMap
WeakHashMap
IdentityHashMap
Collections.unmodifiableXxx()
Collections.synchronizedXxx()
```

The important lesson is:

> Don't automatically use the most famous collection. Choose according to the required semantics.

---

# 74. Collection Views

Some collection methods return **views**, not copies.

Examples:

```java
map.keySet();
map.values();
map.entrySet();
```

For example:

```java
Map<String, Integer> map =
    new HashMap<>();

map.put("A", 1);
map.put("B", 2);

Set<String> keys =
    map.keySet();
```

`keys` is backed by the map.

Therefore changes can be reflected in both directions.

Similarly:

```java
map.entrySet()
map.values()
```

are views.

---

# 75. `Map.Entry`

A map entry represents:

```text
key + value
```

Example:

```java
for (Map.Entry<String, Integer> entry :
        map.entrySet()) {

    System.out.println(
        entry.getKey()
        + " = "
        + entry.getValue()
    );
}
```

This is usually preferable to repeatedly doing:

```java
for (String key : map.keySet()) {
    map.get(key);
}
```

when you need both key and value.

---

# 76. `Map.computeIfAbsent()`

Extremely useful modern map operation:

```java
Map<String, List<String>> groups =
    new HashMap<>();
```

Instead of:

```java
if (!groups.containsKey(key)) {
    groups.put(key, new ArrayList<>());
}

groups.get(key).add(value);
```

use:

```java
groups
    .computeIfAbsent(
        key,
        k -> new ArrayList<>()
    )
    .add(value);
```

This is particularly useful for grouping/indexing.

---

# 77. `Map.merge()`

Another useful operation:

```java
Map<String, Integer> counts =
    new HashMap<>();

counts.merge(
    "apple",
    1,
    Integer::sum
);
```

This is ideal for counters.

Example:

```java
for (String word : words) {
    counts.merge(
        word,
        1,
        Integer::sum
    );
}
```

---

# 78. `Map.compute()`

You can calculate a value from the existing key/value:

```java
map.compute(
    key,
    (k, oldValue) ->
        oldValue == null
            ? 1
            : oldValue + 1
);
```

Modern `Map` APIs make many common update patterns much cleaner.

---

# 79. `removeIf()`

Collections support predicate-based removal:

```java
list.removeIf(
    value -> value.isExpired()
);
```

This works with many mutable collection implementations.

It is usually clearer than manually modifying a collection while iterating.

---

# 80. `Iterator`

An `Iterator` provides sequential traversal.

```java
Iterator<String> iterator =
    list.iterator();

while (iterator.hasNext()) {

    String value =
        iterator.next();

    System.out.println(value);
}
```

It can also safely remove through the iterator:

```java
iterator.remove();
```

when the collection/iterator supports it.

---

# 81. Fail-Fast Iterators

Many standard collection iterators are described as **fail-fast**.

Example:

```java
for (String value : list) {
    list.remove(value);
}
```

This can produce:

```text
ConcurrentModificationException
```

A safer approach can be:

```java
list.removeIf(...);
```

or use the iterator's removal operation:

```java
Iterator<String> iterator =
    list.iterator();

while (iterator.hasNext()) {

    if (condition(iterator.next())) {
        iterator.remove();
    }
}
```

Important:

> Fail-fast behavior is a bug-detection mechanism, not a concurrency guarantee.

---

# 82. `Spliterator`

Collections also provide a `Spliterator`.

```java
Spliterator<String> spliterator =
    list.spliterator();
```

It supports traversal and partitioning, particularly for streams and parallel processing.

Important operations include:

```java
tryAdvance()
forEachRemaining()
trySplit()
```

Conceptually:

```text
Collection
    │
    ▼
Spliterator
    │
    ├──────────┐
    ▼          ▼
Part A       Part B
```

This is one of the mechanisms behind parallel streams.

---

# 83. Streams Are Not Collections

Don't confuse:

```java
Collection
```

with:

```java
Stream
```

A collection represents stored data.

A stream represents a computation over data.

```java
List<Integer> numbers =
    List.of(1, 2, 3, 4, 5);

List<Integer> result =
    numbers.stream()
           .filter(n -> n % 2 == 0)
           .toList();
```

The stream processes elements.

The resulting:

```java
toList()
```

produces a list, but the stream itself is not a collection.

---

# 84. `Stream.toList()` vs `Collectors.toList()`

Modern Java:

```java
List<String> result =
    stream.toList();
```

The returned list is unmodifiable.

By contrast:

```java
List<String> result =
    stream.collect(Collectors.toList());
```

does not provide the same unmodifiable guarantee.

If you specifically need a mutable `ArrayList`:

```java
List<String> result =
    stream.collect(
        Collectors.toCollection(ArrayList::new)
    );
```

---

# 85. Arrays vs Collections

Arrays:

```java
String[] names;
```

have:

* fixed size
* indexed access
* low-level representation
* no collection methods

Collections:

```java
List<String> names;
```

provide:

* dynamic size
* standard APIs
* interoperability
* collection algorithms
* streams
* iterators

Use arrays when their fixed-size semantics and lower-level representation are useful.

Use collections for most application-level data structures.

---

# 86. Complexity Cheat Sheet

Typical complexity:

| Structure       | `get` / lookup |    `add` / put |        remove | Ordered?  |
| --------------- | -------------: | -------------: | ------------: | --------- |
| `ArrayList`     |     O(1) index | O(1) amortized |          O(n) | Yes       |
| `LinkedList`    |     O(n) index |      O(1) ends |     O(1) ends | Yes       |
| `HashSet`       |       O(1) avg |       O(1) avg |      O(1) avg | No        |
| `LinkedHashSet` |       O(1) avg |       O(1) avg |      O(1) avg | Insertion |
| `TreeSet`       |       O(log n) |       O(log n) |      O(log n) | Sorted    |
| `HashMap`       |       O(1) avg |       O(1) avg |      O(1) avg | No        |
| `LinkedHashMap` |       O(1) avg |       O(1) avg |      O(1) avg | Insertion |
| `TreeMap`       |       O(log n) |       O(log n) |      O(log n) | Sorted    |
| `ArrayDeque`    |      O(1) ends | O(1) amortized |     O(1) ends | Yes       |
| `PriorityQueue` |      O(1) head |       O(log n) | O(log n) head | Priority  |

These are typical complexity characteristics, not guarantees that every operation on every implementation has exactly these costs.

---

# 87. The Hash vs Tree Decision

This is one of the most important decisions.

## Hash

```text
HashMap
HashSet
```

Choose when you want:

```text
Fast lookup
+
No ordering requirement
```

Typical:

```text
O(1) average
```

---

## Tree

```text
TreeMap
TreeSet
```

Choose when you want:

```text
Sorted data
+
Navigation
+
Range queries
```

Typical:

```text
O(log n)
```

---

# 88. The Linked Hash Decision

```text
LinkedHashMap
LinkedHashSet
```

Choose when you want:

```text
Hash performance
+
Predictable encounter order
```

Conceptually:

```text
Hash
+
Linked ordering information
```

---

# 89. The Main Decision Tree

Start with the data model.

## Do you need key → value?

```text
YES
 │
 ▼
Map
 │
 ├── Fast lookup
 │      → HashMap
 │
 ├── Fast lookup + insertion order
 │      → LinkedHashMap
 │
 ├── Sorted keys
 │      → TreeMap
 │
 └── Concurrent access
        → ConcurrentHashMap
```

---

## No key → value?

Do you allow duplicates?

```text
YES
 │
 ▼
List
 │
 ├── General purpose
 │      → ArrayList
 │
 └── Specialized linked/deque behavior
        → LinkedList
```

---

## No duplicates?

```text
NO DUPLICATES
 │
 ▼
Set
 │
 ├── Fast lookup
 │      → HashSet
 │
 ├── Fast lookup + insertion order
 │      → LinkedHashSet
 │
 └── Sorted + navigation
        → TreeSet
```

---

## Processing order?

```text
YES
 │
 ▼
Queue / Deque
 │
 ├── FIFO
 │      → ArrayDeque
 │
 ├── FIFO + blocking
 │      → BlockingQueue
 │
 ├── Priority
 │      → PriorityQueue
 │
 └── Stack / double-ended
        → ArrayDeque
```

---

# 90. The Main Collection Families

```text
                         JAVA COLLECTIONS
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
         LIST                  SET                  QUEUE
          │                     │                     │
     ArrayList              HashSet               ArrayDeque
     LinkedList              │                    LinkedList
     CopyOnWrite             ├─ LinkedHashSet      PriorityQueue
     ArrayList                │
                              └─ TreeSet
                                  │
                            SortedSet
                                  │
                           NavigableSet
```

And maps:

```text
                           MAP
                            │
              ┌─────────────┼─────────────┐
              │             │             │
           HashMap     LinkedHashMap   TreeMap
              │             │             │
         Concurrent       Sequenced      Sorted
         HashMap            Map           Map
                                          │
                                     NavigableMap
```

---

# 91. Modern Java 25 Hierarchy

The modern hierarchy is worth memorizing:

```text
                         Collection
                             │
                    SequencedCollection
                       /             \
                     List            Deque
                                      │
                                    Queue


                         Set
                         │
              ┌──────────┴──────────┐
              │                     │
       SequencedSet             SortedSet
              │                     │
       LinkedHashSet          NavigableSet
                                    │
                                 TreeSet
```

And:

```text
                           Map
                            │
              ┌─────────────┴─────────────┐
              │                           │
        SequencedMap                  SortedMap
              │                           │
       LinkedHashMap               NavigableMap
                                          │
                                       TreeMap
```

This reflects the modern Java Collections Framework after the introduction of the sequenced interfaces. ([Oracle Documentation][2])

---

# 92. What to Use in Real Java/Spring Applications

A practical default table:

| Situation                  | Recommendation                |
| -------------------------- | ----------------------------- |
| Generic list               | `ArrayList`                   |
| Unique values              | `HashSet`                     |
| Unique + insertion order   | `LinkedHashSet`               |
| Sorted unique values       | `TreeSet`                     |
| Generic map                | `HashMap`                     |
| Map + insertion order      | `LinkedHashMap`               |
| Sorted map                 | `TreeMap`                     |
| FIFO                       | `ArrayDeque`                  |
| Stack                      | `ArrayDeque`                  |
| Priority processing        | `PriorityQueue`               |
| Concurrent map             | `ConcurrentHashMap`           |
| Concurrent sorted map      | `ConcurrentSkipListMap`       |
| Concurrent read-heavy list | `CopyOnWriteArrayList`        |
| Producer/consumer          | `BlockingQueue`               |
| Enum set                   | `EnumSet`                     |
| Enum map                   | `EnumMap`                     |
| Identity-based keys        | `IdentityHashMap`             |
| Weak-key behavior          | `WeakHashMap`                 |
| Immutable list             | `List.of()` / `List.copyOf()` |
| Immutable set              | `Set.of()` / `Set.copyOf()`   |
| Immutable map              | `Map.of()` / `Map.copyOf()`   |

---

# 93. The Most Important Interview Distinctions

## `ArrayList` vs `LinkedList`

```text
ArrayList
→ fast index access
→ generally preferred

LinkedList
→ linked nodes
→ useful mainly for specialized deque behavior
```

---

## `HashSet` vs `TreeSet`

```text
HashSet
→ unique
→ fast lookup
→ no sorting

TreeSet
→ unique
→ sorted
→ navigation
→ O(log n)
```

---

## `HashMap` vs `TreeMap`

```text
HashMap
→ fast average lookup
→ no ordering guarantee

TreeMap
→ sorted keys
→ navigation
→ range queries
→ O(log n)
```

---

## `HashMap` vs `LinkedHashMap`

```text
HashMap
→ fast
→ unordered

LinkedHashMap
→ fast
→ predictable encounter order
```

---

## `List.of()` vs `Collections.unmodifiableList()`

```text
List.of()
→ unmodifiable collection

Collections.unmodifiableList(list)
→ unmodifiable view of another collection
```

---

## `List.copyOf()` vs `Collections.unmodifiableList()`

```text
List.copyOf(list)
→ independent unmodifiable copy

Collections.unmodifiableList(list)
→ live unmodifiable view
```

---

## `Queue` vs `Deque`

```text
Queue
→ processing abstraction
→ generally FIFO

Deque
→ both ends
→ can implement queue
→ can implement stack
```

---

## `TreeSet` vs `PriorityQueue`

```text
TreeSet
→ entire collection is sorted
→ unique

PriorityQueue
→ only head has priority guarantee
→ duplicates allowed
```

---

# 94. A Very Useful Mental Model

Don't memorize 20 classes independently.

Think in **dimensions**.

## Dimension 1 — What is the data model?

```text
elements
   ↓
Collection

key → value
   ↓
Map
```

## Dimension 2 — Do duplicates matter?

```text
duplicates allowed
   ↓
List

duplicates forbidden
   ↓
Set
```

## Dimension 3 — Does order matter?

```text
no order requirement
   ↓
Hash*

predictable insertion order
   ↓
LinkedHash*

sorted order
   ↓
Tree*
```

## Dimension 4 — Do you need concurrent access?

```text
single-thread / externally synchronized
   ↓
normal collections

high concurrency
   ↓
java.util.concurrent collections
```

## Dimension 5 — Do you need mutability?

```text
mutable
   ↓
new ArrayList<>()
new HashMap<>()
...

unmodifiable
   ↓
List.of()
Set.of()
Map.of()

read-only view
   ↓
Collections.unmodifiableXxx()

independent unmodifiable copy
   ↓
List.copyOf()
Set.copyOf()
Map.copyOf()
```

---

# 95. The Final Mental Map

```text
                         WHAT DO I NEED?
                                │
              ┌─────────────────┴─────────────────┐
              │                                   │
          key → value                          elements
              │                                   │
             Map                              Collection
              │                                   │
       ┌──────┼──────┐                 ┌─────────┼─────────┐
       │      │      │                 │         │         │
      Hash  Linked  Tree              List       Set      Queue
       │      │      │                 │         │         │
    fast   order   sorted          ArrayList   HashSet  ArrayDeque
                                      │         │         │
                                  LinkedList  LinkedHash  Priority
                                                 │
                                               TreeSet
                                                 │
                                           sorted + nav
```

Then add concurrency:

```text
HashMap
   │
   └── concurrent → ConcurrentHashMap

TreeMap
   │
   └── concurrent → ConcurrentSkipListMap

HashSet
   │
   └── concurrent → ConcurrentHashMap.newKeySet()

TreeSet
   │
   └── concurrent → ConcurrentSkipListSet

ArrayList
   │
   └── read-heavy concurrent → CopyOnWriteArrayList
```

And add sequencing:

```text
Collection
    │
    └── SequencedCollection
            │
            ├── List
            └── Deque


Set
    │
    └── SequencedSet
            │
            ├── LinkedHashSet
            └── SortedSet


Map
    │
    └── SequencedMap
            │
            ├── LinkedHashMap
            └── SortedMap
```

---

# 96. One-Page Cheat Sheet

```text
LIST
────────────────────────────────────────────
ArrayList
    → resizable array
    → O(1) index access
    → default List

LinkedList
    → doubly linked
    → O(n) index access
    → also Deque
    → rarely the best default


SET
────────────────────────────────────────────
HashSet
    → unique
    → hash-based
    → O(1) average lookup
    → no order

LinkedHashSet
    → unique
    → hash-based
    → insertion/encounter order
    → SequencedSet

TreeSet
    → unique
    → sorted
    → O(log n)
    → NavigableSet


MAP
────────────────────────────────────────────
HashMap
    → key/value
    → O(1) average lookup
    → no order

LinkedHashMap
    → key/value
    → predictable encounter order
    → SequencedMap

TreeMap
    → key/value
    → sorted keys
    → O(log n)
    → NavigableMap


QUEUE / DEQUE
────────────────────────────────────────────
ArrayDeque
    → queue
    → deque
    → stack
    → usually preferred

PriorityQueue
    → priority-based
    → heap

BlockingQueue
    → producer/consumer
    → blocking operations


CONCURRENT
────────────────────────────────────────────
ConcurrentHashMap
    → concurrent map

ConcurrentSkipListMap
    → concurrent + sorted map

ConcurrentSkipListSet
    → concurrent + sorted set

CopyOnWriteArrayList
    → many reads + few writes

ConcurrentLinkedQueue
    → concurrent non-blocking queue


IMMUTABLE / UNMODIFIABLE
────────────────────────────────────────────
List.of()
Set.of()
Map.of()

List.copyOf()
Set.copyOf()
Map.copyOf()

Collections.unmodifiableList()
Collections.unmodifiableSet()
Collections.unmodifiableMap()


SPECIALIZED
────────────────────────────────────────────
EnumSet
    → enum values

EnumMap
    → enum keys

WeakHashMap
    → weak keys

IdentityHashMap
    → == instead of equals()

BitSet
    → compact bit representation
```

---

# 97. The Rule of Thumb

When you face a collection problem in Java 25, start with the **contract**, not the implementation.

Ask:

```text
1. Do I have elements or key/value pairs?

2. Do I allow duplicates?

3. Does encounter order matter?

4. Do I need sorted order?

5. Do I need range/navigation operations?

6. Do I need random access?

7. Is this FIFO, LIFO, or priority-based?

8. Is it accessed concurrently?

9. Should it be mutable?

10. Do I need a live view or an independent copy?
```

Then select the implementation.

The most useful default choices to remember are:

```text
List             → ArrayList
Set              → HashSet
Ordered Set      → LinkedHashSet
Sorted Set       → TreeSet

Map              → HashMap
Ordered Map      → LinkedHashMap
Sorted Map       → TreeMap

Queue            → ArrayDeque
Stack            → ArrayDeque
Priority Queue   → PriorityQueue

Concurrent Map   → ConcurrentHashMap
Blocking Queue   → BlockingQueue implementation

Immutable List   → List.of() / List.copyOf()
Immutable Set    → Set.of() / Set.copyOf()
Immutable Map    → Map.of() / Map.copyOf()
```

And the modern Java 25 concepts that should **not** be forgotten are:

```text
SequencedCollection
SequencedSet
SequencedMap

SortedSet
NavigableSet

SortedMap
NavigableMap

ConcurrentMap
ConcurrentNavigableMap
```

These interfaces give you the conceptual vocabulary needed to understand the entire modern Collections Framework rather than simply memorizing individual classes.

[1]: https://docs.oracle.com/en/java/javase/25/docs/api/allclasses-index.html?utm_source=chatgpt.com "All Classes and Interfaces (Java SE 25 & JDK 25)"
[2]: https://docs.oracle.com/en/java/javase/25/core/java-core-libraries-developer-guide.pdf?utm_source=chatgpt.com "Core Libraries"
[3]: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/List.html?utm_source=chatgpt.com "List (Java SE 25 & JDK 25)"
[4]: https://docs.oracle.com/en/java/javase/21/core/creating-sequenced-collections-sets-and-maps.html?utm_source=chatgpt.com "Creating Sequenced Collections, Sets, and Maps"
[5]: https://docs.oracle.com/javase/jp/25/docs/api/java.base/java/util/Collections.html?utm_source=chatgpt.com "Collections (Java SE 25 & JDK 25)"
[6]: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/doc-files/coll-overview.html?utm_source=chatgpt.com "Collections Framework Overview (Java SE 25 & JDK 25)"
