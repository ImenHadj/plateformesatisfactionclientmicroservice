package satisfactionclient.reclamation_service.reclamation_service.Dtos;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private List<String> roles; // ✅ Ajouté

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; } // ✅ Getter
    public void setRoles(List<String> roles) { this.roles = roles; } // ✅ Setter
}
