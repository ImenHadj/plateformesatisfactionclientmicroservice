package satisfactionclient.reclamation_service.reclamation_service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.UserDto;
import satisfactionclient.reclamation_service.reclamation_service.Entity.Temoignage;
import satisfactionclient.reclamation_service.reclamation_service.Repository.TemoignageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemoignageService {

    @Autowired
    private TemoignageRepository temoignageRepository;

    public Temoignage ajouterTemoignage(String commentaire, int note, UserDto client) {
        Temoignage temoignage = new Temoignage();
        temoignage.setCommentaire(commentaire);
        temoignage.setNote(note);
        temoignage.setUserId(client.getId());
        temoignage.setDateSoumission(LocalDateTime.now());
        return temoignageRepository.save(temoignage);
    }

    public List<Temoignage> getByUserId(Long userId) {
        return temoignageRepository.findByUserId(userId);
    }

    public List<Temoignage> getAll() {
        return temoignageRepository.findAllByOrderByDateSoumissionDesc();
    }
}
