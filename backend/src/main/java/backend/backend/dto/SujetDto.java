package backend.backend.dto;
import jakarta.validation.constraints.*;
public record SujetDto(Long id, String titre, String description, int capaciteMax, boolean ouvert, Long enseignantId, String enseignantNom, long placesRestantes, boolean disponible) {}
