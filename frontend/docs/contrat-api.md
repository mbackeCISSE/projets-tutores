# Contrat API - Projets tutores Master 2 GDIL

Choix confirme pendant le developpement : **MySQL** remplace PostgreSQL demande initialement, pour conserver la base du projet Initializr. Les routes metier restent identiques ; la connexion de demonstration est ajoutee ci-dessous.

Base : `http://localhost:8080/api`. Frontend CORS : `http://localhost:4200`.
Les routes metier exigent `X-Utilisateur-Id`, identifiant numerique d'un utilisateur existant. POST /auth/connexion et GET /utilisateurs-demo (profil demo uniquement) sont accessibles sans cet en-tete. Le backend verifie un mot de passe BCrypt mais ne cree aucun jeton ni session serveur. X-Utilisateur-Id peut etre falsifie : ce mecanisme reste une simulation locale, sans securite de production.

Les listes sont des tableaux JSON, les identifiants sont numeriques et les dates sont ISO 8601 en UTC.
Les noms complets suivent l'ordre prenom puis nom.

## Connexion de demonstration

`POST /api/auth/connexion`, sans X-Utilisateur-Id :

```json
{"email":"aminata.diallo@gdil.example","motDePasse":"Projet2026!"}
```

Reponse 200, DTO Utilisateur existant (identifiant genere, cet exemple est indicatif) :

```json
{"id":4,"nom":"Diallo","prenom":"Aminata","email":"aminata.diallo@gdil.example","role":"ETUDIANT"}
```

Aucune reponse ne contient motDePasse ou motDePasseHash.

- 400 : email invalide, champ obligatoire absent, null ou blanc, JSON invalide.
- 401 : `{"message":"Adresse email ou mot de passe incorrect."}` pour email inconnu, mauvais mot de passe ou compte sans empreinte.
- Les emails sont normalises avec trim et minuscules avant validation, enregistrement et recherche. Le mot de passe saisi reste strictement inchange.
- CORS autorise http://localhost:4200, Content-Type et X-Utilisateur-Id, POST et OPTIONS ainsi que les methodes metier existantes.
- Pas d'inscription publique, JWT, jeton, cookie de connexion, session serveur ou route de deconnexion.
- Angular conserve uniquement l'utilisateur recu en memoire. Deconnexion et rafraichissement effacent cet utilisateur. Les requetes metier gardent X-Utilisateur-Id et leurs controles de role/propriete.
- GET /utilisateurs-demo reste disponible en profil demo pour compatibilite ; la connexion ne l'appelle pas et n'en depend pas.
- En profil demo, les nouveaux etudiants et enseignants crees par ADMIN recoivent temporairement Projet2026! (BCrypt). Les corps POST/PUT restent {nom, prenom, email}. Une modification des coordonnees conserve l'empreinte existante.
- Les comptes fictifs et l'initialisation des empreintes manquantes ne sont executes qu'en profil demo. Les empreintes deja presentes ne sont jamais remplacees au demarrage. Hors demo, aucun mot de passe initial commun n'est attribue ; la gestion de vrais comptes reste hors perimetre.

## Objets renvoyes

Utilisateur :
```json
{"id":1,"nom":"Martin","prenom":"Alice","email":"admin@demo.fr","role":"ADMIN"}
```
Sujet :
```json
{"id":1,"titre":"Projet","description":"Description","capaciteMax":2,"ouvert":true,"enseignantId":2,"enseignantNom":"Moussa Diallo","placesRestantes":2,"disponible":true}
```
Candidature :
```json
{"id":1,"etudiantId":4,"etudiantNom":"Emma Durand","sujetId":1,"sujetTitre":"Projet","motivation":"Ma motivation","dateCandidature":"2026-10-01T09:00:00Z","statut":"EN_ATTENTE"}
```
Erreur :
```json
{"message":"Explication"}
```

Roles : ADMIN, ENSEIGNANT, ETUDIANT, RESPONSABLE.
Statuts : EN_ATTENTE, ACCEPTEE, REJETEE.
Les etudiants et enseignants sont des utilisateurs, sans heritage JPA.

## Routes

