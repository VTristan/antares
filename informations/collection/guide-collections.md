 
# RAISONNEMENT #

_Comment choisir la bonne collection ?_

**Abstraction d'abord, implémentation généraliste ensuite, spécialisation uniquement si le besoin l'exige. Dans ce cas là, j'identifie les contraintes et je recherche les outils spécialisés adaptés (trade-off)**

- Je commence par choisir l'interface selon la sémantique du besoin : `List`, `Set`, `Map` ou `Deque`. 
- Ensuite, sans contrainte particulière, je privilégie l'implémentation généraliste la plus simple : `ArrayList`, `HashSet`, `HashMap` ou `ArrayDeque`. 
- Je ne passe à une implémentation spécialisée que si une contrainte fonctionnelle ou technique le justifie, par exemple 
  - l'`ordre` (LinkedHashSet / LinkedHashMap), 
  - le `tri` (TreeSet / TreeMap), 
  - la `concurrence` (ConcurrentHashMap / collections concurrentes), 
  - la `priorité` (PriorityQueue) 
  - ou un type particulier comme un `enum` (EnumSet / EnumMap).

```
                     Quel est mon besoin ?
                            │
            ┌───────────────┼────────────────┐
            │               │                │
      clé → valeur ?   collection simple ?  file/pile ?
            │               │                │
           OUI             OUI              OUI
            │               │                │
           Map        ┌──────┴──────┐       Deque
            │         │             │         │
            │     doublons ?    unicité ?  ArrayDeque
            │         │             │
            │        OUI           OUI
            │         │             │
            │        List          Set
            │         │             │
            │     ArrayList      HashSet
            │
            ▼
         HashMap
```

Contraintes supplémentaires ?

```
LIST
│
├── cas général
│      → ArrayList
│
└── énormément de lectures + très peu d'écritures + concurrence
       → CopyOnWriteArrayList


SET
│
├── cas général
│      → HashSet
│
├── ordre d'insertion
│      → LinkedHashSet
│
├── éléments triés / floor / ceiling
│      → TreeSet
│
└── enum
       → EnumSet


MAP
│
├── cas général
│      → HashMap
│
├── ordre d'insertion
│      → LinkedHashMap
│
├── clés triées / ranges / floor / ceiling
│      → TreeMap
│
├── accès concurrent
│      → ConcurrentHashMap
│
└── clé enum
       → EnumMap


QUEUE / DEQUE
│
├── FIFO / LIFO / opérations aux deux extrémités
│      → ArrayDeque
│
├── priorité
│      → PriorityQueue
│
└── producteur / consommateur concurrent
       → BlockingQueue
```

Tableau comparatif

| Besoin                 | Interface       | Choix par défaut    | Complexité principale             | Quand changer ?          |
| ---------------------- | --------------- | ------------------- | --------------------------------- | ------------------------ |
| Liste ordonnée/indexée | `List`          | `ArrayList`         | `get` O(1), ajout fin O(1) amorti | concurrence particulière |
| Éléments uniques       | `Set`           | `HashSet`           | `add/contains/remove` O(1) moyen  | ordre ou tri             |
| Clé → valeur           | `Map`           | `HashMap`           | `get/put/remove` O(1) moyen       | ordre, tri, concurrence  |
| Queue / pile           | `Deque`         | `ArrayDeque`        | extrémités O(1) amorti            | concurrence              |
| Éléments par priorité  | `Queue`         | `PriorityQueue`     | ajout O(log n), tête O(1)         | —                        |
| Map concurrente        | `ConcurrentMap` | `ConcurrentHashMap` | O(1) moyen                        | besoin d'ordre trié      |

| Besoin                 | Interface       | Choix par défaut    | Complexité principale             | Quand changer ?          |
| ---------------------- | --------------- | ------------------- | --------------------------------- | ------------------------ |
| Liste ordonnée/indexée | `List`          | `ArrayList`         | `get` O(1), ajout fin O(1) amorti | concurrence particulière |
| Éléments uniques       | `Set`           | `HashSet`           | `add/contains/remove` O(1) moyen  | ordre ou tri             |
| Clé → valeur           | `Map`           | `HashMap`           | `get/put/remove` O(1) moyen       | ordre, tri, concurrence  |
| Queue / pile           | `Deque`         | `ArrayDeque`        | extrémités O(1) amorti            | concurrence              |
| Éléments par priorité  | `Queue`         | `PriorityQueue`     | ajout O(log n), tête O(1)         | —                        |
| Map concurrente        | `ConcurrentMap` | `ConcurrentHashMap` | O(1) moyen                        | besoin d'ordre trié      |

* moyen ou amorti.
  ** O(1) si la position est déjà connue ; accéder à une position arbitraire reste O(n).
