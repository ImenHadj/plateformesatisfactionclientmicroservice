package satisfactionclient.Enquete_service.Controller;


import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import satisfactionclient.Enquete_service.Dto.EnqueteResponseDTO;
import satisfactionclient.Enquete_service.Dto.QuestionDTO;
import satisfactionclient.Enquete_service.Dto.ReponseDTO;
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



}

