# Vérifications — intégration du 16 octobre 2026

## Sources et configuration

Backend cloné depuis https://github.com/mbackeCISSE/backend au commit fdc826af1ef732fb54ab0c5f18ceacca6f40aa30, dans le dossier voisin ../backend.
Le code source du backend n’a pas été modifié.

Le dépôt utilise Spring Boot 4.1.1, Java 21 et MySQL, contrairement au choix PostgreSQL de la demande initiale.
Les choix du dépôt ont été conservés. MySQL 8.4 a été lancé dans Docker sur 127.0.0.1:3307, avec une base dédiée projets_tutores et un volume persistant.

Le frontend utilise désormais l’API réelle par défaut :
- POST /api/auth/connexion, sans en-tête utilisateur ;
- utilisateur renvoyé conservé en mémoire ;
- X-Utilisateur-Id sur les requêtes métier ;
- aucune dépendance à GET /utilisateurs-demo pour la connexion ;
- aucun repli automatique sur les données mock.

Le contrat dans docs/contrat-api.md est une copie du contrat du backend cloné, incluant sa route de connexion.

## Compilation et tests automatisés

- Frontend : npm.cmd run build réussi ; bundle initial de 364,36 ko.
- Angular : 21 tests réussis sous Chrome Headless.
- Les nouveaux tests vérifient le corps POST de connexion, l’email normalisé, le mot de passe transmis sans modification, l’absence d’en-tête sur la connexion, l’identifiant renvoyé par le backend et l’absence de session après un refus 401.
- Les tests existants couvrent les rôles, les corps HTTP métier, la capacité entière, les candidatures déjà rejetées et le rechargement des places après décision.
- Backend : mvnw.cmd package réussi avec le JDK 26.0.1 disponible sur le poste, en conservant la cible Java 21.
- Backend : 15 tests H2 exécutés avec succès ; les 15 tests MySQL optionnels de Maven n’ont pas été activés et sont signalés comme ignorés.
- La première compilation Maven utilisait JAVA_HOME pointant vers Java 17 et a échoué ; le choix du JDK a été corrigé uniquement pour le processus de compilation.
- git diff --check réussi.

## Parcours réel dans Chrome

Origine Angular : http://localhost:4200, autorisée par le CORS du backend.
API : http://localhost:8080/api, profil demo, connectée à MySQL Docker.

1. Connexion de Moussa Diop, enseignant.
2. Création d’un sujet ouvert avec une place.
3. Déconnexion puis connexion d’Aminata Diallo, étudiante.
4. Candidature au sujet avec une motivation.
5. Déconnexion puis connexion de Cheikh Seck, responsable.
6. Acceptation de la candidature et rechargement des informations.
7. Reconnexion d’Aminata et lecture du statut Acceptée.
8. Rafraîchissement : retour à la connexion, puis reconnexion de l’enseignant et vérification que le sujet est toujours présent en MySQL.

Ce parcours a réussi avec les mocks désactivés. Les captures ordinateur (1440 px) et téléphone (390 px) ont été produites.
Le scénario conserve un sujet d’intégration et sa candidature acceptée dans la base locale dédiée.

## Contrôles HTTP supplémentaires sur le backend réel

- Connexion des quatre rôles avec leurs comptes de démonstration.
- Mauvais mot de passe : 401.
- Ajout, modification, lecture et suppression d’un étudiant et d’un enseignant sans liens métier.
- Connexion des nouveaux comptes avec le mot de passe initial du profil demo.
- Création, modification et suppression d’un sujet sans candidature.
- Modification du sujet d’un autre enseignant : 403.
- Consultation des candidatures par ADMIN : 403.
- Nouvelle candidature au même sujet : 409.
- Nouvelle décision sur une candidature déjà traitée : 409.
- Vérification des places restantes à 0 après acceptation.
- Prévol CORS pour la connexion depuis localhost:4200 autorisé.

Les utilisateurs et le sujet temporaires des contrôles CRUD ont été supprimés par les routes API prévues.

## Limites conservées

La connexion du backend reste la simulation locale prévue par son auteur : BCrypt à la connexion, mais ni JWT ni session serveur.
Les autorisations métier utilisent X-Utilisateur-Id, qui peut être falsifié. Aucune authentification de production n’a été ajoutée.

La session frontend reste en mémoire ; les données réelles restent dans MySQL. Le mode mock facultatif est indépendant de MySQL et doit être activé explicitement.
Les tests n’ont pas été exécutés contre une installation MySQL personnelle ou un serveur déployé.

## Lancement et arrêt

Les instructions complètes figurent dans ../README.md.
MySQL et le backend ont été démarrés localement pour les vérifications ; un frontend était déjà accessible sur localhost:4200.
Arrêter MySQL sans supprimer les données : docker compose -f compose.mysql.yaml stop.
