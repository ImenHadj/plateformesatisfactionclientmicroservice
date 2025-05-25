package satisfactionclient.reclamation_service.reclamation_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import satisfactionclient.reclamation_service.reclamation_service.Entity.Reclamation;

import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByUserId(Long userId);
}
