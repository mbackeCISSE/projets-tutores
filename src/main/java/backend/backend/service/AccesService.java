package backend.backend.service;
import backend.backend.entity.*;
import backend.backend.exception.ErreurMetier;
import backend.backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
@Service
public class AccesService {
    private final UtilisateurRepository utilisateurs;
    public AccesService(UtilisateurRepository utilisateurs) { this.utilisateurs = utilisateurs; }
    public Utilisateur charger(Long id) {
        if (id == null || id < 1) throw new ErreurMetier(HttpStatus.BAD_REQUEST, "Utilisateur selectionne invalide.");
        return utilisateurs.findById(id).orElseThrow(() ->
                new ErreurMetier(HttpStatus.BAD_REQUEST, "Utilisateur selectionne invalide."));
    }
    public void exiger(Utilisateur utilisateur, Role... roles) {
        if (!Arrays.asList(roles).contains(utilisateur.getRole()))
            throw new ErreurMetier(HttpStatus.FORBIDDEN, "Acces interdit pour ce role.");
    }
}
