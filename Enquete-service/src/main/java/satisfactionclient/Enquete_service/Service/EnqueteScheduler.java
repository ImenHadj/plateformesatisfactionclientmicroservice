package satisfactionclient.Enquete_service.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import satisfactionclient.Enquete_service.Clients.UserServiceClient;
import satisfactionclient.Enquete_service.Dto.UserDto;
import satisfactionclient.Enquete_service.Entity.Enquete;
import satisfactionclient.Enquete_service.Entity.StatutEnquete;
import satisfactionclient.Enquete_service.Repository.EnqueteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class EnqueteScheduler {


    private final EnqueteRepository enqueteRepository;
    @Autowired
    private UserServiceClient userServiceClient;  // Remplace Authservice

    @Autowired
    private Emailservice emailService;
    public EnqueteScheduler(EnqueteRepository enqueteRepository) {
        this.enqueteRepository = enqueteRepository;
    }




    @Scheduled(fixedRate = 60000) // Vérification toutes les minutes
    public void checkAndPublishEnquetes() {
        LocalDateTime now = LocalDateTime.now();

        List<Enquete> enquetesToPublish = enqueteRepository.findAllByDatePublicationBeforeAndStatut(now, StatutEnquete.BROUILLON);

        for (Enquete enquete : enquetesToPublish) {
            enquete.setStatut(StatutEnquete.PUBLIEE);
            enqueteRepository.save(enquete);

            List<UserDto> clients = userServiceClient.getUsersByRole("ROLE_Client");

            for (UserDto client : clients) {
                String enqueteLink = "http://localhost:5173/enquete/respond/" + enquete.getId() + "?userId=" + client.getId();
                emailService.sendEnqueteLink(client.getEmail(), enqueteLink);
            }
        }
        }

}

