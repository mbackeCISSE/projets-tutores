package backend.backend.controller;

import backend.backend.dto.ConnexionRequete;
import backend.backend.dto.UtilisateurDto;
import backend.backend.service.ConnexionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ConnexionController {
    private final ConnexionService connexion;

    public ConnexionController(ConnexionService connexion) {
        this.connexion = connexion;
    }

    @PostMapping("/connexion")
    public UtilisateurDto connecter(@Valid @RequestBody ConnexionRequete requete) {
        return connexion.connecter(requete);
    }
}
