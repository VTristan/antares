Un Reverse Proxy (proxy inversé) et une API Gateway (passerelle API) sont deux outils qui servent d'intermédiaires entre vos clients (utilisateurs, applications) et vos serveurs backend.
Pour faire simple : une API Gateway est une version évoluée et plus intelligente d'un Reverse Proxy, spécialement conçue pour gérer des architectures modernes comme les microservices.
Voici la distinction claire entre les deux :

1. Le Reverse Proxy : Le gardien de base

Un reverse proxy se place devant un ou plusieurs serveurs web. Son rôle principal est de protéger, optimiser et diriger le trafic réseau.
• Ce qu'il fait : Il masque l'identité de vos vrais serveurs (sécurité), répartit la charge de travail (load balancing), gère les certificats SSL (chiffrement) et met en cache le contenu pour aller plus vite.
• Exemples populaires : Nginx, HAProxy, Apache.

2. L'API Gateway : Le chef d'orchestre des API

Une API Gateway fait tout ce que fait un reverse proxy, mais elle possède une intelligence applicative. Elle comprend le contenu des requêtes (le langage des API) et sait gérer la complexité des architectures distribuées.
• Ce qu'il fait en plus : Il vérifie l'identité des utilisateurs (Authentification JWT, OAuth), bloque les abus en limitant le nombre de requêtes par minute (rate limiting), transforme les données à la volée (convertir du XML en JSON), et distribue les requêtes vers des dizaines de microservices différents.
• Exemples populaires : Kong, Apigee (Google), AWS API Gateway, Traefik.



Transport Layer Security (TLS)

Le protocole TLS est un standard cryptographique qui succède au protocole obsolète SSL (Secure Sockets Layer).
• Fonctionnement : Il sécurise les communications en assurant l'authentification du serveur, la confidentialité des données via un chiffrement symétrique/asymétrique et l'intégrité des flux échangés.
• HTTPS : Il constitue la base du protocole HTTPS, qui sécurise la navigation web et l'authentification des sites via des certificats numériques.
• Versions : La version la plus récente et performante est TLS 1.3 (définie en 2018), qui accélère l'établissement de la connexion (handshake).
