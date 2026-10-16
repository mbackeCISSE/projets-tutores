package backend.backend.controller;
import backend.backend.dto.*;
import backend.backend.service.CandidatureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {
    private final CandidatureService candidatures;
    public CandidatureController(CandidatureService candidatures) { this.candidatures = candidatures; }
    @GetMapping
    public List<CandidatureDto> lister(@RequestHeader("X-Utilisateur-Id") Long acteurId) {
        return candidatures.lister(acteurId);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidatureDto creer(@RequestHeader("X-Utilisateur-Id") Long acteurId,
            @Valid @RequestBody CandidatureRequete requete) { return candidatures.creer(acteurId, requete); }
    @PatchMapping("/{id}/statut")
    public CandidatureDto decider(@RequestHeader("X-Utilisateur-Id") Long acteurId, @PathVariable Long id,
            @Valid @RequestBody DecisionRequete requete) { return candidatures.decider(acteurId, id, requete); }
}
