-- Rejouable sans ecraser les modifications existantes. Les identifiants sont generes.
-- Les empreintes manquantes seront generees avec BCrypt par InitialisationDemoService.
INSERT IGNORE INTO utilisateur (nom, prenom, email, role) VALUES
    ('Ndiaye', 'Fatou', 'fatou.ndiaye@gdil.example', 'ADMIN'),
    ('Diop', 'Moussa', 'moussa.diop@gdil.example', 'ENSEIGNANT'),
    ('Fall', 'Awa', 'awa.fall@gdil.example', 'ENSEIGNANT'),
    ('Diallo', 'Aminata', 'aminata.diallo@gdil.example', 'ETUDIANT'),
    ('Sarr', 'Ibrahima', 'ibrahima.sarr@gdil.example', 'ETUDIANT'),
    ('Ba', 'Mariama', 'mariama.ba@gdil.example', 'ETUDIANT'),
    ('Seck', 'Cheikh', 'cheikh.seck@gdil.example', 'RESPONSABLE');
INSERT IGNORE INTO sujet (titre, description, capacite_max, ouvert, enseignant_id)
SELECT 'Gestion des projets tutores', 'Application web de suivi des projets du Master GDIL.', 2, true, u.id
FROM utilisateur u WHERE u.email = 'moussa.diop@gdil.example'
AND NOT EXISTS (SELECT 1 FROM sujet WHERE titre = 'Gestion des projets tutores');
INSERT IGNORE INTO sujet (titre, description, capacite_max, ouvert, enseignant_id)
SELECT 'Tableau de bord pedagogique', 'Visualisation des indicateurs de formation.', 1, true, u.id
FROM utilisateur u WHERE u.email = 'awa.fall@gdil.example'
AND NOT EXISTS (SELECT 1 FROM sujet WHERE titre = 'Tableau de bord pedagogique');
INSERT IGNORE INTO sujet (titre, description, capacite_max, ouvert, enseignant_id)
SELECT 'Archives numeriques', 'Organisation des anciens projets.', 3, false, u.id
FROM utilisateur u WHERE u.email = 'moussa.diop@gdil.example'
AND NOT EXISTS (SELECT 1 FROM sujet WHERE titre = 'Archives numeriques');
INSERT IGNORE INTO candidature (etudiant_id, sujet_id, motivation, date_candidature, statut)
SELECT u.id, s.id, 'Je souhaite developper mes competences en Spring et Angular.',
       '2026-10-01 09:00:00.000000', 'EN_ATTENTE'
FROM utilisateur u CROSS JOIN sujet s
WHERE u.email = 'aminata.diallo@gdil.example' AND s.titre = 'Gestion des projets tutores';
INSERT IGNORE INTO candidature (etudiant_id, sujet_id, motivation, date_candidature, statut)
SELECT u.id, s.id, 'Je souhaite travailler sur la visualisation des donnees.',
       '2026-10-01 10:00:00.000000', 'EN_ATTENTE'
FROM utilisateur u CROSS JOIN sujet s
WHERE u.email = 'ibrahima.sarr@gdil.example' AND s.titre = 'Tableau de bord pedagogique';
