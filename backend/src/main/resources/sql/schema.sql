CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom TEXT NOT NULL CHECK (TRIM(nom) <> ''),
    prenom TEXT NOT NULL CHECK (TRIM(prenom) <> ''),
    email VARCHAR(254) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(60) NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'ENSEIGNANT', 'ETUDIANT', 'RESPONSABLE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS sujet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre TEXT NOT NULL CHECK (TRIM(titre) <> ''),
    description TEXT NOT NULL CHECK (TRIM(description) <> ''),
    capacite_max INTEGER NOT NULL CHECK (capacite_max >= 1),
    ouvert BIT NOT NULL,
    enseignant_id BIGINT NOT NULL,
    CONSTRAINT fk_sujet_enseignant FOREIGN KEY (enseignant_id) REFERENCES utilisateur(id),
    INDEX idx_sujet_enseignant (enseignant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS candidature (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id BIGINT NOT NULL,
    sujet_id BIGINT NOT NULL,
    motivation TEXT NOT NULL CHECK (TRIM(motivation) <> ''),
    date_candidature TIMESTAMP(6) NOT NULL,
    statut VARCHAR(255) NOT NULL CHECK (statut IN ('EN_ATTENTE', 'ACCEPTEE', 'REJETEE')),
    CONSTRAINT fk_candidature_etudiant FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id),
    CONSTRAINT fk_candidature_sujet FOREIGN KEY (sujet_id) REFERENCES sujet(id),
    INDEX idx_candidature_sujet_statut (sujet_id, statut),
    CONSTRAINT uk_candidature_etudiant_sujet UNIQUE (etudiant_id, sujet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
