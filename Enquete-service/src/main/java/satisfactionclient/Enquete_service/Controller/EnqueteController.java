package satisfactionclient.Enquete_service.Controller;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import satisfactionclient.Enquete_service.Clients.UserServiceClient;
import satisfactionclient.Enquete_service.Dto.EnqueteResponseDTO;
import satisfactionclient.Enquete_service.Dto.QuestionDTO;
import satisfactionclient.Enquete_service.Dto.UserDto;
import satisfactionclient.Enquete_service.Entity.Enquete;
import satisfactionclient.Enquete_service.Entity.Question;
import satisfactionclient.Enquete_service.Entity.StatutEnquete;
import satisfactionclient.Enquete_service.Entity.TypeQuestion;
import satisfactionclient.Enquete_service.Repository.EnqueteRepository;
import satisfactionclient.Enquete_service.Service.Emailservice;
import satisfactionclient.Enquete_service.Service.EnqueteService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/admin/enquetes")
public class EnqueteController {

    @Autowired
    private EnqueteService enqueteService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private EnqueteRepository enqueteRepository;
    @Autowired
    private Emailservice emailService;


    @PostMapping("/create")
    public ResponseEntity<?> creerEnqueteAvecQuestions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Enquete enquete) {

        // Récupérer les infos depuis le JWT
        String userId = jwt.getSubject(); // ou jwt.getClaimAsString("id") si ton token l'inclut explicitement
        List<String> roles = jwt.getClaimAsStringList("roles"); // adapte selon la structure de ton JWT

        // Vérifier le rôle
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : seul un administrateur peut créer une enquête.");
        }

        // Appel à Feign Client pour récupérer les détails de l’utilisateur
        UserDto admin = userServiceClient.getUserById(Long.valueOf(userId)); // Converti si nécessaire

        // Création de l’enquête
        LocalDateTime publicationDate = enquete.getDatePublication();
        LocalDateTime expirationDate = enquete.getDateExpiration();

        Enquete savedEnquete = enqueteService.creerEnqueteAvecQuestionsEtOptions(
                enquete.getTitre(),
                enquete.getDescription(),
                publicationDate,
                expirationDate,
                admin,
                enquete.getQuestions()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(savedEnquete);
    }
    @PostMapping("/{enqueteId}/add-question")
    public ResponseEntity<Question> ajouterQuestion(@PathVariable Long enqueteId,
                                                    @RequestParam String texte,
                                                    @RequestParam List<String> options,
                                                    @RequestParam TypeQuestion type) {
        Question question = enqueteService.ajouterQuestion(enqueteId, texte, options, type);
        return ResponseEntity.ok(question);
    }

    // Récupérer toutes les enquêtes d'un admin
    @GetMapping("/all/{adminId}")
    public ResponseEntity<List<Enquete>> getEnquetesAdmin(@PathVariable Long adminId) {
        List<Enquete> enquetes = enqueteService.getEnquetesAdmin(adminId);
        return ResponseEntity.ok(enquetes);
    }



    // Récupérer les questions d'une enquête
    @GetMapping("/{enqueteId}/questions")
    public ResponseEntity<List<Question>> getQuestionsForEnquete(@PathVariable Long enqueteId) {
        List<Question> questions = enqueteService.getQuestionsForEnquete(enqueteId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping
    public List<EnqueteResponseDTO> getAllEnquetes() {
        return enqueteService.getAllEnquetes();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getEnqueteById(@PathVariable Long id) {
        try {
            EnqueteResponseDTO dto = enqueteService.getEnqueteById(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage()); // String seulement
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur interne du serveur"); // String seulement
        }
    }

    // ✏️ PUT : Modifier une enquête
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateEnquete(@PathVariable Long id, @RequestBody EnqueteResponseDTO dto) {
        enqueteService.updateEnquete(id, dto);
        return ResponseEntity.ok("Enquête mise à jour avec succès.");
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEnquete(@PathVariable Long id) {
        try {
            enqueteService.deleteEnquete(id);
            return ResponseEntity.ok("✅ Enquête supprimée avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Erreur lors de la suppression de l'enquête");
        }
    }
   /* @PostMapping("/create-ia")
    public ResponseEntity<?> creerEnqueteAvecIA(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Enquete enquete) {

        // Extraire les infos du JWT
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");

        // Vérification du rôle
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : seul un administrateur peut créer une enquête.");
        }

        // Récupération des détails de l'utilisateur via Feign
        UserDto admin = userServiceClient.getUserById(Long.valueOf(userId));

        // Appel du service avec génération automatique de questions via IA
        Enquete savedEnquete = enqueteService.creerEnqueteAvecIA(
                enquete.getTitre(),
                enquete.getDescription(),
                enquete.getDatePublication(),
                enquete.getDateExpiration(),
                admin
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(savedEnquete);
    }*/

    @PostMapping("/create-ia")
    public ResponseEntity<?> genererQuestionsIA(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Enquete enquete) {

        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : seul un administrateur peut générer des questions.");
        }

        List<QuestionDTO> questions = enqueteService.genererQuestionsAvecIA(
                enquete.getTitre(), enquete.getDescription()
        );

        return ResponseEntity.ok(Map.of("questions", questions));
    }

    @GetMapping("/call")
    public ResponseEntity<String> testCallToIA() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("titre", "Satisfaction bancaire");
        requestMap.put("description", "Je veux des questions sur la qualité du service bancaire.");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);

        String url = "http://localhost:8000/generate-questions";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }
}

