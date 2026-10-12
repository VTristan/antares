
# Collections #

Une collection représente un ensemble d'objets. Le Java Collections Framework fournit trois choses :

- des interfaces : List, Set, Queue, Map...
- des implémentations : ArrayList, HashSet, HashMap...
- des algorithmes/utilitaires : Collections.sort(), binarySearch(), etc.


Schéma simplifié :
```
Iterable<E>
│
└── Collection<E>
│
├── Set<E>
│   │
│   └── SequencedSet<E>
│       │
│       └── SortedSet<E>
│           │
│           └── NavigableSet<E>
│
├── SequencedCollection<E>
│   │
│   ├── List<E>
│   │
│   └── Deque<E>
│   │
│   └── SequencedSet<E>
│
└── Queue<E>
│
└── Deque<E>


Map<K,V>
│
└── SequencedMap<K,V>
│
└── SortedMap<K,V>
│
└── NavigableMap<K,V>
```
Map est volontairement en dehors de Collection.
Parce qu'une Collection<E> contient des éléments E, alors qu'une Map<K,V> contient des associations K,V

Une map peut néanmoins exposer des collections :

map.keySet();
map.values();
map.entrySet();

## Iterable ##

```java
public interface Iterable<T> {
  Iterator<T> iterator();
}
```
Il signifie essentiellement : « cet objet peut être parcouru ».

```
for (String name : names) {
  System.out.println(name);
}
```

Le for-each utilise conceptuellement un Iterator.

Équivalent approximatif :

Iterator<String> iterator = names.iterator();

while (iterator.hasNext()) {
String name = iterator.next();
}

Iterable fournit également :

forEach(...)
spliterator()
4. Iterator

Interface fondamentale :

Iterator<E>

Méthodes essentielles :

boolean hasNext();
E next();
void remove();

Exemple :

Iterator<String> it = names.iterator();

while (it.hasNext()) {
String name = it.next();

    if (name.isBlank()) {
        it.remove();
    }
}

C'est important parce que ceci peut être incorrect :

for (String name : names) {
if (name.isBlank()) {
names.remove(name);
}
}

Une modification structurelle pendant une itération peut provoquer :

ConcurrentModificationException

Pour supprimer proprement :

Iterator<String> it = names.iterator();

while (it.hasNext()) {
if (it.next().isBlank()) {
it.remove();
}
}

ou plus simplement :

names.removeIf(String::isBlank);
5. Fail-fast n'est pas une garantie de synchronisation

Beaucoup d'itérateurs de collections classiques sont fail-fast.

Exemple :

List<String> list = new ArrayList<>(List.of("A", "B", "C"));

for (String s : list) {
list.add("X");
}

peut provoquer :

ConcurrentModificationException

Mais point senior important :

ConcurrentModificationException est un mécanisme de détection de bug, pas un mécanisme de synchronisation.

Oracle précise que ce comportement est effectué en best effort et qu'un programme ne doit jamais dépendre de cette exception pour assurer sa correction.

6. Collection<E>

C'est l'interface centrale :

Collection<E>

Elle expose notamment :

size()
isEmpty()

add()
addAll()

remove()
removeAll()
removeIf()

contains()
containsAll()

retainAll()

clear()

iterator()

toArray()

stream()
parallelStream()

Il faut remarquer que certaines opérations sont dites optionnelles.

Une implémentation peut donc faire :

throw new UnsupportedOperationException();

Exemple avec une collection non modifiable :

List<String> names = List.of("A", "B");

names.add("C");

→ UnsupportedOperationException.

7. Les tableaux Java

Un tableau n'est pas une Collection.

String[] names = new String[10];

Il possède une taille fixe.

names.length

et permet l'accès indexé :

names[3]

Complexité :

Opération	Tableau
accès array[i]	O(1)
modification array[i] = x	O(1)
recherche non triée	O(n)
recherche binaire triée	O(log n)
insertion au milieu	O(n)
suppression au milieu	O(n)
8. Tableaux primitifs vs objets
   int[] a;
   Integer[] b;

Ce ne sont absolument pas les mêmes structures.

Un :

int[]

contient directement des int.

Un :

Integer[]

contient des références vers des objets Integer.

Donc :

int[]

est généralement beaucoup plus compact.

C'est aussi pour cela que :

List<int>

n'existe pas.

Les génériques Java travaillent avec des types référence :

List<Integer>

Ce qui implique éventuellement boxing/unboxing :

int x = 42;

List<Integer> list = new ArrayList<>();

list.add(x); // boxing int -> Integer
9. Covariance des tableaux

Les tableaux Java sont covariants.

Cela compile :

String[] strings = new String[10];

Object[] objects = strings;

Mais :

objects[0] = 42;

compile également.

Et échoue à l'exécution :

ArrayStoreException

Parce que le vrai tableau reste :

String[]

C'est un défaut historique de conception que les génériques évitent.

10. Les génériques sont invariants

Contrairement aux tableaux :

List<String>

n'est pas un sous-type de :

List<Object>

Donc ceci ne compile pas :

List<String> strings = new ArrayList<>();

List<Object> objects = strings;

Et c'est une bonne chose : autrement on pourrait ajouter un Integer dans une List<String>.

11. Wildcards et PECS

Indispensable pour un senior utilisant les collections.

Producteur
List<? extends Number>

La liste produit des Number.

Number n = list.get(0);

Mais :

list.add(10);

est interdit.

Pourquoi ?

Parce que la vraie liste pourrait être :

List<Double>
Consommateur
List<? super Integer>

On peut y ajouter :

list.add(42);

Mais lorsqu'on lit :

Object x = list.get(0);

On ne peut garantir que Object.

Règle :

PECS

Producer Extends
Consumer Super

Exemple standard :

public static <T> void copy(
List<? super T> destination,
List<? extends T> source) {
}
12. SequencedCollection — important depuis Java 21

Une SequencedCollection possède un ordre de rencontre défini.

Elle permet :

getFirst();
getLast();

addFirst();
addLast();

removeFirst();
removeLast();

