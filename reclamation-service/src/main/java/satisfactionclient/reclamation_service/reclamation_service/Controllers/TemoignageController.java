package satisfactionclient.reclamation_service.reclamation_service.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import satisfactionclient.reclamation_service.reclamation_service.Clients.RabbitUserClient;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.TemoignageRequestDto;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.UserDto;
import satisfactionclient.reclamation_service.reclamation_service.Entity.Temoignage;
import satisfactionclient.reclamation_service.reclamation_service.Service.TemoignageService;

import java.util.List;

@RestController
@RequestMapping("/api/temoignages")
public class TemoignageController {

    @Autowired
    private TemoignageService temoignageService;

    @Autowired
    private RabbitUserClient rabbitUserClient;

    @PostMapping("/submit")
    public ResponseEntity<?> ajouterTemoignage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody TemoignageRequestDto dto) {

        String userId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || !roles.contains("ROLE_Client")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Accès refusé : seuls les clients peuvent soumettre un témoignage.");
        }

        UserDto client = rabbitUserClient.getUserById(Long.valueOf(userId));

        Temoignage saved = temoignageService.ajouterTemoignage(dto.getCommentaire(), dto.getNote(), client);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> getAllTemoignages() {
        return ResponseEntity.ok(temoignageService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTemoignagesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(temoignageService.getByUserId(userId));
    }
}

