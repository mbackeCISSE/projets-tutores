package backend.backend.controller;
import backend.backend.dto.*;
import backend.backend.service.SujetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/sujets")
public class SujetController {
    private final SujetService sujets;
    public SujetController(SujetService sujets) { this.sujets = sujets; }
    @GetMapping
    public List<SujetDto> lister(@RequestHeader("X-Utilisateur-Id") Long acteurId) { return sujets.lister(acteurId); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SujetDto creer(@RequestHeader("X-Utilisateur-Id") Long acteurId,
            @Valid @RequestBody SujetRequete requete) { return sujets.creer(acteurId, requete); }
    @PutMapping("/{id}")
    public SujetDto modifier(@RequestHeader("X-Utilisateur-Id") Long acteurId, @PathVariable Long id,
            @Valid @RequestBody SujetRequete requete) { return sujets.modifier(acteurId, id, requete); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@RequestHeader("X-Utilisateur-Id") Long acteurId, @PathVariable Long id) {
        sujets.supprimer(acteurId, id);
    }
}
