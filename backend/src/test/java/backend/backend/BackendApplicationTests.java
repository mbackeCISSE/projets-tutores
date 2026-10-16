package backend.backend;

import backend.backend.dto.*;
import backend.backend.entity.*;
import backend.backend.exception.ErreurMetier;
import backend.backend.repository.*;
import backend.backend.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tests;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "logging.level.root=WARN", "debug=false",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.hikari.connection-init-sql=", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"
})
@org.springframework.test.context.ActiveProfiles("demo")
@AutoConfigureMockMvc
class BackendApplicationTests {
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired SujetRepository sujets;
    @Autowired CandidatureRepository candidatures;
    @Autowired UtilisateurService gestionUtilisateurs;
    @Autowired SujetService gestionSujets;
    @Autowired CandidatureService gestionCandidatures;
    @Autowired MockMvc http;
    @Autowired MotDePasseService motsDePasse;
    @Autowired InitialisationDemoService initialisationDemo;
    Utilisateur admin, enseignant, autreEnseignant, etudiant, autreEtudiant, responsable;
    SujetDto sujet;

    @BeforeEach
    void preparer() {
        candidatures.deleteAll(); sujets.deleteAll(); utilisateurs.deleteAll();
        admin = utilisateur("admin", Role.ADMIN);
        enseignant = utilisateur("enseignant", Role.ENSEIGNANT);
        autreEnseignant = utilisateur("autreenseignant", Role.ENSEIGNANT);
        etudiant = utilisateur("etudiant", Role.ETUDIANT);
        autreEtudiant = utilisateur("autreetudiant", Role.ETUDIANT);
        responsable = utilisateur("responsable", Role.RESPONSABLE);
        sujet = gestionSujets.creer(enseignant.getId(), new SujetRequete("Sujet", "Description", 1, true));
    }
    private Utilisateur utilisateur(String nom, Role role) {
        Utilisateur u = new Utilisateur();
        u.setNom(nom); u.setPrenom("Demo"); u.setEmail(nom + "@demo.fr"); u.setRole(role);
        return utilisateurs.saveAndFlush(u);
    }
    private CandidatureDto candidater(Utilisateur u) {
        return gestionCandidatures.creer(u.getId(), new CandidatureRequete(sujet.id(), "Motivation"));
    }
    private void erreur(int statut, org.junit.jupiter.api.function.Executable action) {
        assertEquals(statut, assertThrows(ErreurMetier.class, action).getStatut().value());
    }
    @Test
    void doublonEtContrainteUnique() {
        CandidatureDto c = candidater(etudiant);
        assertEquals(Statut.EN_ATTENTE, c.statut());
        erreur(409, () -> candidater(etudiant));
        Candidature doublon = new Candidature();
        doublon.setEtudiant(etudiant); doublon.setSujet(sujets.findById(sujet.id()).orElseThrow());
        doublon.setMotivation("Autre"); doublon.setDateCandidature(java.time.Instant.now());
        doublon.setStatut(Statut.EN_ATTENTE);
        assertThrows(DataIntegrityViolationException.class, () -> candidatures.saveAndFlush(doublon));
    }
    @Test
    void capaciteDecisionsEtSuppression() {
        CandidatureDto premiere = candidater(etudiant);
        CandidatureDto seconde = candidater(autreEtudiant);
        gestionCandidatures.decider(responsable.getId(), premiere.id(), new DecisionRequete(Statut.ACCEPTEE));
        erreur(409, () -> gestionCandidatures.decider(responsable.getId(), seconde.id(), new DecisionRequete(Statut.ACCEPTEE)));
        erreur(409, () -> gestionCandidatures.decider(responsable.getId(), premiere.id(), new DecisionRequete(Statut.REJETEE)));
        erreur(400, () -> gestionCandidatures.decider(responsable.getId(), seconde.id(), new DecisionRequete(Statut.EN_ATTENTE)));
        assertEquals(Statut.REJETEE, gestionCandidatures.decider(responsable.getId(), seconde.id(), new DecisionRequete(Statut.REJETEE)).statut());
        assertTrue(gestionSujets.lister(etudiant.getId()).isEmpty());
        SujetDto complet = gestionSujets.lister(admin.getId()).getFirst();
        assertEquals(0, complet.placesRestantes()); assertFalse(complet.disponible());
        erreur(409, () -> gestionSujets.supprimer(enseignant.getId(), sujet.id()));
        erreur(409, () -> gestionUtilisateurs.supprimer(admin.getId(), etudiant.getId(), Role.ETUDIANT));
        erreur(409, () -> gestionUtilisateurs.supprimer(admin.getId(), enseignant.getId(), Role.ENSEIGNANT));
        gestionSujets.modifier(enseignant.getId(), sujet.id(), new SujetRequete("Sujet", "Description", 2, true));
        // Un second accepte rend une diminution a une place impossible.
        Utilisateur troisieme = utilisateur("troisieme", Role.ETUDIANT);
        CandidatureDto troisiemeCandidature = candidater(troisieme);
        gestionCandidatures.decider(responsable.getId(), troisiemeCandidature.id(), new DecisionRequete(Statut.ACCEPTEE));
        erreur(409, () -> gestionSujets.modifier(enseignant.getId(), sujet.id(), new SujetRequete("Sujet", "Description", 1, true)));
    }
    @Test
    void sujetFermeEtCandidaturesMultiples() {
        CandidatureDto c = candidater(etudiant);
        gestionSujets.modifier(enseignant.getId(), sujet.id(), new SujetRequete("Sujet", "Description", 1, false));
        erreur(409, () -> candidater(autreEtudiant));
        erreur(409, () -> gestionCandidatures.decider(responsable.getId(), c.id(), new DecisionRequete(Statut.ACCEPTEE)));
        assertEquals(Statut.REJETEE, gestionCandidatures.decider(responsable.getId(), c.id(), new DecisionRequete(Statut.REJETEE)).statut());
        SujetDto autre = gestionSujets.creer(enseignant.getId(), new SujetRequete("Autre", "Description", 2, true));
        gestionCandidatures.creer(etudiant.getId(), new CandidatureRequete(autre.id(), "Motivation"));
        assertEquals(2, gestionCandidatures.lister(etudiant.getId()).size());
    }
    @Test
    void permissionsEtFiltrage() {
        CandidatureDto c = candidater(etudiant);
        for (Utilisateur u : List.of(enseignant, etudiant, responsable))
            erreur(403, () -> gestionUtilisateurs.lister(u.getId(), Role.ETUDIANT));
        for (Utilisateur u : List.of(admin, etudiant, responsable))
            erreur(403, () -> gestionSujets.creer(u.getId(), new SujetRequete("X", "Y", 1, true)));
        for (Utilisateur u : List.of(admin, enseignant, responsable)) erreur(403, () -> candidater(u));
        for (Utilisateur u : List.of(admin, enseignant, etudiant))
            erreur(403, () -> gestionCandidatures.decider(u.getId(), c.id(), new DecisionRequete(Statut.ACCEPTEE)));
        erreur(403, () -> gestionCandidatures.lister(admin.getId()));
        erreur(403, () -> gestionSujets.modifier(autreEnseignant.getId(), sujet.id(), new SujetRequete("X", "Y", 1, true)));
        erreur(403, () -> gestionSujets.supprimer(autreEnseignant.getId(), sujet.id()));
        assertTrue(gestionSujets.lister(autreEnseignant.getId()).isEmpty());
        assertTrue(gestionCandidatures.lister(autreEnseignant.getId()).isEmpty());
        assertTrue(gestionCandidatures.lister(autreEtudiant.getId()).isEmpty());
        assertEquals(1, gestionCandidatures.lister(enseignant.getId()).size());
        assertEquals(1, gestionCandidatures.lister(responsable.getId()).size());
    }
    @Test
    void acceptationsSimultanees() throws Exception {
        CandidatureDto a = candidater(etudiant), b = candidater(autreEtudiant);
        CountDownLatch depart = new CountDownLatch(1);
        try (ExecutorService execution = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> resultats = new ArrayList<>();
            for (CandidatureDto c : List.of(a, b)) resultats.add(execution.submit(() -> {
                depart.await();
                try {
                    gestionCandidatures.decider(responsable.getId(), c.id(), new DecisionRequete(Statut.ACCEPTEE));
                    return 200;
                } catch (ErreurMetier e) { return e.getStatut().value(); }
            }));
            depart.countDown();
            List<Integer> codes = new ArrayList<>();
            for (Future<Integer> resultat : resultats) codes.add(resultat.get(15, TimeUnit.SECONDS));
            Collections.sort(codes);
            assertEquals(List.of(200, 409), codes);
        }
        assertEquals(1, candidatures.countBySujetIdAndStatut(sujet.id(), Statut.ACCEPTEE));
    }
    @Test
    void contratHttpEtValidation() throws Exception {
        http.perform(get("/api/utilisateurs-demo")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber()).andExpect(jsonPath("$[0].role").value("ADMIN"));
        http.perform(get("/api/sujets")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        http.perform(get("/api/sujets").header("X-Utilisateur-Id", "invalide")).andExpect(status().isBadRequest());
        http.perform(get("/api/sujets").header("X-Utilisateur-Id", 999999)).andExpect(status().isBadRequest());
        http.perform(get("/api/etudiants").header("X-Utilisateur-Id", etudiant.getId())).andExpect(status().isForbidden());
        http.perform(post("/api/etudiants").header("X-Utilisateur-Id", admin.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Nom\",\"prenom\":\"Prenom\",\"email\":\"invalide\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        http.perform(post("/api/sujets").header("X-Utilisateur-Id", enseignant.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"Sujet\",\"description\":\"Texte\",\"capaciteMax\":1.5,\"ouvert\":true}"))
                .andExpect(status().isBadRequest());
        http.perform(post("/api/sujets").header("X-Utilisateur-Id", enseignant.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\" \",\"description\":\"Texte\",\"capaciteMax\":0,\"ouvert\":true}"))
                .andExpect(status().isBadRequest());
        http.perform(put("/api/sujets/999999").header("X-Utilisateur-Id", enseignant.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"titre\":\"Sujet\",\"description\":\"Texte\",\"capaciteMax\":1,\"ouvert\":true}"))
                .andExpect(status().isNotFound());
        http.perform(options("/api/sujets").header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "X-Utilisateur-Id,Content-Type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        http.perform(post("/api/candidatures").header("X-Utilisateur-Id", etudiant.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"sujetId\":" + sujet.id() + ",\"motivation\":\"Mon projet\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.dateCandidature").isString()).andExpect(jsonPath("$.etudiantId").value(etudiant.getId()));
    }
    @Test
    void decisionsHttp() throws Exception {
        CandidatureDto c = candidater(etudiant);
        String route = "/api/candidatures/" + c.id() + "/statut";
        http.perform(patch(route).header("X-Utilisateur-Id", enseignant.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"statut\":\"ACCEPTEE\"}")).andExpect(status().isForbidden());
        http.perform(patch(route).header("X-Utilisateur-Id", responsable.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"statut\":\"EN_ATTENTE\"}")).andExpect(status().isBadRequest());
        http.perform(patch(route).header("X-Utilisateur-Id", responsable.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"statut\":\"ACCEPTEE\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"));
        http.perform(patch(route).header("X-Utilisateur-Id", responsable.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"statut\":\"REJETEE\"}")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isString());
    }
    @Test
    void utilisateursCrudEmailEtRoleImpose() throws Exception {
        http.perform(post("/api/etudiants").header("X-Utilisateur-Id", admin.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Nouveau\",\"prenom\":\"Demo\",\"email\":\"nouveau@demo.fr\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("ETUDIANT"));
        erreur(409, () -> gestionUtilisateurs.creer(admin.getId(), Role.ENSEIGNANT,
                new UtilisateurRequete("Nom", "Prenom", "NOUVEAU@demo.fr")));
        UtilisateurDto u = gestionUtilisateurs.creer(admin.getId(), Role.ENSEIGNANT,
                new UtilisateurRequete("Nom", "Prenom", "libre@demo.fr"));
        http.perform(put("/api/enseignants/" + u.id()).header("X-Utilisateur-Id", admin.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Modifie\",\"prenom\":\"Demo\",\"email\":\"libre@demo.fr\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nom").value("Modifie"));
        http.perform(delete("/api/enseignants/" + u.id()).header("X-Utilisateur-Id", admin.getId()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    private org.springframework.test.web.servlet.ResultActions connecter(String email, String motDePasse) throws Exception {
        return http.perform(post("/api/auth/connexion").contentType(MediaType.APPLICATION_JSON)
                .content(new tools.jackson.databind.json.JsonMapper().writeValueAsString(
                        Map.of("email", email, "motDePasse", motDePasse))));
    }

    @Test
    void connexionPourLesQuatreRolesSansEmpreinte() throws Exception {
        for (Utilisateur u : List.of(admin, enseignant, etudiant, responsable)) {
            String empreinte = motsDePasse.encoder("Projet2026!");
            assertTrue(empreinte.startsWith("$2"));
            assertNotEquals("Projet2026!", empreinte);
            u.setMotDePasseHash(empreinte);
            utilisateurs.saveAndFlush(u);
            connecter(u.getEmail(), "Projet2026!").andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(u.getId()))
                    .andExpect(jsonPath("$.role").value(u.getRole().name()))
                    .andExpect(jsonPath("$.motDePasse").doesNotExist())
                    .andExpect(jsonPath("$.motDePasseHash").doesNotExist())
                    .andExpect(jsonPath("$.length()").value(5));
        }
        http.perform(get("/api/utilisateurs-demo")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].motDePasseHash").isEmpty());
        http.perform(get("/api/etudiants").header("X-Utilisateur-Id", admin.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].motDePasseHash").isEmpty());
    }

    @Test
    void identifiantsIncorrectsMemeMessage() throws Exception {
        etudiant.setMotDePasseHash(motsDePasse.encoder("Projet2026!"));
        utilisateurs.saveAndFlush(etudiant);
        for (String email : List.of(etudiant.getEmail(), "inconnu@gdil.example")) {
            connecter(email, "Incorrect!").andExpect(status().isUnauthorized())
                    .andExpect(content().json("{\"message\":\"Adresse email ou mot de passe incorrect.\"}"));
        }
        connecter(etudiant.getEmail(), "x".repeat(1000)).andExpect(status().isUnauthorized());
        connecter(admin.getEmail(), "Projet2026!").andExpect(status().isUnauthorized());
    }

    @Test
    void connexionSaisiesInvalidesEtCors() throws Exception {
        for (String email : List.of("", "  ", "invalide")) {
            connecter(email, "Projet2026!").andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isString());
        }
        for (String motDePasse : List.of("", "   "))
            connecter(etudiant.getEmail(), motDePasse).andExpect(status().isBadRequest());
        for (String corps : List.of("{}", "{\"email\":\"etudiant@demo.fr\"}",
                "{\"email\":null,\"motDePasse\":null}")) {
            http.perform(post("/api/auth/connexion").contentType(MediaType.APPLICATION_JSON).content(corps))
                    .andExpect(status().isBadRequest());
        }
        http.perform(options("/api/auth/connexion").header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,X-Utilisateur-Id"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void normalisationEmailSansModifierMotDePasse() throws Exception {
        etudiant.setMotDePasseHash(motsDePasse.encoder(" Projet2026! "));
        utilisateurs.saveAndFlush(etudiant);
        connecter("  ETUDIANT@DEMO.FR  ", " Projet2026! ").andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("etudiant@demo.fr"));
        connecter(etudiant.getEmail(), "Projet2026!").andExpect(status().isUnauthorized());
    }

    @Test
    void utilisateurCreeParAdminEtModificationConserveEmpreinte() throws Exception {
        for (Role role : List.of(Role.ETUDIANT, Role.ENSEIGNANT)) {
            String route = role == Role.ETUDIANT ? "etudiants" : "enseignants";
            String email = route + "@gdil.example";
            http.perform(post("/api/" + route).header("X-Utilisateur-Id", admin.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nom\":\"Nouveau\",\"prenom\":\"Demo\",\"email\":\"  " + email.toUpperCase(Locale.ROOT) + "  \"}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.motDePasseHash").doesNotExist());
            Utilisateur u = utilisateurs.findByEmail(email).orElseThrow();
            connecter(email, "Projet2026!").andExpect(status().isOk());
            String empreinte = motsDePasse.encoder("AutreMotDePasse!");
            u.setMotDePasseHash(empreinte);
            utilisateurs.saveAndFlush(u);
            http.perform(put("/api/" + route + "/" + u.getId()).header("X-Utilisateur-Id", admin.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nom\":\"Modifie\",\"prenom\":\"Demo\",\"email\":\"  modifie." + email.toUpperCase(Locale.ROOT) + "  \"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.motDePasseHash").doesNotExist());
            assertEquals(empreinte, utilisateurs.findById(u.getId()).orElseThrow().getMotDePasseHash());
            connecter("modifie." + email, "AutreMotDePasse!").andExpect(status().isOk());
            connecter("modifie." + email, "Projet2026!").andExpect(status().isUnauthorized());
        }
    }

    @Test
    void initialisationSeulementDesEmpreintesManquantes() {
        String personnalise = motsDePasse.encoder("Personnel!");
        admin.setMotDePasseHash(personnalise);
        utilisateurs.saveAndFlush(admin);
        initialisationDemo.run(null);
        String premiere = utilisateurs.findById(etudiant.getId()).orElseThrow().getMotDePasseHash();
        assertTrue(motsDePasse.correspond("Projet2026!", premiere));
        initialisationDemo.run(null);
        assertEquals(premiere, utilisateurs.findById(etudiant.getId()).orElseThrow().getMotDePasseHash());
        assertEquals(personnalise, utilisateurs.findById(admin.getId()).orElseThrow().getMotDePasseHash());
    }
}
