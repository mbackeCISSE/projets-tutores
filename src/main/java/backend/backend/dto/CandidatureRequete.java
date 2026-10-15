package backend.backend.dto;
import jakarta.validation.constraints.*;
public record CandidatureRequete(@NotNull @Positive Long sujetId, @NotBlank String motivation) {}