reversed();

Exemple Java 25 :

List<String> list =
new ArrayList<>(List.of("A", "B", "C"));

System.out.println(list.getFirst()); // A
System.out.println(list.getLast());  // C

System.out.println(list.reversed());

Important :

list.reversed()

renvoie une vue inversée, pas nécessairement une copie.

13. List

Une List :

possède un ordre ;
possède des indices ;
autorise généralement les doublons.

Oracle définit List comme une collection ordonnée avec contrôle précis de la position de chaque élément.

Exemple :

List<String> list = new ArrayList<>();

list.add("A");
list.add("B");
list.add("A");

Résultat :

[A, B, A]
14. API importante de List
    get(index)
    set(index, value)

add(value)
add(index, value)

remove(index)
remove(object)

indexOf()
lastIndexOf()

subList()

sort()

replaceAll()

listIterator()
15. ArrayList

C'est généralement l'implémentation List par défaut.

Structure conceptuelle :

ArrayList
│
▼
Object[]

Exemple :

List<String> list = new ArrayList<>();

À l'intérieur :

elementData:

[A][B][C][ ][ ][ ][ ][ ]

avec séparément :

size = 3
16. Taille vs capacité

Très important.

ArrayList<String> list = new ArrayList<>(100);

Cela ne veut pas dire :

size = 100

mais :

capacity ≈ 100
size = 0

La taille = nombre d'éléments présents.

La capacité = taille du tableau interne.

17. Croissance de ArrayList

Lorsqu'il n'y a plus de place, l'implémentation doit :

1. créer un nouveau tableau
2. copier les éléments
3. remplacer l'ancien tableau

Dans l'implémentation OpenJDK actuelle, la croissance préférée est d'environ 50 % :

oldCapacity >> 1

donc approximativement :

new ≈ old × 1.5

Mais c'est une implémentation OpenJDK, pas un contrat de l'API à coder en dur dans une application.

18. Pourquoi ArrayList.add() est O(1) amorti

La plupart des appels :

list.add(x);

font simplement :

elementData[size] = x

→ O(1).

Parfois il faut agrandir le tableau :

copier n éléments

→ O(n).

Mais ces redimensionnements sont relativement rares.

Sur une longue série d'insertions :

coût moyen par insertion ≈ O(1)

On dit :

O(1) amorti
19. Complexités ArrayList
    Opération	Complexité
    get(i)	O(1)
    set(i)	O(1)
    add(x) fin	O(1) amorti
    add(i,x)	O(n)
    remove(i)	O(n)
    contains(x)	O(n)
    indexOf(x)	O(n)
    itération	O(n)

Insertion au milieu :

[A][B][C][D]

insert X index 1

[A][X][B][C][D]

Il faut déplacer :

B C D
20. Pourquoi ArrayList est souvent extrêmement performant

Même lorsqu'une opération théorique semble moins intéressante, ArrayList profite de :

données relativement compactes ;
peu d'allocations ;
accès séquentiels efficaces ;
bonne localité mémoire/cache CPU.

C'est une raison pour laquelle ArrayList est généralement préférable à LinkedList.

21. ensureCapacity

Si tu connais approximativement la taille :

ArrayList<User> users =
new ArrayList<>(1_000_000);

ou :

list.ensureCapacity(1_000_000);

tu peux éviter plusieurs reallocations.

Depuis Java 19, il existe également des factories dimensionnées pour les collections hashées, par exemple :

HashMap.newHashMap(1_000_000);
HashSet.newHashSet(1_000_000);
LinkedHashMap.newLinkedHashMap(1_000_000);
LinkedHashSet.newLinkedHashSet(1_000_000);

qui prennent le nombre attendu d'éléments, ce qui est plus pratique que de calculer soi-même une capacité tenant compte du load factor.

22. LinkedList

Structure doublement chaînée :

      ┌──────────────┐
      ▼              │
[A] ⇄ [B] ⇄ [C] ⇄ [D]

Chaque nœud contient conceptuellement :

previous
value
next

Contrairement à ArrayList, les objets ne sont pas stockés dans un tableau contigu.

23. Complexités LinkedList
    Opération	Complexité
    get(i)	O(n)
    set(i,x)	O(n)
    ajout début	O(1)
    ajout fin	O(1)
    suppression début	O(1)
    suppression fin	O(1)
    recherche	O(n)
    insertion avec ListIterator déjà positionné	O(1)

Attention à une réponse d'entretien trop simpliste :

« LinkedList est O(1) pour insérer au milieu. »

Seulement si tu as déjà atteint le nœud.

Faire :

list.add(500_000, value);

nécessite d'abord de trouver le nœud :

O(n)
24. Le piège classique avec LinkedList

Ceci est catastrophique :

for (int i = 0; i < list.size(); i++) {
process(list.get(i));
}

Si list est une LinkedList :

get(0) → parcours
get(1) → parcours
get(2) → parcours
...

Complexité totale potentielle :

O(n²)

Il faut utiliser :

for (var element : list) {
process(element);
}
25. ArrayList vs LinkedList

En pratique :

Besoin général → ArrayList

Queue/stack → ArrayDeque

LinkedList → cas beaucoup plus rares

LinkedList implémente :

List
Deque
Queue

mais cela ne signifie pas qu'elle soit le meilleur choix pour chacun.

26. ListIterator

Plus puissant que Iterator.

ListIterator<E>

permet :

next()
previous()

hasNext()
hasPrevious()

add()
remove()
set()

nextIndex()
previousIndex()

Exemple :

ListIterator<String> it = list.listIterator();

while (it.hasNext()) {
String value = it.next();

    if (value.equals("A")) {
        it.set("B");
    }
}
27. subList() : attention à la vue
    List<String> list =
    new ArrayList<>(List.of("A", "B", "C", "D"));

List<String> sub = list.subList(1, 3);

Résultat :

[B, C]

Mais généralement subList() représente une vue de la liste originale.

Modifier :

sub.set(0, "X");

modifie également :

list
28. Set

Un Set représente des éléments uniques.

