package satisfactionclient.user_service.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import satisfactionclient.user_service.Config.RabbitMQConfig;
import satisfactionclient.user_service.Dtos.UserDto;
import satisfactionclient.user_service.Entity.ERole;

import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class UserMessageListener {

    private final Authservice authservice;

    public UserMessageListener(Authservice authservice) {
        this.authservice = authservice;
    }

    @RabbitListener(queues = {
            RabbitMQConfig.USER_REQUEST_QUEUE,
            RabbitMQConfig.RECLAMATION_REQUEST_QUEUE // ✅ Ajout ici
    })
    public Message handleUserRequests(Message message) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("📥 Reçu une demande utilisateur avec ID: " + payload);

            ObjectMapper objectMapper = new ObjectMapper();

            if (payload.matches("\\d+")) {
                Long id = Long.parseLong(payload);
                UserDto dto = authservice.getUserdtoById(id);

                if (dto == null) {
                    System.out.println("❌ Aucun utilisateur trouvé avec l'ID: " + id);
                    return null;
                }

                System.out.println("✅ Utilisateur trouvé: " + dto.getUsername() + ", rôle: " + dto.getRole());

                String json = objectMapper.writeValueAsString(dto);

                return MessageBuilder
                        .withBody(json.getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}

