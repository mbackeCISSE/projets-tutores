package backend.backend.service;
import backend.backend.dto.*;
import backend.backend.entity.*;
import backend.backend.exception.ErreurMetier;
import backend.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class SujetService {
    private final SujetRepository sujets;
    private final CandidatureRepository candidatures;
    private final AccesService acces;
    public SujetService(SujetRepository sujets, CandidatureRepository candidatures, AccesService acces) {
        this.sujets = sujets; this.candidatures = candidatures; this.acces = acces;
    }
    @Transactional(readOnly = true)
    public List<SujetDto> lister(Long acteurId) {
        Utilisateur acteur = acces.charger(acteurId);
        List<Sujet> liste = acteur.getRole() == Role.ENSEIGNANT
                ? sujets.findByEnseignantIdOrderByIdAsc(acteurId) : sujets.findAllByOrderByIdAsc();
        return liste.stream().map(this::convertir)
                .filter(s -> acteur.getRole() != Role.ETUDIANT || s.disponible()).toList();
    }
    public SujetDto creer(Long acteurId, SujetRequete requete) {
        Utilisateur acteur = acces.charger(acteurId);
        acces.exiger(acteur, Role.ENSEIGNANT);
        Sujet sujet = new Sujet();
        sujet.setEnseignant(acteur);
        remplir(sujet, requete);
        return convertir(sujets.saveAndFlush(sujet));
    }
    public SujetDto modifier(Long acteurId, Long id, SujetRequete requete) {
        Utilisateur acteur = acces.charger(acteurId);
        acces.exiger(acteur, Role.ENSEIGNANT);
        Sujet sujet = verrouiller(id);
        verifierProprietaire(acteur, sujet);
        if (requete.capaciteMax() < nombreAcceptes(id))
            throw new ErreurMetier(HttpStatus.CONFLICT, "Capacite inferieure au nombre d'etudiants acceptes.");
        remplir(sujet, requete);
        return convertir(sujets.saveAndFlush(sujet));
    }
    public void supprimer(Long acteurId, Long id) {
        Utilisateur acteur = acces.charger(acteurId);
        acces.exiger(acteur, Role.ENSEIGNANT);
        Sujet sujet = verrouiller(id);
        verifierProprietaire(acteur, sujet);
        if (candidatures.existsBySujetId(id))
            throw new ErreurMetier(HttpStatus.CONFLICT, "Ce sujet possede des candidatures.");
        sujets.delete(sujet);
        sujets.flush();
    }
    public Sujet verrouiller(Long id) {
        return sujets.trouverAvecVerrou(id).orElseThrow(() ->
                new ErreurMetier(HttpStatus.NOT_FOUND, "Sujet absent."));
    }
    public long nombreAcceptes(Long id) {
        return candidatures.countBySujetIdAndStatut(id, Statut.ACCEPTEE);
    }
    private SujetDto convertir(Sujet sujet) { return ConversionDto.sujet(sujet, nombreAcceptes(sujet.getId())); }
    private void verifierProprietaire(Utilisateur acteur, Sujet sujet) {
        if (!sujet.getEnseignant().getId().equals(acteur.getId()))
            throw new ErreurMetier(HttpStatus.FORBIDDEN, "Ce sujet appartient a un autre enseignant.");
    }
    private void remplir(Sujet sujet, SujetRequete requete) {
        sujet.setTitre(requete.titre().strip());
        sujet.setDescription(requete.description().strip());
        sujet.setCapaciteMax(requete.capaciteMax());
        sujet.setOuvert(requete.ouvert());
    }
}
