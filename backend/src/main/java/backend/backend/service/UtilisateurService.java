package backend.backend.service;
import backend.backend.dto.*;
import backend.backend.entity.*;
import backend.backend.exception.ErreurMetier;
import backend.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class UtilisateurService {
    private final UtilisateurRepository utilisateurs;
    private final SujetRepository sujets;
    private final CandidatureRepository candidatures;
    private final AccesService acces;
    private final MotDePasseService motsDePasse;
    private final org.springframework.core.env.Environment environnement;
    public UtilisateurService(UtilisateurRepository utilisateurs, SujetRepository sujets,
                              CandidatureRepository candidatures, AccesService acces,
                              MotDePasseService motsDePasse, org.springframework.core.env.Environment environnement) {
        this.utilisateurs = utilisateurs; this.sujets = sujets;
        this.candidatures = candidatures; this.acces = acces;
        this.motsDePasse = motsDePasse; this.environnement = environnement;
    }
    @Transactional(readOnly = true)
    public List<UtilisateurDto> demo() {
        return utilisateurs.findAllByOrderByIdAsc().stream().map(ConversionDto::utilisateur).toList();
    }
    @Transactional(readOnly = true)
    public List<UtilisateurDto> lister(Long acteurId, Role role) {
        acces.exiger(acces.charger(acteurId), Role.ADMIN);
        return utilisateurs.findByRoleOrderByIdAsc(role).stream().map(ConversionDto::utilisateur).toList();
    }
    public UtilisateurDto creer(Long acteurId, Role role, UtilisateurRequete requete) {
        acces.exiger(acces.charger(acteurId), Role.ADMIN);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setRole(role);
        if (environnement.acceptsProfiles(org.springframework.core.env.Profiles.of("demo"))) {
            utilisateur.setMotDePasseHash(motsDePasse.encoder(InitialisationDemoService.MOT_DE_PASSE_INITIAL));
        }
        remplir(utilisateur, requete);
        return ConversionDto.utilisateur(utilisateurs.saveAndFlush(utilisateur));
    }
    public UtilisateurDto modifier(Long acteurId, Long id, Role role, UtilisateurRequete requete) {
        acces.exiger(acces.charger(acteurId), Role.ADMIN);
        Utilisateur utilisateur = trouver(id, role);
        remplir(utilisateur, requete);
        return ConversionDto.utilisateur(utilisateurs.saveAndFlush(utilisateur));
    }
    public void supprimer(Long acteurId, Long id, Role role) {
        acces.exiger(acces.charger(acteurId), Role.ADMIN);
        Utilisateur utilisateur = trouver(id, role);
        if (sujets.existsByEnseignantId(id) || candidatures.existsByEtudiantId(id))
            throw new ErreurMetier(HttpStatus.CONFLICT, "Cet utilisateur est lie a des sujets ou candidatures.");
        utilisateurs.delete(utilisateur);
        utilisateurs.flush();
    }
    private Utilisateur trouver(Long id, Role role) {
        return utilisateurs.findById(id).filter(u -> u.getRole() == role).orElseThrow(() ->
                new ErreurMetier(HttpStatus.NOT_FOUND, "Utilisateur absent pour ce role."));
    }
    private void remplir(Utilisateur utilisateur, UtilisateurRequete requete) {
        String email = requete.email().trim().toLowerCase(Locale.ROOT);
        boolean existe = utilisateur.getId() == null ? utilisateurs.existsByEmailIgnoreCase(email)
                : utilisateurs.existsByEmailIgnoreCaseAndIdNot(email, utilisateur.getId());
        if (existe) throw new ErreurMetier(HttpStatus.CONFLICT, "Cet email est deja utilise.");
        utilisateur.setNom(requete.nom().strip());
        utilisateur.setPrenom(requete.prenom().strip());
        utilisateur.setEmail(email);
    }
}
