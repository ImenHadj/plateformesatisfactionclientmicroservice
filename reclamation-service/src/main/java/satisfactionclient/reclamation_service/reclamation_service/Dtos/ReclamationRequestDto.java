package satisfactionclient.reclamation_service.reclamation_service.Dtos;

import lombok.Data;

@Data

public class ReclamationRequestDto {
    private String contenu;

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }


}

