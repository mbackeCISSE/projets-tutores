package backend.backend.repository;
import backend.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    List<Utilisateur> findByRoleOrderByIdAsc(Role role);
    List<Utilisateur> findAllByOrderByIdAsc();
    java.util.Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
