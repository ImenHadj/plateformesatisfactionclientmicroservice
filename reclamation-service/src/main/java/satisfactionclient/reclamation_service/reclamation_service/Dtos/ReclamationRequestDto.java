package satisfactionclient.reclamation_service.reclamation_service.Dtos;

import lombok.Data;
import satisfactionclient.reclamation_service.reclamation_service.Entity.StatutReclamation;
import satisfactionclient.reclamation_service.reclamation_service.Entity.TypeReclamation;

import java.time.LocalDateTime;

@Data

public class ReclamationRequestDto {
    private String contenu;

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    public TypeReclamation getType() {
        return type;
    }

    public void setType(TypeReclamation type) {
        this.type = type;
    }

    private StatutReclamation statut;
    private TypeReclamation type;
    private LocalDateTime dateSoumission;
}

