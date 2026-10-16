package backend.backend.service;
import backend.backend.dto.*;
import backend.backend.entity.*;
import backend.backend.exception.ErreurMetier;
import backend.backend.repository.CandidatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.List;
@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class CandidatureService {
    private final CandidatureRepository candidatures;
    private final SujetService sujets;
    private final AccesService acces;
    public CandidatureService(CandidatureRepository candidatures, SujetService sujets, AccesService acces) {
        this.candidatures = candidatures; this.sujets = sujets; this.acces = acces;
    }
    @Transactional(readOnly = true)
    public List<CandidatureDto> lister(Long acteurId) {
        Utilisateur acteur = acces.charger(acteurId);
        acces.exiger(acteur, Role.ETUDIANT, Role.ENSEIGNANT, Role.RESPONSABLE);
        List<Candidature> liste = switch (acteur.getRole()) {
            case ETUDIANT -> candidatures.findByEtudiantIdOrderByIdAsc(acteurId);
            case ENSEIGNANT -> candidatures.findBySujetEnseignantIdOrderByIdAsc(acteurId);
            default -> candidatures.findAllByOrderByIdAsc();
        };
        return liste.stream().map(ConversionDto::candidature).toList();
    }
    public CandidatureDto creer(Long acteurId, CandidatureRequete requete) {
        Utilisateur acteur = acces.charger(acteurId);
        acces.exiger(acteur, Role.ETUDIANT);
        Sujet sujet = sujets.verrouiller(requete.sujetId());
        if (candidatures.existsByEtudiantIdAndSujetId(acteurId, sujet.getId()))
            throw new ErreurMetier(HttpStatus.CONFLICT, "Vous avez deja candidate a ce sujet.");
        verifierDisponible(sujet);
        Candidature candidature = new Candidature();
        candidature.setEtudiant(acteur);
        candidature.setSujet(sujet);
        candidature.setMotivation(requete.motivation().strip());
        candidature.setDateCandidature(Instant.now());
        candidature.setStatut(Statut.EN_ATTENTE);
        return ConversionDto.candidature(candidatures.saveAndFlush(candidature));
    }
    public CandidatureDto decider(Long acteurId, Long id, DecisionRequete requete) {
        acces.exiger(acces.charger(acteurId), Role.RESPONSABLE);
        if (requete.statut() != Statut.ACCEPTEE && requete.statut() != Statut.REJETEE)
            throw new ErreurMetier(HttpStatus.BAD_REQUEST, "Decision attendue : ACCEPTEE ou REJETEE.");
        // Lire seulement l'identifiant avant le verrou evite de conserver un statut perime en contexte JPA.
        Long sujetId = candidatures.findSujetId(id).orElseThrow(() ->
                new ErreurMetier(HttpStatus.NOT_FOUND, "Candidature absente."));
        Sujet sujet = sujets.verrouiller(sujetId);
        Candidature candidature = candidatures.findById(id).orElseThrow(() ->
                new ErreurMetier(HttpStatus.NOT_FOUND, "Candidature absente."));
        if (candidature.getStatut() != Statut.EN_ATTENTE)
            throw new ErreurMetier(HttpStatus.CONFLICT, "Cette candidature a deja ete traitee.");
        if (requete.statut() == Statut.ACCEPTEE) verifierDisponible(sujet);
        candidature.setStatut(requete.statut());
        return ConversionDto.candidature(candidatures.saveAndFlush(candidature));
    }
    private void verifierDisponible(Sujet sujet) {
        if (!sujet.isOuvert() || sujets.nombreAcceptes(sujet.getId()) >= sujet.getCapaciteMax())
            throw new ErreurMetier(HttpStatus.CONFLICT, "Ce sujet est ferme ou complet.");
    }
}
