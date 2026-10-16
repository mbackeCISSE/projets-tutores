package backend.backend.exception;
import backend.backend.dto.ErreurDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingRequestHeaderException;
@RestControllerAdvice
public class GestionErreurs {
    @ExceptionHandler(ErreurMetier.class)
    public ResponseEntity<ErreurDto> metier(ErreurMetier erreur) {
        return ResponseEntity.status(erreur.getStatut()).body(new ErreurDto(erreur.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurDto> validation(MethodArgumentNotValidException erreur) {
        String message = erreur.getBindingResult().getFieldErrors().stream()
                .map(champ -> champ.getField() + " : " + champ.getDefaultMessage())
                .sorted().collect(java.util.stream.Collectors.joining(" ; "));
        return ResponseEntity.badRequest().body(new ErreurDto(message));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class})
    public ResponseEntity<ErreurDto> requeteInvalide(Exception erreur) {
        return ResponseEntity.badRequest().body(new ErreurDto("Corps, identifiant ou en-tete X-Utilisateur-Id invalide."));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurDto> conflit(DataIntegrityViolationException erreur) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErreurDto("Conflit : email ou candidature deja utilise, ou ressource encore liee."));
    }
}
