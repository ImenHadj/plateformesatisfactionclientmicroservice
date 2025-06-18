package satisfactionclient.reclamation_service.reclamation_service.Controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import satisfactionclient.reclamation_service.reclamation_service.Clients.RabbitUserClient;
import satisfactionclient.reclamation_service.reclamation_service.Clients.UserServiceClient;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.ReclamationRequestDto;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.UserDto;
import satisfactionclient.reclamation_service.reclamation_service.Entity.Reclamation;
import satisfactionclient.reclamation_service.reclamation_service.Entity.StatutReclamation;
import satisfactionclient.reclamation_service.reclamation_service.Service.ReclamationService;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
public class ReclamationController {
    @Autowired
    private RabbitUserClient rabbitTemplate;
    private final ReclamationService reclamationService;
   // private final UserServiceClient userServiceClient;

    public ReclamationController(ReclamationService reclamationService /*UserServiceClient userServiceClient*/) {
        this.reclamationService = reclamationService;
        //this.userServiceClient = userServiceClient;

    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitReclamation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReclamationRequestDto dto) {

        // 🔐 Récupérer infos depuis le token
        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");

        // 🔒 Vérification du rôle
        if (roles == null || !roles.contains("ROLE_Client")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : seuls les clients peuvent soumettre une réclamation.");
        }

        UserDto client = rabbitTemplate.getUserById(Long.valueOf(userId));

        Reclamation saved = reclamationService.creerReclamationAvecStatut(dto.getContenu(), client);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @PutMapping("/{id}/statut")
    public ResponseEntity<?> updateStatut(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam StatutReclamation statut) {

        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || !roles.contains("ROLE_AgentBancaire")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : seuls les agents peuvent modifier le statut.");
        }

        UserDto agent = rabbitTemplate.getUserById(Long.valueOf(userId));
        Reclamation updated = reclamationService.mettreAJourStatutParAgent(id, statut, agent);

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId) {

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || !roles.contains("ROLE_AgentBancaire")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé.");
        }

        List<Reclamation> list = reclamationService.getReclamationsByUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || !roles.contains("ROLE_AgentBancaire")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé.");
        }

        Reclamation r = reclamationService.getById(id);
        return ResponseEntity.ok(r);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @AuthenticationPrincipal Jwt jwt) {

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || !roles.contains("ROLE_AgentBancaire")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé.");
        }

        return ResponseEntity.ok(reclamationService.getAll());
    }
}