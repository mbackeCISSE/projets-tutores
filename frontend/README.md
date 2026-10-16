# Gestion des projets tutorés

Frontend Angular réalisé pour le Master 2 GDIL.

L’application permet de gérer les étudiants, les enseignants, les sujets, les candidatures et les affectations selon le rôle de l’utilisateur.

## Installation

Prérequis : Node.js 22 et npm.

    npm install
    npm start

Ouvrir http://localhost:4200.

Le [backend Spring Boot](https://github.com/mbackeCISSE/backend) doit être démarré sur http://localhost:8080 avec sa base MySQL et le profil demo. Les instructions sont dans son README.

Sous PowerShell, utiliser npm.cmd si la commande npm est bloquée.

## Comptes de test

Mot de passe commun : Projet2026!

| Rôle | Email |
| --- | --- |
| Administrateur | fatou.ndiaye@gdil.example |
| Enseignant | moussa.diop@gdil.example |
| Étudiant | aminata.diallo@gdil.example |
| Responsable | cheikh.seck@gdil.example |

La connexion est prévue pour une démonstration locale. Un rafraîchissement demande de se reconnecter.

## Organisation

- pages : écrans de l’application.
- components : éléments réutilisables.
- services : appels API et connexion.
- models : interfaces TypeScript.
- guards : accès aux pages selon le rôle.

## Compilation et tests

    npm run build
    npm test -- --watch=false --browsers=ChromeHeadless

Le contrat API est disponible dans [docs/contrat-api.md](docs/contrat-api.md).
