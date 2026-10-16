package backend.backend.repository;
import backend.backend.entity.Sujet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface SujetRepository extends JpaRepository<Sujet, Long> {
    List<Sujet> findAllByOrderByIdAsc();
    List<Sujet> findByEnseignantIdOrderByIdAsc(Long enseignantId);
    boolean existsByEnseignantId(Long enseignantId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sujet s where s.id = :id")
    Optional<Sujet> trouverAvecVerrou(@Param("id") Long id);
}