Set<String> set = new HashSet<>();

set.add("A");
set.add("A");
set.add("B");

Résultat :

A
B

La notion de doublon repose généralement sur le contrat d'égalité de l'implémentation. Pour un HashSet, elle repose sur hashCode() et equals(). Oracle définit Set comme ne contenant pas deux éléments tels que e1.equals(e2).

29. HashSet

Structure fondamentale :

HashSet
│
▼
HashMap

HashSet utilise effectivement une HashMap en interne.

Conceptuellement :

Map<E, Object>

L'élément du Set devient la clé de la map.

La valeur interne est simplement un marqueur.

30. hashCode() et equals()

C'est l'un des sujets les plus importants des collections Java.

Contrat :

Si :

a.equals(b)

est true, alors :

a.hashCode() == b.hashCode()

doit obligatoirement être vrai.

Mais l'inverse n'est pas vrai.

Deux objets peuvent avoir :

a.hashCode() == b.hashCode()

tout en ayant :

a.equals(b) == false

Cela s'appelle une collision.

31. Pourquoi HashSet utilise les deux

Imaginons :

set.contains(person);

Le processus conceptuel est :

hashCode
↓
bucket
↓
candidats
↓
equals

Le hash permet de trouver rapidement une petite zone.

equals() détermine l'égalité réelle.

32. Mauvaise implémentation de equals/hashCode

Exemple :

class User {

    String email;

    @Override
    public boolean equals(Object o) {
        ...
    }
}

Si tu implémentes equals() mais pas correctement hashCode(), alors :

HashSet
HashMap

peuvent se comporter de manière incohérente.

Avec les records :

record User(String email, String name) {}

Java génère automatiquement un equals/hashCode cohérent basé sur les composants du record.

33. Objet mutable comme clé

Très gros piège.

class User {
String email;

    // equals/hashCode basés sur email
}

Puis :

User user = new User("a@test.com");

Map<User, String> map = new HashMap<>();

map.put(user, "Admin");

user.email = "b@test.com";

map.get(user);

Le hashCode() a changé.

L'objet peut maintenant être recherché dans un autre bucket que celui où il avait été inséré.

Résultat :

entrée présente physiquement
mais pratiquement introuvable normalement

Règle senior :

Les clés de HashMap devraient avoir un état participant à equals/hashCode qui reste stable tant qu'elles sont dans la map.

34. HashMap — structure fondamentale

Conceptuellement :

table
│
▼
+---+---+---+---+---+---+
| 0 | 1 | 2 | 3 | 4 | 5 |
+---+---+---+---+---+---+
│       │
▼       ▼
Node    Node
│
▼
Node

Chaque position est souvent appelée bucket/bin.

Chaque Node contient notamment :

hash
key
value
next

L'implémentation OpenJDK actuelle confirme cette structure.

35. Fonctionnement d'un put
    map.put(key, value);

Conceptuellement :

1. calcul key.hashCode()
2. amélioration/mélange du hash
3. calcul de l'index du bucket
4. regarder le bucket
5. si clé identique → remplacer valeur
6. sinon ajouter une entrée
36. Pourquoi la capacité est une puissance de 2

Une implémentation classique pourrait calculer :

index = hash % capacity;

HashMap utilise une optimisation du type :

index = (capacity - 1) & hash;

Ce mécanisme fonctionne efficacement lorsque la capacité est une puissance de deux.

L'implémentation OpenJDK utilise bien des capacités en puissance de deux.

37. Load factor

Par défaut :

0.75

Capacité initiale classique :

16

Seuil approximatif :

16 × 0.75 = 12

Au-delà, la table est agrandie.

La documentation Java 25 indique bien une capacité initiale par défaut de 16 et un load factor de 0.75.

Pourquoi 0.75 ?

Compromis entre :

plus faible
→ moins de collisions
→ plus de mémoire

plus élevé
→ moins de mémoire
→ davantage de collisions
38. Resize

Quand le seuil est dépassé :

table ancienne

16 buckets
↓
table nouvelle

32 buckets

Les éléments sont redistribués.

Cette opération coûte :

O(n)

mais elle n'arrive pas à chaque insertion.

D'où le coût moyen proche de :

O(1)

pour put.

39. Collisions et arbres

Historiquement, un bucket pouvait devenir :

Node → Node → Node → Node → Node

avec un mauvais hash.

Les versions modernes de HashMap peuvent transformer un bucket très chargé en arbre rouge-noir.

L'implémentation OpenJDK 25 utilise notamment :

TREEIFY_THRESHOLD = 8
UNTREEIFY_THRESHOLD = 6
MIN_TREEIFY_CAPACITY = 64

Donc dire simplement :

« après 8 collisions, ça devient un arbre »

est incomplet.

La capacité de la table intervient également.

40. Complexités HashMap

Cas moyen avec bon hash :

Opération	Complexité moyenne
get()	O(1)
put()	O(1)
remove()	O(1)
containsKey()	O(1)
containsValue()	O(n)
itération	O(n + capacité)

Oracle décrit get et put comme étant à temps constant en supposant une bonne dispersion du hash.

Attention à :

containsValue()

Une map est optimisée par clé, pas par valeur.

41. get() et null

HashMap autorise :

null

comme clé et comme valeur.

Donc :

map.get("abc")

qui renvoie null peut signifier :

clé absente

OU :

clé présente avec valeur null

Pour distinguer :

map.containsKey("abc");
42. API moderne de Map

À maîtriser :

get()
put()

getOrDefault()

putIfAbsent()

compute()
computeIfAbsent()
computeIfPresent()

merge()

replace()
replaceAll()

forEach()
43. computeIfAbsent

Excellent exemple :

Map<String, List<User>> usersByCountry =
new HashMap<>();

usersByCountry
.computeIfAbsent(
"France",
key -> new ArrayList<>()
)
.add(user);

Sans computeIfAbsent :

List<User> users = usersByCountry.get("France");

if (users == null) {
users = new ArrayList<>();
usersByCountry.put("France", users);
}

