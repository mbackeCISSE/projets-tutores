package backend.backend.service;

import backend.backend.entity.Utilisateur;
import backend.backend.repository.UtilisateurRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("demo")
public class InitialisationDemoService implements ApplicationRunner {
    public static final String MOT_DE_PASSE_INITIAL = "Projet2026!";
    private final UtilisateurRepository utilisateurs;
    private final MotDePasseService motsDePasse;

    public InitialisationDemoService(UtilisateurRepository utilisateurs, MotDePasseService motsDePasse) {
        this.utilisateurs = utilisateurs;
        this.motsDePasse = motsDePasse;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        for (Utilisateur utilisateur : utilisateurs.findAll()) {
            String empreinte = utilisateur.getMotDePasseHash();
            if (empreinte == null || empreinte.isBlank()) {
                utilisateur.setMotDePasseHash(motsDePasse.encoder(MOT_DE_PASSE_INITIAL));
            }
        }
    }
}
