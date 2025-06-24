package satisfactionclient.Enquete_service.Controller;


import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import satisfactionclient.Enquete_service.Clients.RabbitUserClient;
import satisfactionclient.Enquete_service.Dto.EnqueteResponseDTO;
import satisfactionclient.Enquete_service.Dto.QuestionDTO;
import satisfactionclient.Enquete_service.Dto.ReponseDTO;
import satisfactionclient.Enquete_service.Dto.UserDto;
import satisfactionclient.Enquete_service.Entity.Enquete;
import satisfactionclient.Enquete_service.Repository.EnqueteRepository;
import satisfactionclient.Enquete_service.Repository.QuestionRepository;
import satisfactionclient.Enquete_service.Service.EnqueteService;

import java.util.List;
import java.util.stream.Collectors;
//@CrossOrigin(origins = "http://localhost:5173")

@RestController
@RequestMapping("/enquete")
public class EnquetePublicController {


    @Autowired
    private EnqueteService enqueteService;
    @Autowired
    private RabbitUserClient rabbitTemplate;

    @GetMapping("respond/{id}")
    public ResponseEntity<EnqueteResponseDTO> getEnqueteWithQuestions(@PathVariable Long id) {
        Enquete enquete = enqueteService.getEnqueteWithQuestions(id);

        EnqueteResponseDTO enqueteResponseDTO = new EnqueteResponseDTO();
        enqueteResponseDTO.setTitre(enquete.getTitre());
        enqueteResponseDTO.setDescription(enquete.getDescription());
        enqueteResponseDTO.setDateExpiration(enquete.getDateExpiration()); // ✅ Ajout ici

        List<QuestionDTO> questionDTOs = enquete.getQuestions().stream()
                .map(question -> {
                    QuestionDTO dto = new QuestionDTO();
                    dto.setId(question.getId()); // Ajout de l'ID
                    dto.setTexte(question.getTexte());
                    dto.setType(question.getType());
                    dto.setOptions(question.getOptions()); // Si vous avez des options
                    return dto;
                })
                .collect(Collectors.toList());

        enqueteResponseDTO.setQuestions(questionDTOs);

        return ResponseEntity.ok(enqueteResponseDTO);
    }



    @PermitAll

    @PostMapping("/respond/{enqueteId}")
    public ResponseEntity<String> repondreEnquete(
            @PathVariable Long enqueteId,
            @RequestParam Long userId,
            @RequestBody List<ReponseDTO> reponsesDTO) {
        try {
            enqueteService.enregistrerReponses(enqueteId, userId, reponsesDTO);
            return ResponseEntity.ok("Réponses enregistrées");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/publiees")
    public ResponseEntity<List<EnqueteResponseDTO>> getEnquetesPubliees() {
        List<Enquete> enquetesPubliees = enqueteService.getEnquetesPubliees();

        List<EnqueteResponseDTO> dtoList = enquetesPubliees.stream().map(enquete -> {
            EnqueteResponseDTO dto = new EnqueteResponseDTO();
            dto.setId(enquete.getId());  // <== Ajoute cette ligne pour transmettre l'id
            dto.setTitre(enquete.getTitre());
            dto.setDescription(enquete.getDescription());
            dto.setDateExpiration(enquete.getDateExpiration());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/repond/{enqueteId}")
    public ResponseEntity<?> repondreEnquete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long enqueteId,
            @RequestBody List<ReponseDTO> reponsesDTO) {

        List<String> roles = jwt.getClaimAsStringList("roles");
        String userId = jwt.getSubject();

        if (roles == null || !roles.contains("ROLE_Client")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : rôle non autorisé.");
        }

        UserDto client = rabbitTemplate.getUserById(Long.valueOf(userId));

        System.out.println("🔍 Client récupéré : " + (client != null ? client.getUsername() : "null"));

        if (client == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de l'utilisateur.");
        }

        try {
            enqueteService.enregistrerReponses(enqueteId, client.getId(), reponsesDTO);
            return ResponseEntity.ok("Réponses enregistrées avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


}

