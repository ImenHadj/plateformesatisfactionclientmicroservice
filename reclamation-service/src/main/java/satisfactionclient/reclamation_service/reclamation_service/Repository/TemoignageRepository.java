package satisfactionclient.reclamation_service.reclamation_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import satisfactionclient.reclamation_service.reclamation_service.Entity.Temoignage;

import java.util.List;

@Repository
public interface TemoignageRepository extends JpaRepository<Temoignage, Long> {
    List<Temoignage> findAllByOrderByDateSoumissionDesc();
    List<Temoignage> findByUserId(Long userId);

}
