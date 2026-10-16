package backend.backend.repository;
import backend.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    @org.springframework.data.jpa.repository.Query("select c.sujet.id from Candidature c where c.id = :id")
    java.util.Optional<Long> findSujetId(@org.springframework.data.repository.query.Param("id") Long id);
    List<Candidature> findAllByOrderByIdAsc();
    List<Candidature> findByEtudiantIdOrderByIdAsc(Long etudiantId);
    List<Candidature> findBySujetEnseignantIdOrderByIdAsc(Long enseignantId);
    long countBySujetIdAndStatut(Long sujetId, Statut statut);
    boolean existsByEtudiantIdAndSujetId(Long etudiantId, Long sujetId);
    boolean existsBySujetId(Long sujetId);
    boolean existsByEtudiantId(Long etudiantId);
}