users.add(user);
44. merge

Très élégant pour compter :

Map<String, Integer> counts = new HashMap<>();

counts.merge(word, 1, Integer::sum);

Pour "Java" :

absent → 1

déjà 1 → 2

déjà 2 → 3
45. Les trois vues d'une Map
    map.keySet();

→ Set<K>

map.values();

→ Collection<V>

map.entrySet();

→ Set<Map.Entry<K,V>>

Pour parcourir clés + valeurs :

for (var entry : map.entrySet()) {
System.out.println(
entry.getKey() + " -> " + entry.getValue()
);
}

Éviter :

for (K key : map.keySet()) {
V value = map.get(key);
}

lorsqu'une seule itération sur les entrées suffit.

46. Les vues sont connectées à la Map

Exemple :

Set<String> keys = map.keySet();

keys.remove("A");

Cela supprime également :

A

de la map.

Ce n'est généralement pas une copie.

47. LinkedHashMap

C'est :

HashMap
+
liste doublement chaînée des entrées

Elle possède donc un ordre de rencontre défini.

Ordre normal :

ordre d'insertion

Exemple :

map.put("C", 3);
map.put("A", 1);
map.put("B", 2);

Itération :

C
A
B
48. LinkedHashMap et LRU cache

Elle peut également utiliser l'ordre d'accès :

new LinkedHashMap<>(
16,
0.75f,
true
);

true signifie :

access order

Les éléments récemment consultés sont déplacés.

Oracle documente explicitement cet usage pour les caches LRU.

On peut également surcharger :

removeEldestEntry(...)

pour créer un cache limité.

49. SequencedMap

Java moderne introduit :

SequencedMap<K,V>

avec notamment :

firstEntry()
lastEntry()

pollFirstEntry()
pollLastEntry()

putFirst()
putLast()

reversed()

et :

sequencedKeySet()
sequencedValues()
sequencedEntrySet()

Exemple :

LinkedHashMap<String, Integer> map =
new LinkedHashMap<>();

map.put("A", 1);
map.put("B", 2);

map.firstEntry();
map.lastEntry();

map.reversed();
50. TreeMap

TreeMap repose sur un :

Red-Black Tree

C'est un arbre binaire de recherche auto-équilibré.

Exemple conceptuel :

        8
       / \
      4   12
     / \  / \
    2  6 10 14

Contrairement à HashMap, les clés restent triées.

Complexités principales :

get       O(log n)
put       O(log n)
remove    O(log n)
51. Pourquoi un arbre équilibré

Un arbre naïf peut devenir :

1
\
2
\
3
\
4

Il se comporte alors presque comme une liste :

O(n)

Un Red-Black Tree garde une hauteur proportionnelle à :

log n
52. SortedMap et NavigableMap

TreeMap implémente :

NavigableMap
↑
SortedMap

Méthodes importantes :

firstKey()
lastKey()

lowerKey()
floorKey()

ceilingKey()
higherKey()

headMap()
tailMap()
subMap()

Comprendre :

lower(10)   → strictement < 10
floor(10)   → <= 10

ceiling(10) → >= 10
higher(10)  → strictement > 10

Même logique pour NavigableSet.

53. Comparable

Définit l'ordre naturel d'une classe.

record User(
String name,
int age
) implements Comparable<User> {

    @Override
    public int compareTo(User other) {
        return Integer.compare(age, other.age);
    }
}

Ensuite :

Collections.sort(users);

ou :

new TreeSet<User>();

peut utiliser cet ordre.

54. Comparator

Définit un ordre extérieur à la classe.

Comparator<User> byName =
Comparator.comparing(User::name);

Ou :

Comparator<User> comparator =
Comparator
.comparingInt(User::age)
.thenComparing(User::name);

À connaître :

Comparator.comparing(...)
Comparator.comparingInt(...)

naturalOrder()
reverseOrder()

reversed()

thenComparing()

nullsFirst()
nullsLast()
55. Comparaison avec soustraction : mauvais réflexe

Éviter :

return user1.age() - user2.age();

Risque d'overflow sur des valeurs arbitraires.

Préférer :

return Integer.compare(
user1.age(),
user2.age()
);
56. Cohérence compareTo / equals

Piège senior majeur.

Supposons :

compare(a, b) == 0

mais :

a.equals(b) == false

Dans :

HashSet

ils peuvent être deux éléments différents.

Dans :

TreeSet

ils peuvent être considérés comme occupant la même position logique.

Exemple :

Comparator<User> byAge =
Comparator.comparingInt(User::age);

Deux utilisateurs différents âgés de 32 ans :

new TreeSet<>(byAge)

peut n'en conserver qu'un.

57. HashSet

Résumé :

unicité : equals/hashCode
ordre : non garanti
structure : HashMap
recherche : O(1) moyenne
58. LinkedHashSet

C'est conceptuellement :

HashSet
+
ordre de rencontre

Il implémente maintenant SequencedSet.

Il permet notamment :

getFirst()
getLast()

addFirst()
addLast()

reversed()

en Java 25.

59. TreeSet

Structure :

TreeMap

Le TreeSet est basé sur un TreeMap.

Il offre :

unicité
+
tri
+
navigation

Complexité :

add      O(log n)
remove   O(log n)
contains O(log n)
60. EnumSet

Sous-utilisé mais excellent.

Si tu as :

enum Permission {
READ,
WRITE,
DELETE,
ADMIN
}

Évite :

Set<Permission> permissions =
new HashSet<>();

Tu peux préférer :

EnumSet<Permission> permissions =
EnumSet.of(
Permission.READ,
Permission.WRITE
);

EnumSet est représenté comme un vecteur de bits, ce qui le rend extrêmement compact et performant.

Conceptuellement :

READ WRITE DELETE ADMIN

1     1      0     0
61. EnumMap

Même principe :

EnumMap<Day, Schedule> schedules =
new EnumMap<>(Day.class);

Les clés doivent appartenir au même enum.

Son implémentation utilise des tableaux internes et est très efficace.

