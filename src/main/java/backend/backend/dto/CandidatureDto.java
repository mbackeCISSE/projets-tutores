package backend.backend.dto;
import jakarta.validation.constraints.*;
public record CandidatureDto(Long id, Long etudiantId, String etudiantNom, Long sujetId, String sujetTitre, String motivation, java.time.Instant dateCandidature, backend.backend.entity.Statut statut) {}
