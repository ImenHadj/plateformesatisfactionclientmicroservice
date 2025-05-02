package satisfactionclient.user_service.Controller;


import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;  // ✅ Google GsonFactory attendu
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import satisfactionclient.user_service.Entity.User;
import satisfactionclient.user_service.Repository.RoleRepository;
import satisfactionclient.user_service.Repository.UserRepository;
import satisfactionclient.user_service.Service.Authservice;
import satisfactionclient.user_service.Service.Emailservice;
import satisfactionclient.user_service.payload.request.LoginRequest;
import satisfactionclient.user_service.payload.request.SignupRequest;
import satisfactionclient.user_service.security.jwt.JwtUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
public class AuthController {

    private static final String CLIENT_ID = "678352302593-efqco2fe19sb705grc97nni2q8k8q49p.apps.googleusercontent.com";

    private final Authservice authService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private Emailservice emailService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    public AuthController(Authservice authService) {
        this.authService = authService;
    }






    /*@PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        authService.authenticateUser(loginRequest, response);
        return ResponseEntity.ok("Connexion réussie !");
    }*/

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            String jwt = authService.authenticateUser(loginRequest, response);
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("jwt", jwt);
            responseMap.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "roles", user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList())
            ));

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            e.printStackTrace();  // Affiche l'erreur dans les logs du serveur
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Erreur de connexion", "details", e.getMessage()));
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        String response = authService.registerUser(signUpRequest);
        if (response.startsWith("Error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        List<String> roles = roleRepository.findAll()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        // Générer un token unique (UUID)
        String token = UUID.randomUUID().toString();

        // Créer un lien de réinitialisation (remplace localhost par ton vrai domaine)
        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        // Envoyer l'email
        emailService.sendResetPasswordEmail(email, resetLink);

        return "Un email de réinitialisation a été envoyé à " + email;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            // Appeler le service pour réinitialiser le mot de passe
            String response = authService.resetPassword(email, newPassword);
            return ResponseEntity.ok(response);  // Réponse OK avec le message de succès
        } catch (Exception e) {
            // Si une erreur survient (utilisateur non trouvé)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }
   /* @PostMapping("/google")
    public ResponseEntity<String> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        try {
            String idTokenString = request.get("idToken");

            if (idTokenString == null || idTokenString.isEmpty()) {
                return ResponseEntity.status(400).body("ID token is missing or empty");
            }

            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                return ResponseEntity.ok("Token is valid");
            } else {
                return ResponseEntity.status(400).body("Invalid ID token");
            }
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(500).body("Error during token verification: " + e.getMessage());
        }
    }*/
   @PostMapping("/google")
   public ResponseEntity<String> authenticateWithGoogle(@RequestBody Map<String, String> request, HttpServletResponse response) {
       try {
           String idTokenString = request.get("idToken");

           if (idTokenString == null || idTokenString.isEmpty()) {
               return ResponseEntity.status(400).body("ID token is missing or empty");
           }

           HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
           JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
           GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                   .setAudience(Collections.singletonList(CLIENT_ID))
                   .build();

           GoogleIdToken idToken = verifier.verify(idTokenString);

           if (idToken != null) {
               // Validation du token réussie. Créer un JWT et l'ajouter dans un cookie.
               String username = idToken.getPayload().getEmail(); // Utilisez l'email comme identifiant pour l'utilisateur.
               String jwt = jwtUtil.generateToken(username); // Générer un JWT pour l'utilisateur (assurez-vous que votre méthode de génération de JWT est correcte).

               // Créer un cookie JWT
               Cookie cookie = new Cookie("jwt", jwt);
               cookie.setHttpOnly(true);  // Empêche l'accès au cookie via JavaScript
               cookie.setSecure(true);    // Doit être activé en production (HTTPS)
               cookie.setPath("/");       // Accessible sur toute l'application
               cookie.setMaxAge(24 * 60 * 60);  // Expire après 24h
               response.addCookie(cookie); // Ajouter le cookie à la réponse.

               return ResponseEntity.ok("Token is valid");
           } else {
               return ResponseEntity.status(400).body("Invalid ID token");
           }
       } catch (GeneralSecurityException | IOException e) {
           return ResponseEntity.status(500).body("Error during token verification: " + e.getMessage());
       }
   }

}
