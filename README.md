# Backend - Projets tutores Master 2 GDIL

Java 21, Maven, Spring Boot 4.1.1, MySQL 8.0.16+ (InnoDB). Versions et organisation conservees. Le seul module Spring Security ajoute est spring-security-crypto pour BCrypt : aucune protection HTTP, JWT ou session serveur.

## Lancement local

Demarrer MySQL puis, dans PowerShell :

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="votre-mot-de-passe-MySQL"
$env:DB_URL="jdbc:mysql://localhost:3306/projets_tutores?createDatabaseIfNotExist=true&serverTimezone=UTC"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Ou apres compilation :

```powershell
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Le profil **demo doit etre active explicitement**. Sans ce profil : aucun compte fictif ni mot de passe initial commun n'est initialise, et GET /api/utilisateurs-demo est absent. Le compte MySQL doit pouvoir creer la base (sinon la creer manuellement) et executer la migration ALTER TABLE.

## Base et migration

L'initialisation execute [schema.sql](src/main/resources/sql/schema.sql), puis [migration-connexion.sql](src/main/resources/sql/migration-connexion.sql), avant la validation Hibernate. La migration ajoute mot_de_passe_hash VARCHAR(60) NULL seulement si la colonne manque, sans supprimer les utilisateurs, sujets ou candidatures. Elle normalise les emails existants avec LOWER(TRIM(email)). D'eventuels doublons apres normalisation doivent etre resolus avant migration.

En profil demo uniquement, [demo.sql](src/main/resources/sql/demo.sql) insere les comptes absents et les exemples metier. InitialisationDemoService genere ensuite une empreinte BCrypt salee pour chaque utilisateur sans empreinte, y compris les anciens utilisateurs. Les empreintes existantes sont conservees. Les anciens comptes et leurs relations restent presents ; les nouveaux identifiants ne doivent pas etre codes en dur.

Les scripts sont rejouables. Un compte fictif supprime peut etre recree au redemarrage. Pour une execution manuelle : schema.sql, migration-connexion.sql, puis demo.sql seulement en demonstration ; le demarrage du backend avec le profil demo initialise ensuite les empreintes BCrypt manquantes.

## Connexion de demonstration

API : http://localhost:8080/api. CORS Angular : http://localhost:4200.
Mot de passe local commun : **Projet2026!**.

| Role | Prenom et nom | Email |
| --- | --- | --- |
| ADMIN | Fatou Ndiaye | fatou.ndiaye@gdil.example |
| ENSEIGNANT | Moussa Diop | moussa.diop@gdil.example |
| ENSEIGNANT | Awa Fall | awa.fall@gdil.example |
| ETUDIANT | Aminata Diallo | aminata.diallo@gdil.example |
| ETUDIANT | Ibrahima Sarr | ibrahima.sarr@gdil.example |
| ETUDIANT | Mariama Ba | mariama.ba@gdil.example |
| RESPONSABLE | Cheikh Seck | cheikh.seck@gdil.example |

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/connexion -ContentType "application/json" -Body '{"email":"aminata.diallo@gdil.example","motDePasse":"Projet2026!"}'
```

Reponse : {id, nom, prenom, email, role}, sans mot de passe ni empreinte. Email invalide ou champ vide : 400. Email inconnu ou mauvais mot de passe : 401 avec le meme message `Adresse email ou mot de passe incorrect.`. Emails normalises (trim, minuscules), mot de passe conserve tel que saisi.

En profil demo, les etudiants et enseignants crees par l'admin avec {nom, prenom, email} recoivent aussi temporairement Projet2026!, stocke en BCrypt. Une modification des coordonnees ne modifie pas leur empreinte. Hors demo, aucun mot de passe initial n'est attribue ; la gestion de mots de passe de production n'est pas implementee.

Cette connexion reste une **simulation locale** : Angular garde l'utilisateur en memoire et transmet X-Utilisateur-Id, qui peut etre falsifie. Elle ne protege pas les routes comme une authentification de production. Aucun jeton ni session serveur n'est cree. La deconnexion et le rafraichissement effacent seulement l'utilisateur cote Angular, sans route backend de deconnexion.

Adaptations Angular (frontend non modifie ici) :

1. AuthService appelle POST /auth/connexion avec email et motDePasse.
2. SessionService conserve l'utilisateur recu uniquement en memoire.
3. Garder l'intercepteur X-Utilisateur-Id pour les routes metier.
4. Desactiver MOCK_ENABLED lorsque le backend est pret.

## Verification

```powershell
.\mvnw.cmd package
```

Les tests H2 couvrent connexion pour les quatre roles, erreurs 400/401, normalisation, conservation du mot de passe saisi, absence de secrets dans les reponses, comptes crees par l'admin, conservation des empreintes, initialisation idempotente et comportement hors profil demo. Les tests metier existants couvrent permissions, doublons, capacites, decisions et acceptations concurrentes.

Tests optionnels sur MySQL actif sur localhost:3306 :

```powershell
.\mvnw.cmd "-Dtest=MySqlBackendTests,MySqlMigrationTests" "-Dtests.mysql=true" test
```

Chaque execution cree une base projets_tutores_test_<identifiant-aleatoire> distincte, sans toucher a projets_tutores. Hibernate supprime les tables de test a la fermeture ; la base vide reste visible. Les identifiants techniques proviennent de DB_USERNAME et DB_PASSWORD.

Sur ce poste, les commandes Maven necessitent ces options pour le depot et les certificats Windows (sans desactiver TLS) :

```powershell
mvn "-Dmaven.repo.local=C:/Users/PC/.m2/repository" "-Djavax.net.ssl.trustStoreType=Windows-ROOT" "-Djavax.net.ssl.trustStore=NONE" package
```

[Contrat API](docs/contrat-api.md) | [Diagramme Mermaid](docs/diagramme-classes.md)

Verifications realisees pour cette evolution : compilation et JAR, 15 tests H2 (14 connexion/metier et 1 hors demo), 15 tests MySQL (14 connexion/metier et 1 migration/reexecution des scripts). Aucun echec ni erreur. Les anciens utilisateurs ont ete conserves lors de la migration de projets_tutores, leurs empreintes manquantes ont ete initialisees, et une connexion HTTP avec Aminata a ete verifiee. Les nouvelles candidatures fictives sont EN_ATTENTE pour preserver les affectations et capacites existantes.

Rapports : `target/surefire-reports/backend.backend.BackendApplicationTests.txt`, `backend.backend.HorsDemoTests.txt`, `backend.backend.MySqlBackendTests.txt` et `backend.backend.MySqlMigrationTests.txt`.