| Methode | Route | Autorisation et resultat |
| --- | --- | --- |
| POST | /auth/connexion | Sans en-tete obligatoire, verification email et mot de passe, renvoie un Utilisateur |
| GET | /utilisateurs-demo | Compatibilite locale, profil demo uniquement, sans en-tete obligatoire |
| GET | /etudiants | ADMIN, liste des etudiants |
| POST | /etudiants | ADMIN, creation d'un ETUDIANT |
| PUT | /etudiants/{id} | ADMIN, modification d'un ETUDIANT |
| DELETE | /etudiants/{id} | ADMIN, suppression d'un ETUDIANT |
| GET | /enseignants | ADMIN, liste des enseignants |
| POST | /enseignants | ADMIN, creation d'un ENSEIGNANT |
| PUT | /enseignants/{id} | ADMIN, modification d'un ENSEIGNANT |
| DELETE | /enseignants/{id} | ADMIN, suppression d'un ENSEIGNANT |
| GET | /sujets | ADMIN et RESPONSABLE : tous ; ENSEIGNANT : ses sujets ; ETUDIANT : disponibles |
| POST | /sujets | ENSEIGNANT uniquement, enseignant deduit de l'en-tete |
| PUT | /sujets/{id} | ENSEIGNANT uniquement, ses propres sujets |
| DELETE | /sujets/{id} | ENSEIGNANT uniquement, ses propres sujets |
| GET | /candidatures | ETUDIANT : les siennes ; ENSEIGNANT : celles de ses sujets ; RESPONSABLE : toutes ; ADMIN interdit |
| POST | /candidatures | ETUDIANT uniquement, etudiant deduit de l'en-tete |
| PATCH | /candidatures/{id}/statut | RESPONSABLE uniquement |

Corps POST et PUT /etudiants et /enseignants :
```json
{"nom":"Durand","prenom":"Emma","email":"emma@demo.fr"}
```
Le role est attribue par le backend selon la route.

Corps POST et PUT /sujets :
```json
{"titre":"Projet","description":"Description","capaciteMax":2,"ouvert":true}
```
Corps POST /candidatures :
```json
{"sujetId":1,"motivation":"Ma motivation"}
```
Corps PATCH /candidatures/{id}/statut :
```json
{"statut":"ACCEPTEE"}
```
ou `{"statut":"REJETEE"}`. Les affectations sont les candidatures ACCEPTEE, sans route supplementaire.

## Codes HTTP

- POST : 201 avec l'objet cree.
- GET, PUT, PATCH : 200 avec les donnees.
- DELETE : 204 sans corps.
- 400 : saisie invalide, en-tete absent ou mal forme, utilisateur selectionne inexistant.
- 401 : connexion refusee, message unique `Adresse email ou mot de passe incorrect.` pour email inconnu ou mauvais mot de passe.
- 403 : acces interdit, role non autorise ou sujet d'un autre enseignant.
- 404 : ressource absente.
- 409 : conflit metier (doublon, capacite, decision deja prise, suppression d'une ressource liee).

## Regles metier

- Une candidature unique par couple etudiant/sujet, avec contrainte UNIQUE en base.
- Nouvelle candidature EN_ATTENTE ; un etudiant peut candidater a plusieurs sujets.
- Un sujet est disponible si ouvert et placesRestantes > 0.
- placesRestantes = capaciteMax - nombre de candidatures ACCEPTEE.
- Seules les candidatures EN_ATTENTE peuvent etre acceptees ou rejetees.
- Une acceptation exige un sujet ouvert et une place restante.
- Transaction et verrou pessimiste sur le sujet pour les acceptations et changements de capacite.
- Les transactions de modification utilisent READ_COMMITTED pour relire les comptes apres acquisition du verrou, y compris avec MySQL.
- La capacite ne peut pas etre reduite sous le nombre d'etudiants acceptes.
- Suppression interdite d'un sujet ayant des candidatures ou d'un utilisateur lie a des sujets ou candidatures.
- Email unique et valide, textes obligatoires non blancs, capacite entiere >= 1.
- Le backend refuse egalement une nouvelle candidature sur un sujet ferme ou complet, conformement a la liste des sujets disponibles.

## Organisation

Spring Boot, Maven, Spring Web MVC, Spring Data JPA, Bean Validation, Java 21.
Packages entity, repository, service, controller, dto, exception.
Conversions DTO manuelles, aucun Lombok, MapStruct ni interface de service.