Si la clé est un enum :

EnumMap

est souvent préférable à :

HashMap
62. Queue

Une queue représente généralement :

FIFO

First In, First Out.

ajout
↓
[A][B][C]
↑
sortie

Mais toutes les Queue ne sont pas nécessairement FIFO : PriorityQueue est une exception importante.

63. Deux familles de méthodes Queue

Insertion :

add(e)
offer(e)

Extraction :

remove()
poll()

Lecture tête :

element()
peek()

Différence :

Action	Exception	Valeur spéciale
insertion	add	offer
suppression	remove	poll
consultation	element	peek

Par exemple queue vide :

queue.remove();

→ exception.

queue.poll();

→ null.

64. Deque

Double-ended queue :

← [A][B][C] →

On peut insérer et retirer aux deux extrémités.

API :

addFirst()
addLast()

offerFirst()
offerLast()

removeFirst()
removeLast()

pollFirst()
pollLast()

getFirst()
getLast()

peekFirst()
peekLast()

65. ArrayDeque

C'est souvent le meilleur choix pour :

queue
stack
deque

Exemple FIFO :

Deque<Task> queue = new ArrayDeque<>();

queue.addLast(task1);
queue.addLast(task2);

Task task = queue.removeFirst();

Exemple stack LIFO :

Deque<String> stack = new ArrayDeque<>();

stack.push("A");
stack.push("B");

System.out.println(stack.pop()); // B
66. Pourquoi éviter Stack

Stack est une ancienne classe :

Stack<E>

qui hérite de :

Vector<E>

Design historique peu élégant.

Aujourd'hui :

Deque<E> stack = new ArrayDeque<>();

est généralement préféré.

67. PriorityQueue

Très importante en algorithmique.

Elle repose sur un :

binary heap

et non sur un arbre de recherche complet.

Oracle décrit PriorityQueue comme une queue basée sur un priority heap.

Exemple :

PriorityQueue<Integer> queue =
new PriorityQueue<>();

queue.add(10);
queue.add(2);
queue.add(7);

queue.poll(); // 2

La tête est le plus petit élément selon l'ordre naturel ou le Comparator.

68. Complexités PriorityQueue
    Opération	Complexité
    peek()	O(1)
    offer() / add()	O(log n)
    poll()	O(log n)
    contains()	O(n)
    supprimer un élément arbitraire	O(n)

Très important :

Une PriorityQueue n'est pas une collection totalement triée.

Si tu itères dessus :

for (Integer i : queue)

l'ordre complet n'est pas garanti.

Seule la tête respecte la priorité.

69. Heap

Exemple min-heap :

       2
      / \
     5   4
    / \
8   10

Invariant :

parent <= children

Mais :

5

et :

4

ne sont pas nécessairement ordonnés entre eux.

70. WeakHashMap

Collection spécialisée.

Les clés sont détenues via des références faibles.

Cela permet à une entrée de disparaître lorsqu'une clé n'est plus fortement référencée ailleurs.

Cas possibles :

metadata attachées à des objets
caches très particuliers

Mais :

WeakHashMap n'est pas un cache universel.

Le comportement dépend fortement du garbage collector et du cycle de vie des références.

71. IdentityHashMap

Très particulière.

Normalement une map compare les clés avec :

equals()

IdentityHashMap utilise l'identité :

==

Exemple :

String a = new String("Java");
String b = new String("Java");

a.equals(b); // true
a == b;      // false

Dans une :

HashMap

a et b représentent normalement la même clé.

Dans :

IdentityHashMap

ce sont deux clés différentes.

Usage très spécialisé :

algorithmes sur graphes d'objets
détection d'identité
frameworks
72. BitSet

BitSet n'est pas un Set<Integer>.

Mais il est utile à connaître.

BitSet bits = new BitSet();

bits.set(10);
bits.set(42);

bits.get(42); // true

Extrêmement efficace pour représenter un grand ensemble de booléens.

73. List.of, Set.of, Map.of

Java moderne :

List<String> list =
List.of("A", "B", "C");
Set<String> set =
Set.of("A", "B");
Map<String, Integer> map =
Map.of(
"A", 1,
"B", 2
);

Ces collections sont non modifiables.

74. Elles refusent généralement null

Par exemple :

List.of("A", null);

→ NullPointerException.

La documentation Java 25 garantit notamment ce comportement pour List.of.

75. List.copyOf
    List<String> original =
    new ArrayList<>();

List<String> copy =
List.copyOf(original);

copy est non modifiable.

Si ensuite :

original.add("A");

copy ne reflète pas ce changement. Oracle le garantit explicitement.

76. unmodifiableList ≠ copyOf

Compare :

List<String> source =
new ArrayList<>();

List<String> view =
Collections.unmodifiableList(source);

Puis :

source.add("A");

view voit généralement :

A

Parce qu'il s'agit d'une vue non modifiable.

En revanche :

List<String> copy =
List.copyOf(source);

représente le contenu au moment de la copie logique.

77. Non modifiable ≠ objets immuables

Exemple :

List<User> users =
List.of(user);

Tu ne peux pas faire :

users.add(...)

Mais si User est mutable :

user.setName("Bob");

l'objet contenu peut toujours changer.

Donc :

collection non modifiable
≠
graphe d'objets immuable
78. Arrays.asList

Piège classique.

String[] array = {"A", "B", "C"};

List<String> list =
Arrays.asList(array);

Cette liste a une taille fixe liée au tableau.

Ceci fonctionne :

list.set(0, "X");

Mais :

list.add("D");

→ UnsupportedOperationException.

Et :

array[0] = "Y";

est visible via :

list
79. Arrays.asList avec primitives

Énorme piège :

int[] array = {1, 2, 3};

var list = Arrays.asList(array);

Ce n'est pas :

List<Integer>

mais conceptuellement :

List<int[]>

avec un seul élément : le tableau entier.

80. Arrays

À connaître :

Arrays.sort()

Arrays.parallelSort()

Arrays.binarySearch()

Arrays.copyOf()
Arrays.copyOfRange()

