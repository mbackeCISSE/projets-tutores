package backend.backend;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "tests.mysql", matches = "true")
class MySqlMigrationTests {
    @Test
    void migrationEtDemoConserventDonneesEtCapacite() throws Exception {
        String base = "projets_migration_test_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:mysql://localhost:3306/" + base + "?createDatabaseIfNotExist=true&serverTimezone=UTC";
        try (Connection connexion = DriverManager.getConnection(url,
                System.getenv().getOrDefault("DB_USERNAME", "root"),
                System.getenv().getOrDefault("DB_PASSWORD", ""));
             Statement requete = connexion.createStatement()) {
            String ancienSchema = new ClassPathResource("sql/schema.sql").getContentAsString(StandardCharsets.UTF_8)
                    .replace("    mot_de_passe_hash VARCHAR(60) NULL,", "");
            ScriptUtils.executeSqlScript(connexion, new ByteArrayResource(ancienSchema.getBytes(StandardCharsets.UTF_8)));
            requete.executeUpdate("INSERT INTO utilisateur(id,nom,prenom,email,role) VALUES "
                    + "(1,'Ancien','Etudiant','  ANCIEN@EXAMPLE.FR  ','ETUDIANT'),"
                    + "(2,'Ancien','Enseignant','enseignant@example.fr','ENSEIGNANT')");
            requete.executeUpdate("INSERT INTO sujet(id,titre,description,capacite_max,ouvert,enseignant_id) "
                    + "VALUES(1,'Tableau de bord pedagogique','Sujet existant',1,true,2)");
            requete.executeUpdate("INSERT INTO candidature(etudiant_id,sujet_id,motivation,date_candidature,statut) "
                    + "VALUES(1,1,'Ancienne affectation',CURRENT_TIMESTAMP,'ACCEPTEE')");
            executerScripts(connexion);
            assertEquals(9, nombre(requete, "SELECT COUNT(*) FROM utilisateur"));
            assertEquals(1, nombre(requete, "SELECT COUNT(*) FROM utilisateur WHERE id=1 AND email='ancien@example.fr'"));
            assertEquals(1, nombre(requete, "SELECT COUNT(*) FROM candidature WHERE sujet_id=1 AND statut='ACCEPTEE'"));
            assertEquals(1, nombre(requete, "SELECT COUNT(*) FROM candidature c JOIN utilisateur u ON u.id=c.etudiant_id "
                    + "WHERE u.email='ibrahima.sarr@gdil.example' AND c.statut='EN_ATTENTE'"));
            requete.executeUpdate("UPDATE utilisateur SET mot_de_passe_hash='empreinte-existante' WHERE id=1");
            long candidatures = nombre(requete, "SELECT COUNT(*) FROM candidature");
            executerScripts(connexion);
            assertEquals(9, nombre(requete, "SELECT COUNT(*) FROM utilisateur"));
            assertEquals(candidatures, nombre(requete, "SELECT COUNT(*) FROM candidature"));
            assertEquals(1, nombre(requete, "SELECT COUNT(*) FROM utilisateur WHERE id=1 AND mot_de_passe_hash='empreinte-existante'"));
            // Nettoyer seulement la base aleatoire creee par ce test.
            requete.executeUpdate("DROP DATABASE `" + base + "`");
        }
    }

    private void executerScripts(Connection connexion) {
        for (String fichier : new String[]{"schema.sql", "migration-connexion.sql", "demo.sql"}) {
            ScriptUtils.executeSqlScript(connexion, new ClassPathResource("sql/" + fichier));
        }
    }

    private long nombre(Statement requete, String sql) throws SQLException {
        try (ResultSet resultat = requete.executeQuery(sql)) {
            resultat.next();
            return resultat.getLong(1);
        }
    }
}
