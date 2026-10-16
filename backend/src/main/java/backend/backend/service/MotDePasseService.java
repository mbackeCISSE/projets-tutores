package backend.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class MotDePasseService {
    private final BCryptPasswordEncoder encodeur = new BCryptPasswordEncoder();

    public String encoder(String motDePasse) {
        return encodeur.encode(motDePasse);
    }

    public boolean correspond(String motDePasse, String empreinte) {
        if (empreinte == null || empreinte.isBlank()) return false;
        try {
            return encodeur.matches(motDePasse, empreinte);
        } catch (IllegalArgumentException erreur) {
            return false;
        }
    }
}
