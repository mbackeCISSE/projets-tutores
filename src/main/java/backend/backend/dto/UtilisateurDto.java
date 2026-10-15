package backend.backend.dto;
import jakarta.validation.constraints.*;
public record UtilisateurDto(Long id, String nom, String prenom, String email, backend.backend.entity.Role role) {}
