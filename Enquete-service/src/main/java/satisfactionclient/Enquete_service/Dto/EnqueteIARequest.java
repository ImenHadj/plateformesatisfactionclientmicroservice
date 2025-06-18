package satisfactionclient.Enquete_service.Dto;

import java.io.Serializable;

public class EnqueteIARequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titre;
    private String description;

    // Constructeur vide (requis pour Jackson)
    public EnqueteIARequest() {}

    // Getters et Setters
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
