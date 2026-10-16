# Regroupement du 16 octobre 2026

Le dépôt GitHub mbackeCISSE/projets-tutores conserve son historique et regroupe désormais backend/, frontend/, capture/ et un README général.

- Backend déplacé depuis la racine, code applicatif inchangé, référence fdc826af1ef732fb54ab0c5f18ceacca6f40aa30.
- Frontend importé sans modification depuis alimar440/projets-tutores-frontend, commit a8f27352082754f726bb7a3fc7b3826e047e9612.
- Clone indépendant : D:/developpement/angular_project/projets-tutores-frontend.
- Le frontend intégré utilise déjà l’API réelle http://localhost:8080/api avec MOCK_ENABLED=false.
- README général adapté du document fourni, avec les 11 images du dossier capture fourni.
- Aucun dépôt Git imbriqué, sous-module, node_modules ou artefact de compilation à publier.

## Verifications executees

- Maven package depuis backend/ : compilation et JAR reussis avec Java 21 ; 15 tests H2 reussis. Les 15 tests MySQL optionnels ne sont pas actives lors de ce regroupement.
- npm ci : installation reussie avec les versions de package-lock.json conservees.
- Angular build de production : reussi avec Node 22.16.0 ; bundle initial 364,36 kB.
- Angular tests sous Chrome Headless : 21 tests reussis, aucun echec (Node 22.16.0, NG_BUILD_MAX_WORKERS=2).
- Comparaison au commit backend initial : 50 fichiers conserves, hors conversion des fins de ligne Windows.
- Comparaison a l'archive frontend : les 55 fichiers etaient identiques octet pour octet a l'import. Les liens de documentation ont ensuite ete actualises lors du renommage du depot.
- Comparaison des captures : les 11 PNG sont identiques aux fichiers fournis.
- Tous les liens locaux du README general existent et son encodage UTF-8 est verifie.

Les premieres tentatives npm ont rencontre un probleme de certificats, puis un echec de lancement avec Node 26. L'installation a reussi avec le magasin de certificats Windows (NODE_OPTIONS=--use-system-ca), sans desactiver TLS. Node 22 a ensuite ete installe temporairement sous target/tools, sans modifier l'installation systeme ; les compilations et tests Angular utilisent deux workers (NG_BUILD_MAX_WORKERS=2).

Le parcours navigateur complet Angular vers MySQL et les tests MySQL optionnels ne sont pas rejoues pendant ce regroupement. Le document frontend/docs/verification.md et le README backend conservent les resultats historiques de leurs auteurs.

## Nom du depot

Le depot backend a ete renomme projets-tutores sur GitHub. Son historique est conserve et les dossiers backend/, frontend/, capture/ ainsi que le README general restent a la racine. Les liens GitHub et la commande de clonage ont ete actualises ; aucun code applicatif ne change.
