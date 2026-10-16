package backend.backend.dto;
import jakarta.validation.constraints.*;
public record SujetRequete(@NotBlank String titre, @NotBlank String description, @NotNull @Min(1) Integer capaciteMax, @NotNull Boolean ouvert) {}
