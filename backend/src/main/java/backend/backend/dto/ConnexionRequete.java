package backend.backend.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record ConnexionRequete(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank String motDePasse) {
    public ConnexionRequete {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
