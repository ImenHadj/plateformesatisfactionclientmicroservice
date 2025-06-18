package satisfactionclient.user_service.Dtos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class UserDto implements Serializable {  // ✅ 1. Implements Serializable
    private static final long serialVersionUID = 1L; // ✅ 2. Bonnes pratiques

    private Long id;
    private String username;
    private String email;
    private String role; // ✅ On garde comme String
    private Boolean active; // optionnel si tu veux gérer l’activation
    private String fcmToken; // ✅ Ajout du token pour Web Push

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getFcmToken() { return fcmToken; } // ✅ Getter
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; } // ✅ Setter
}

