-- Migration MySQL rejouable : conserver les utilisateurs et leurs relations.
SET @colonne_presente = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'utilisateur' AND column_name = 'mot_de_passe_hash');
SET @migration = IF(@colonne_presente = 0,
    'ALTER TABLE utilisateur ADD COLUMN mot_de_passe_hash VARCHAR(60) NULL', 'SELECT 1');
PREPARE migration_connexion FROM @migration;
EXECUTE migration_connexion;
DEALLOCATE PREPARE migration_connexion;
UPDATE utilisateur SET email = LOWER(TRIM(email)) WHERE BINARY email <> BINARY LOWER(TRIM(email));
