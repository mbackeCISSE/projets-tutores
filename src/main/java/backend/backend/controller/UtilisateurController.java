package backend.backend.controller;
import backend.backend.dto.*;
import backend.backend.entity.Role;
import backend.backend.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api")
public class UtilisateurController {
    private final UtilisateurService utilisateurs;
    public UtilisateurController(UtilisateurService utilisateurs) { this.utilisateurs = utilisateurs; }

    @GetMapping("/etudiants")
    public List<UtilisateurDto> listerEtudiants(@RequestHeader("X-Utilisateur-Id") Long acteurId) {
        return utilisateurs.lister(acteurId, Role.ETUDIANT);
    }
    @PostMapping("/etudiants")
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurDto creerEtudiants(@RequestHeader("X-Utilisateur-Id") Long acteurId,
            @Valid @RequestBody UtilisateurRequete requete) {
        return utilisateurs.creer(acteurId, Role.ETUDIANT, requete);
    }
    @PutMapping("/etudiants/{id}")
    public UtilisateurDto modifierEtudiants(@RequestHeader("X-Utilisateur-Id") Long acteurId,
            @PathVariable Long id, @Valid @RequestBody UtilisateurRequete requete) {
        return utilisateurs.modifier(acteurId, id, Role.ETUDIANT, requete);
    }
    @DeleteMapping("/etudiants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerEtudiants(@RequestHeader("X-Utilisateur-Id") Long acteurId, @PathVariable Long id) {
        utilisateurs.supprimer(acteurId, id, Role.ETUDIANT);
    }


    @GetMapping("/enseignants")
    public List<UtilisateurDto> listerEnseignants(@RequestHeader("X-Utilisateur-Id") Long acteurId) {
        return utilisateurs.lister(acteurId, Role.ENSEIGNANT);
    }
    @PostMapping("/enseignants")
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurDto creerEnseignants(@RequestHeader("X-Utilisateur-Id") Long acteurId,
            @Valid @RequestBody UtilisateurRequete requete) {
        return utilisateurs.creer(acteurId, Role.ENSEIGNANT, requete);
    }
    @PutMapping("/enseignants/{id}")
    public UtilisateurDto modifierEnseignants(@RequestHeader("X-Utilisateur-Id") Long acteurId,
            @PathVariable Long id, @Valid @RequestBody UtilisateurRequete requete) {
        return utilisateurs.modifier(acteurId, id, Role.ENSEIGNANT, requete);
    }
    @DeleteMapping("/enseignants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerEnseignants(@RequestHeader("X-Utilisateur-Id") Long acteurId, @PathVariable Long id) {
        utilisateurs.supprimer(acteurId, id, Role.ENSEIGNANT);
    }
}
