package satisfactionclient.reclamation_service.reclamation_service.Service;


import org.springframework.stereotype.Service;
import satisfactionclient.reclamation_service.reclamation_service.Clients.UserServiceClient;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.UserDto;
import satisfactionclient.reclamation_service.reclamation_service.Entity.Reclamation;
import satisfactionclient.reclamation_service.reclamation_service.Entity.StatutHistorique;
import satisfactionclient.reclamation_service.reclamation_service.Entity.StatutReclamation;
import satisfactionclient.reclamation_service.reclamation_service.Repository.ReclamationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;




@Service
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final UserServiceClient userServiceClient;

    public ReclamationService(ReclamationRepository reclamationRepository, UserServiceClient userServiceClient) {
        this.reclamationRepository = reclamationRepository;
        this.userServiceClient = userServiceClient;
    }


    public Reclamation creerReclamationAvecStatut(String contenu, UserDto client) {
        Reclamation reclamation = new Reclamation();
        reclamation.setContenu(contenu);
        reclamation.setUserId(client.getId());
        reclamation.setDateSoumission(LocalDateTime.now());
        reclamation.setStatut(StatutReclamation.EN_ATTENTE);

        // Création de l’historique
        StatutHistorique historique = new StatutHistorique();
        historique.setDateModification(LocalDateTime.now());
        historique.setNouveauStatut(StatutReclamation.EN_ATTENTE);
        historique.setReclamation(reclamation); // lien inverse

        // Ajout à la liste (initialisée dans l'entité)
        reclamation.getHistorique().add(historique);

        return reclamationRepository.save(reclamation);
    }


    public Reclamation mettreAJourStatutParAgent(Long reclamationId, StatutReclamation newStatut, UserDto agent) {
        Optional<Reclamation> optional = reclamationRepository.findById(reclamationId);
        if (optional.isEmpty()) throw new RuntimeException("Réclamation non trouvée");

        Reclamation r = optional.get();

        StatutHistorique historique = new StatutHistorique();
        historique.setAncienStatut(r.getStatut());
        historique.setNouveauStatut(newStatut);
        historique.setDateModification(LocalDateTime.now());
        historique.setReclamation(r);
        // facultatif : log de l’agent : historique.setModifiePar(agent.getUsername());

        r.mettreAJourStatut(newStatut);
        r.getHistorique().add(historique);

        return reclamationRepository.save(r);
    }


    public List<Reclamation> getReclamationsByUser(Long userId) {
        return reclamationRepository.findByUserId(userId);
    }

    public List<Reclamation> getAll() {
        return reclamationRepository.findAll();
    }

    public Reclamation getById(Long id) {
        return reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));
    }
}