Arrays.fill()

Arrays.equals()
Arrays.deepEquals()

Arrays.compare()
Arrays.mismatch()

Arrays.stream()

Arrays.asList()
81. Recherche binaire

Sur un tableau trié :

Arrays.binarySearch(array, value);

Complexité :

O(log n)

Principe :

1 2 3 4 5 6 7 8

        ↑ milieu

j'élimine la moitié

Mais si le tableau n'est pas trié selon le même ordre :

résultat non pertinent
82. Collections

Classe utilitaire :

Collections.sort()
Collections.reverse()
Collections.rotate()
Collections.shuffle()

Collections.min()
Collections.max()

Collections.frequency()

Collections.binarySearch()

Collections.swap()

Collections.fill()

Collections.copy()

Collections.unmodifiableList()
Collections.unmodifiableSet()
Collections.unmodifiableMap()

Collections.synchronizedList()
Collections.synchronizedMap()
83. Contrat equals() de List

Pour les listes :

List.of("A", "B")

est différent de :

List.of("B", "A")

L'ordre compte.

Conceptuellement :

même taille
+
éléments égaux
+
mêmes positions
84. Contrat equals() de Set

Pour les sets :

Set.of("A", "B")

est égal à :

Set.of("B", "A")

L'ordre ne participe pas à l'égalité.

85. Contrat equals() de Map

Deux maps sont égales si elles possèdent les mêmes associations clé/valeur.

L'ordre n'intervient pas.

Même un SequencedMap respecte ce contrat : son ordre de rencontre n'affecte pas Map.equals.

86. RandomAccess

Interface marqueur :

RandomAccess

Implémentée notamment par :

ArrayList

mais pas :

LinkedList

Elle indique qu'un accès :

list.get(i)

est efficace.

Certains algorithmes peuvent utiliser ce signal pour choisir une stratégie différente.

87. Spliterator

Niveau senior.

Spliterator signifie approximativement :

split + iterator

Il sert notamment aux streams, particulièrement parallèles.

Il peut décrire des caractéristiques :

ORDERED
DISTINCT
SORTED
SIZED
NONNULL
IMMUTABLE
CONCURRENT
SUBSIZED

Exemple :

Spliterator<String> spliterator =
list.spliterator();

Un spliterator peut se diviser :

[A B C D E F G H]

       ↓ split

[A B C D]   [E F G H]

ce qui facilite le traitement parallèle.

88. Collections et Streams
    collection.stream()

ne crée pas une nouvelle collection.

Il crée un pipeline de traitement.

Exemple :

List<String> names =
users.stream()
.filter(User::active)
.map(User::name)
.sorted()
.toList();

Attention :

Stream.toList()

renvoie une liste non modifiable dans les Java modernes.

89. Ne pas modifier une collection depuis son stream

À éviter :

list.stream()
.forEach(x -> list.add(...));

On mélange :

lecture
+
modification structurelle

et on crée des comportements incorrects ou exceptions.

90. Collecteurs

À connaître :

Collectors.toList()

Collectors.toSet()

Collectors.toMap()

Collectors.groupingBy()

Collectors.partitioningBy()

Collectors.joining()

Collectors.counting()

Collectors.mapping()

Collectors.toCollection()

Exemple :

Map<String, List<User>> byCountry =
users.stream()
.collect(
Collectors.groupingBy(
User::country
)
);
91. toMap et clés dupliquées

Piège fréquent :

users.stream()
.collect(
Collectors.toMap(
User::country,
User::name
)
);

Si deux utilisateurs ont le même pays :

duplicate key

Il faut définir une fusion :

Collectors.toMap(
User::country,
User::name,
(a, b) -> a
)
92. Collections concurrentes

Collections classiques :

ArrayList
HashMap
HashSet

ne sont pas thread-safe.

Si plusieurs threads partagent une collection et qu'au moins un modifie son état, il faut penser à la synchronisation ou utiliser une collection concurrente adaptée.

93. Collections.synchronizedList

Exemple :

List<String> list =
Collections.synchronizedList(
new ArrayList<>()
);

Les opérations sont synchronisées.

Mais ce n'est pas la même philosophie qu'une collection conçue spécifiquement pour la concurrence.

94. ConcurrentHashMap

C'est la map concurrente majeure.

ConcurrentHashMap<K,V>

Oracle recommande généralement ConcurrentHashMap plutôt qu'une HashMap synchronisée lorsque plusieurs threads accèdent à la map.

Contrairement à une map protégée par un unique gros verrou :

lock global
↓
HashMap entière

ConcurrentHashMap est conçue pour permettre beaucoup plus de concurrence.

Les lectures ne nécessitent notamment pas de verrou global.

95. ConcurrentHashMap et atomicité

Très important.

Ceci n'est pas nécessairement atomique :

if (!map.containsKey(key)) {
map.put(key, value);
}

Entre les deux opérations, un autre thread peut intervenir.

Préférer :

map.putIfAbsent(key, value);

ou :

map.computeIfAbsent(
key,
k -> createValue(k)
);

ConcurrentMap impose des garanties d'atomicité supplémentaires pour ces opérations.

96. ConcurrentHashMap interdit null

Contrairement à :

HashMap

ConcurrentHashMap n'autorise pas les clés ou valeurs null.

Cela permet notamment d'éviter l'ambiguïté :

null = absent ?

ou

null = valeur présente ?

dans un contexte concurrent.

97. Itérateurs weakly consistent

Les collections concurrentes utilisent souvent des itérateurs weakly consistent plutôt que fail-fast.

Ils :

peuvent fonctionner pendant des modifications concurrentes ;
ne lancent pas ConcurrentModificationException pour cette raison ;
peuvent refléter certaines modifications concurrentes, mais pas nécessairement toutes.

C'est explicitement documenté pour les collections concurrentes Java.

98. CopyOnWriteArrayList

Principe :

À chaque modification :

ancien tableau
[A B C]

    ↓ copie

nouveau tableau
[A B C D]

La lecture devient très simple.

