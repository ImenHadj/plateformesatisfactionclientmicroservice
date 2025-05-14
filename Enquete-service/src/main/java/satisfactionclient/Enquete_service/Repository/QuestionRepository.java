package satisfactionclient.Enquete_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import satisfactionclient.Enquete_service.Entity.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
        List<Question> findByEnqueteId(Long enqueteId);
    }

