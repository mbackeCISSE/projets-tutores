package backend.backend.controller;

import backend.backend.dto.UtilisateurDto;
import backend.backend.service.UtilisateurService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("demo")
@RequestMapping("/api/utilisateurs-demo")
public class DemoController {
    private final UtilisateurService utilisateurs;

    public DemoController(UtilisateurService utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @GetMapping
    public List<UtilisateurDto> lister() {
        return utilisateurs.demo();
    }
}
