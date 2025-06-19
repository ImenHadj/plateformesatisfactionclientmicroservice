package satisfactionclient.Enquete_service.Clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import satisfactionclient.Enquete_service.Config.RabbitMQConfig;
import satisfactionclient.Enquete_service.Dto.UserDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class RabbitUserClient {

    private final RabbitTemplate rabbitTemplate;

    public RabbitUserClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<UserDto> getUsersByRole(String role) {
        Object response = rabbitTemplate.convertSendAndReceive(
                RabbitMQConfig.USER_BY_ROLE_QUEUE, role
        );

        if (response instanceof byte[] data) {
            try {
                String json = new String(data, StandardCharsets.UTF_8);
                System.out.println("✅ JSON reçu : " + json);

                ObjectMapper mapper = new ObjectMapper();
                return Arrays.asList(mapper.readValue(json, UserDto[].class)); // 🔥 liste

            } catch (Exception e) {
                System.err.println("❌ Erreur lors du parsing JSON : " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("⚠️ Réponse non gérée : " + (response != null ? response.getClass().getName() : "null"));
        return List.of(); // ou null
    }

    public UserDto getUserById(Long id) {
        System.out.println("➡️ Envoi d’une requête pour l'utilisateur ID: " + id);
        Object response = rabbitTemplate.convertSendAndReceive(
                RabbitMQConfig.USER_REQUEST_QUEUE, id
        );

        if (response == null) {
            System.out.println("❌ Aucun retour reçu via RabbitMQ");
            return null;
        }

        if (response instanceof LinkedHashMap map) {
            System.out.println("✅ Réponse LinkedHashMap reçue via RabbitMQ");

            UserDto dto = new UserDto();
            dto.setId(Long.valueOf(map.get("id").toString()));
            dto.setUsername((String) map.get("username"));
            dto.setEmail((String) map.get("email"));
            dto.setRole((String) map.get("role"));
            dto.setFcmToken((String) map.get("fcmToken"));
            dto.setActive((Boolean) map.get("active"));

            return dto;
        }

        if (response instanceof byte[] data) {
            try {
                String json = new String(data, StandardCharsets.UTF_8);
                System.out.println("✅ JSON reçu : " + json);

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, UserDto.class);

            } catch (Exception e) {
                System.err.println("❌ Erreur lors du parsing JSON : " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("⚠️ Réponse non gérée : " + response.getClass().getName());
        return null;
    }







}
