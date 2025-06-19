package satisfactionclient.user_service.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import satisfactionclient.user_service.Config.RabbitMQConfig;
import satisfactionclient.user_service.Dtos.UserDto;
import satisfactionclient.user_service.Entity.ERole;
import satisfactionclient.user_service.Entity.User;
import satisfactionclient.user_service.Repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserMessageListener {
    @Autowired
    private ObjectMapper objectMapper;
    private final Authservice authservice;
    private final UserRepository userRepository;

    public UserMessageListener(Authservice authservice, UserRepository userRepository) {
        this.authservice = authservice;
        this.userRepository = userRepository;
    }

    // 🔹 1. Requête par ID (déjà existante)
    @RabbitListener(queues = {
            RabbitMQConfig.USER_REQUEST_QUEUE,
            RabbitMQConfig.RECLAMATION_REQUEST_QUEUE
    })
    public Message handleUserRequests(Message message) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("📥 Reçu une demande utilisateur avec ID: " + payload);

            if (payload.matches("\\d+")) {
                Long id = Long.parseLong(payload);
                UserDto dto = authservice.getUserdtoById(id);

                if (dto == null) {
                    System.out.println("❌ Aucun utilisateur trouvé avec l'ID: " + id);
                    return null;
                }

                ObjectMapper objectMapper = new ObjectMapper();
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

    // 🔹 2. Requête par rôle (nouvelle)
    @RabbitListener(queues = RabbitMQConfig.USER_BY_ROLE_QUEUE)
    public byte[] handleGetUsersByRole(String role) {
        System.out.println("📥 Reçu demande pour les utilisateurs avec rôle : " + role);
        List<User> users = userRepository.findByRoles_Name(ERole.valueOf(role));
        List<UserDto> dtos = users.stream()
                .map(authservice::convertToDto)
                .toList();

        try {
            return objectMapper.writeValueAsBytes(dtos); // 🔥 on renvoie du JSON brut
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur de sérialisation JSON", e);
        }
    }}
