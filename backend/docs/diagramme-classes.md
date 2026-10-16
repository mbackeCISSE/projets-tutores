# Diagramme de classe

```mermaid
classDiagram
    class Utilisateur {
        Long id
        String nom
        String prenom
        String email
        String motDePasseHash
        Role role
    }
    class Sujet {
        Long id
        String titre
        String description
        int capaciteMax
        boolean ouvert
    }
    class Candidature {
        Long id
        String motivation
        Instant dateCandidature
        Statut statut
    }
    class Role {
        <<enumeration>>
        ADMIN
        ENSEIGNANT
        ETUDIANT
        RESPONSABLE
    }
    class Statut {
        <<enumeration>>
        EN_ATTENTE
        ACCEPTEE
        REJETEE
    }
    Utilisateur "1" <-- "0..*" Sujet : enseignant
    Utilisateur "1" <-- "0..*" Candidature : etudiant
    Sujet "1" <-- "0..*" Candidature : sujet
    Utilisateur --> Role
    Candidature --> Statut
```

Les roles remplacent tout heritage JPA. Les affectations sont les candidatures ACCEPTEE.
Les DTO exposent les identifiants des relations et les noms complets (prenom puis nom).

Le motDePasseHash est une empreinte BCrypt interne, absente des DTO et des reponses HTTP.
