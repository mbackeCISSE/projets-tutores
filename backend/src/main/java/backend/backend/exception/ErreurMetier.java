package backend.backend.exception;
import org.springframework.http.HttpStatus;
public class ErreurMetier extends RuntimeException {
    private final HttpStatus statut;
    public ErreurMetier(HttpStatus statut, String message) {
        super(message);
        this.statut = statut;
    }
    public HttpStatus getStatut() { return statut; }
}