Mais l'écriture coûte cher.

Oracle recommande cette collection lorsque les lectures et parcours sont très largement plus fréquents que les modifications.

Excellent cas :

listeners

Exemple :

CopyOnWriteArrayList<Listener>

si on lit 1 million de fois et modifie très rarement.

Mauvais choix pour :

beaucoup d'insertions
99. BlockingQueue

Très importante pour producteurs/consommateurs.

Producer
↓
BlockingQueue
↓
Consumer

Implémentations :

ArrayBlockingQueue
LinkedBlockingQueue
PriorityBlockingQueue
SynchronousQueue

Méthodes importantes :

put()
take()

offer()
poll()

take() peut attendre :

tant qu'aucun élément n'est disponible
100. ArrayBlockingQueue

Queue :

bornée
+
basée sur tableau
+
thread-safe

Exemple :

BlockingQueue<Task> queue =
new ArrayBlockingQueue<>(100);

Si elle est pleine :

queue.put(task);

peut attendre qu'une place se libère.

101. LinkedBlockingQueue

Basée sur des nœuds chaînés.

Elle peut être configurée avec une capacité.

Très utilisée dans certains modèles producer/consumer et executors.

102. ConcurrentLinkedQueue

Queue :

non bloquante
+
thread-safe
+
non bornée

Elle convient lorsque l'on ne veut pas que :

take()

bloque comme avec une BlockingQueue.

103. ConcurrentSkipListMap

Équivalent conceptuel concurrent d'une :

NavigableMap triée

Elle utilise une skip list plutôt qu'un Red-Black Tree classique.

Elle offre typiquement :

O(log n)

pour les opérations principales.

Oracle la recommande généralement plutôt qu'un TreeMap synchronisé lorsqu'une map triée doit être partagée entre plusieurs threads.

104. Skip list

Conceptuellement :

niveau 3: 1 ---------- 8
niveau 2: 1 ---- 4 --- 8
niveau 1: 1 - 2 - 4 - 6 - 8

Les niveaux supérieurs permettent de sauter rapidement des portions de la liste.

105. Vector

Ancienne classe ressemblant à :

ArrayList

mais synchronisée historiquement.

Elle existe surtout pour compatibilité.

En code moderne :

ArrayList

ou une collection concurrente adaptée sont généralement préférables.

106. Hashtable

Ancêtre de HashMap.

Elle est historiquement synchronisée et n'accepte pas null.

En code moderne :

HashMap

ou :

ConcurrentHashMap

sont généralement les choix pertinents selon le besoin.

107. Enumeration

Ancienne abstraction de parcours :

Enumeration<E>

avec :

hasMoreElements()
nextElement()

Elle précède Iterator.

À connaître principalement pour lire du code legacy.

108. Complexités à connaître par cœur
     Structure	Accès index	Recherche	Ajout	Suppression
     Array	O(1)	O(n)	—	—
     ArrayList	O(1)	O(n)	O(1) amorti fin	O(n)
     LinkedList	O(n)	O(n)	O(1) aux extrémités	O(1) aux extrémités
     HashSet	—	O(1) moy.	O(1) moy.	O(1) moy.
     LinkedHashSet	—	O(1) moy.	O(1) moy.	O(1) moy.
     TreeSet	—	O(log n)	O(log n)	O(log n)
     HashMap	—	O(1) moy. clé	O(1) moy.	O(1) moy.
     LinkedHashMap	—	O(1) moy.	O(1) moy.	O(1) moy.
     TreeMap	—	O(log n)	O(log n)	O(log n)
     ArrayDeque	—	O(n)	O(1) amorti extrémités	O(1) extrémités
     PriorityQueue	—	O(n) arbitraire	O(log n)	O(log n) tête
     EnumSet	—	~O(1)	~O(1)	~O(1)
     EnumMap	—	~O(1)	~O(1)	~O(1)

Attention : Big-O est indispensable, mais les constantes, le cache CPU et les allocations comptent aussi.

109. Ordre des collections

Il faut distinguer plusieurs notions.

Ordre d'index
ArrayList
LinkedList
Ordre d'insertion/rencontre
LinkedHashSet
LinkedHashMap
Ordre trié
TreeSet
TreeMap
Ordre de priorité
PriorityQueue
Pas d'ordre garanti
HashSet
HashMap

Très important :

« HashMap semble toujours retourner mes éléments dans cet ordre » n'est pas une garantie contractuelle.

110. null

Le comportement dépend de la collection.

Résumé pratique :

Collection	null
ArrayList	oui
LinkedList	oui
HashSet	oui
HashMap	une clé null, valeurs null
LinkedHashMap	oui
TreeMap	dépend du comparator / généralement éviter
TreeSet	dépend de l'ordre/comparator / généralement éviter
ArrayDeque	non
PriorityQueue	non
ConcurrentHashMap	non
List.of	non
Set.of	non
Map.of	non
111. Choisir la bonne collection

La meilleure manière de raisonner est un arbre de décision :

Ai-je besoin clé → valeur ?
│
├── oui → Map
│         │
│         ├── accès rapide → HashMap
│         ├── ordre insertion → LinkedHashMap
│         ├── clés triées → TreeMap
│         ├── enum → EnumMap
│         └── concurrence → ConcurrentHashMap
│
└── non → Collection
│
├── doublons + indices → List
│       └── ArrayList
│
├── unicité → Set
│       ├── rapide → HashSet
│       ├── ordre insertion → LinkedHashSet
│       ├── trié → TreeSet
│       └── enum → EnumSet
│
└── traitement tête/queue
│
├── FIFO/LIFO → ArrayDeque
└── priorité → PriorityQueue
112. Les choix par défaut d'un développeur senior

Dans énormément de situations :

List<T>

→

ArrayList<T>
Set<T>

→

HashSet<T>
Map<K,V>

→

HashMap<K,V>
Deque<T>

→

ArrayDeque<T>

Puis on change uniquement si une contrainte le justifie :

ordre
tri
concurrence
priorité
type enum
faibles références
identité
113. Piège : remove(int) vs remove(Object)

