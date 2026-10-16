package backend.backend.dto;
import jakarta.validation.constraints.*;
public record DecisionRequete(@NotNull backend.backend.entity.Statut statut) {}
