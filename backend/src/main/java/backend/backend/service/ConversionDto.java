package backend.backend.service;
import backend.backend.dto.*;
import backend.backend.entity.*;
public final class ConversionDto {
    private ConversionDto() {}
    public static String nomComplet(Utilisateur utilisateur) {
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }
    public static UtilisateurDto utilisateur(Utilisateur utilisateur) {
        return new UtilisateurDto(utilisateur.getId(), utilisateur.getNom(), utilisateur.getPrenom(),
                utilisateur.getEmail(), utilisateur.getRole());
    }
    public static SujetDto sujet(Sujet sujet, long acceptes) {
        long places = sujet.getCapaciteMax() - acceptes;
        return new SujetDto(sujet.getId(), sujet.getTitre(), sujet.getDescription(), sujet.getCapaciteMax(),
                sujet.isOuvert(), sujet.getEnseignant().getId(), nomComplet(sujet.getEnseignant()),
                places, sujet.isOuvert() && places > 0);
    }
    public static CandidatureDto candidature(Candidature candidature) {
        return new CandidatureDto(candidature.getId(), candidature.getEtudiant().getId(),
                nomComplet(candidature.getEtudiant()), candidature.getSujet().getId(),
                candidature.getSujet().getTitre(), candidature.getMotivation(),
                candidature.getDateCandidature(), candidature.getStatut());
    }
}