Très classique :

List<Integer> list =
new ArrayList<>(
List.of(10, 20, 30)
);

list.remove(1);

Cela supprime :

20

car Java appelle :

remove(int index)

Pour supprimer la valeur 1 :

list.remove(Integer.valueOf(1));
114. Piège : modification d'une collection pendant forEach
     for (String value : list) {
     list.remove(value);
     }

Dangereux.

Utiliser :

list.removeIf(...);

ou un :

Iterator

approprié.

115. Piège : HashMap et ordre

Ne jamais faire dépendre une logique métier de :

new HashMap<>()

si l'ordre est important.

Utiliser explicitement :

LinkedHashMap

ou :

TreeMap

selon le besoin.

116. Piège : mutable keys

Éviter :

Map<MutableObject, V>

si l'état mutable participe à :

equals()
hashCode()

Le meilleur candidat pour une clé est souvent :

String
UUID
Integer
Long
enum
record immuable
117. Piège : mauvais hashCode

Le pire hash imaginable :

@Override
public int hashCode() {
return 1;
}

Le contrat est techniquement respecté.

Mais tous les objets tombent dans le même bucket.

On détruit les performances de la table de hash.

118. Piège : trop utiliser TreeMap

Si tu n'as pas besoin du tri :

HashMap

offre généralement :

O(1)

moyen.

TreeMap :

O(log n)

Le coût supplémentaire n'a de sens que si tu exploites :

tri
ranges
floor
ceiling
navigation
119. Piège : choisir LinkedList par théorie Big-O

Question :

« Beaucoup d'insertions : LinkedList ? »

Pas nécessairement.

Il faut demander :

où insère-t-on ?

comment atteint-on cette position ?

combien coûte chaque nœud ?

quelle est la localité mémoire ?

comment la collection est-elle parcourue ?

En pratique, ArrayList gagne énormément de scénarios réels.

120. Ce qu'un entretien senior peut réellement tester

On peut te demander :

HashMap<User, String> map = new HashMap<>();

et enchaîner :

Comment fonctionne put() ?

Tu dois pouvoir expliquer :

hashCode
↓
transformation du hash
↓
bucket
↓
collision
↓
equals
↓
linked nodes
↓
treeification éventuelle
↓
load factor
↓
resize

On peut demander :

Pourquoi HashMap est O(1) ?

Réponse senior :

Ce n'est pas une garantie absolue de O(1). Avec une bonne distribution du hash et un load factor raisonnable, get et put ont un coût moyen constant. Les collisions augmentent le coût, et les buckets très chargés peuvent être arborescents dans l'implémentation moderne.

On peut demander :

HashMap ou TreeMap ?

Réponse :

HashMap
→ recherche rapide
→ pas d'ordre garanti
→ equals/hashCode

TreeMap
→ clés triées
→ navigation/ranges
→ O(log n)
→ Comparable/Comparator

On peut demander :

ArrayList ou LinkedList ?

Réponse :

ArrayList par défaut.

LinkedList seulement lorsqu'un cas précis
bénéficie réellement d'une structure chaînée.

On peut demander :

HashSet ou TreeSet ?

HashSet
→ unicité + performance moyenne O(1)

TreeSet
→ unicité + ordre + navigation O(log n)

On peut demander :

HashMap ou ConcurrentHashMap ?

HashMap
→ non concurrente
→ autorise null

ConcurrentHashMap
→ thread-safe
→ opérations atomiques dédiées
→ pas de null
→ haute concurrence
121. Les 15 structures que tu dois réellement maîtriser

Pour être très fort en entretien, je classerais ainsi :

Priorité	Type
★★★★★	Array
★★★★★	ArrayList
★★★★★	HashMap
★★★★★	HashSet
★★★★★	ArrayDeque
★★★★★	ConcurrentHashMap
★★★★	LinkedHashMap
★★★★	TreeMap
★★★★	TreeSet
★★★★	PriorityQueue
★★★	LinkedList
★★★	CopyOnWriteArrayList
★★★	BlockingQueue
★★★	EnumSet
★★★	EnumMap

Puis connaître le rôle des spécialisées :

WeakHashMap
IdentityHashMap
ConcurrentSkipListMap
ConcurrentLinkedQueue
Vector
Stack
Hashtable
BitSet
122. La carte mentale à retenir

Si tu ne retenais qu'une seule vue :

                        Iterable
                           │
                       Collection
                           │
        ┌──────────────────┼────────────────────┐
        │                  │                    │
       List               Set                 Queue
        │                  │                    │
ArrayList           HashSet              ArrayDeque
LinkedList       LinkedHashSet          PriorityQueue
TreeSet
EnumSet


                        Map
                         │
      ┌──────────────────┼──────────────────┐
      │                  │                  │
HashMap            TreeMap          ConcurrentHashMap
│
LinkedHashMap

Special:
EnumMap
WeakHashMap
IdentityHashMap
ConcurrentSkipListMap

Et mentalement ajouter Java 25 :

SequencedCollection
SequencedSet
SequencedMap

qui fournissent une abstraction commune aux collections possédant un ordre de rencontre défini et aux opérations first / last / reversed.

123. Le niveau « senior »

Connaître :

Map<String, User> map = new HashMap<>();

est niveau débutant/intermédiaire.

Savoir expliquer :

HashMap
├── Node<K,V>[]
├── hash
├── index
├── bucket
├── equals/hashCode
├── collisions
├── linked nodes
├── Red-Black Tree
├── load factor
├── threshold
├── resize
├── mutable keys
├── iteration cost
├── fail-fast
└── thread safety

est déjà beaucoup plus proche du niveau senior.

Et le vrai niveau senior consiste surtout à pouvoir répondre :

Pourquoi choisir cette collection plutôt qu'une autre ?

en raisonnant simultanément sur :

sémantique
complexité
mémoire
concurrence
ordre
mutabilité
API
volume de données
patterns d'accès

C'est cette compréhension-là qu'il faut viser pour ton entretien Java 25.