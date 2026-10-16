package backend.backend;

import backend.backend.dto.UtilisateurRequete;
import backend.backend.entity.Role;
import backend.backend.entity.Utilisateur;
import backend.backend.repository.UtilisateurRepository;
import backend.backend.service.InitialisationDemoService;
import backend.backend.service.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:horsdemo;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.datasource.hikari.connection-init-sql=",
    "spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.root=WARN", "debug=false"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HorsDemoTests {
    @Autowired ApplicationContext contexte;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired UtilisateurService gestionUtilisateurs;
    @Autowired MockMvc http;

    @Test
    void aucunCompteNiMotDePasseDemoSansProfilDemo() throws Exception {
        assertTrue(contexte.getBeansOfType(InitialisationDemoService.class).isEmpty());
        assertEquals(0, utilisateurs.count());
        http.perform(get("/api/utilisateurs-demo")).andExpect(status().isNotFound());
        Utilisateur admin = new Utilisateur();
        admin.setNom("Admin"); admin.setPrenom("Test");
        admin.setEmail("admin@test.example"); admin.setRole(Role.ADMIN);
        utilisateurs.saveAndFlush(admin);
        var nouveau = gestionUtilisateurs.creer(admin.getId(), Role.ETUDIANT,
                new UtilisateurRequete("Nom", "Prenom", "nouveau@test.example"));
        assertNull(utilisateurs.findById(nouveau.id()).orElseThrow().getMotDePasseHash());
    }
}
