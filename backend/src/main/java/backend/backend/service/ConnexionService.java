package backend.backend.service;

import backend.backend.dto.ConnexionRequete;
import backend.backend.dto.UtilisateurDto;
import backend.backend.entity.Utilisateur;
import backend.backend.exception.ErreurMetier;
import backend.backend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnexionService {
    private final UtilisateurRepository utilisateurs;
    private final MotDePasseService motsDePasse;

    public ConnexionService(UtilisateurRepository utilisateurs, MotDePasseService motsDePasse) {
        this.utilisateurs = utilisateurs;
        this.motsDePasse = motsDePasse;
    }

    @Transactional(readOnly = true)
    public UtilisateurDto connecter(ConnexionRequete requete) {
        Utilisateur utilisateur = utilisateurs.findByEmail(requete.email()).orElse(null);
        if (utilisateur == null || !motsDePasse.correspond(requete.motDePasse(), utilisateur.getMotDePasseHash())) {
            throw new ErreurMetier(HttpStatus.UNAUTHORIZED, "Adresse email ou mot de passe incorrect.");
        }
        return ConversionDto.utilisateur(utilisateur);
    }
}
