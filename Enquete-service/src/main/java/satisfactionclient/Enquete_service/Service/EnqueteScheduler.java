package satisfactionclient.Enquete_service.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import satisfactionclient.Enquete_service.Clients.RabbitUserClient;
import satisfactionclient.Enquete_service.Dto.UserDto;
import satisfactionclient.Enquete_service.Entity.Enquete;
import satisfactionclient.Enquete_service.Entity.StatutEnquete;
import satisfactionclient.Enquete_service.Repository.EnqueteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class EnqueteScheduler {


    private final EnqueteRepository enqueteRepository;
   // @Autowired
   // private UserServiceClient userServiceClient;  // Remplace Authservice
    @Autowired
    private RabbitUserClient rabbitTemplate;
    @Autowired
    private Emailservice emailService;
    public EnqueteScheduler(EnqueteRepository enqueteRepository) {
        this.enqueteRepository = enqueteRepository;
    }




    @Scheduled(fixedRate = 60000) // Chaque minute
    public void checkAndPublishEnquetes() {
        LocalDateTime now = LocalDateTime.now();

        List<Enquete> enquetesToPublish = enqueteRepository.findAllByDatePublicationBeforeAndStatut(now, StatutEnquete.BROUILLON);

        for (Enquete enquete : enquetesToPublish) {
            // Marquer comme publiée
            enquete.setStatut(StatutEnquete.PUBLIEE);
            enqueteRepository.save(enquete);

            // Récupérer tous les utilisateurs ROLE_Client
            List<UserDto> clients;
            try {
                clients = rabbitTemplate.getUsersByRole("ROLE_Client");
            } catch (Exception e) {
                // Logger une erreur si le UserService est indisponible
                System.err.println("Erreur lors de la récupération des clients : " + e.getMessage());
                continue;
            }

            for (UserDto client : clients) {
                if (client.getEmail() != null && !client.getEmail().isEmpty()) {
                    String link = "http://localhost:5173/enquete/respond/" + enquete.getId() + "?userId=" + client.getId();
                    emailService.sendEnqueteLink(client.getEmail(), link);
                }
            }
        }

    }}

