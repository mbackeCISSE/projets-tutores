package backend.backend.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record UtilisateurRequete(@NotBlank String nom, @NotBlank String prenom,
                                 @NotBlank @Email @Size(max = 254) String email) {
    public UtilisateurRequete {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
