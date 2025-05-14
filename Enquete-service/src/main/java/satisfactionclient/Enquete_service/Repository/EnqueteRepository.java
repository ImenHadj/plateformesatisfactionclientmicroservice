package satisfactionclient.Enquete_service.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import satisfactionclient.Enquete_service.Entity.Enquete;
import satisfactionclient.Enquete_service.Entity.StatutEnquete;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnqueteRepository extends JpaRepository<Enquete, Long> {
    List<Enquete> findByAdminId(Long adminId);
    List<Enquete> findByStatutAndDateExpirationAfter(String statut, LocalDateTime date);  // Trouver les enquêtes publiées qui ne sont pas expirées
    List<Enquete> findAllByDatePublicationBeforeAndStatut(LocalDateTime date, StatutEnquete statut);
    @Query("SELECT e FROM Enquete e LEFT JOIN FETCH e.questions WHERE e.id = :id")
    Optional<Enquete> findByIdWithQuestions(@Param("id") Long id);
}


