package satisfactionclient.Enquete_service.Dto;

import satisfactionclient.Enquete_service.Entity.TypeQuestion;

import java.util.List;

public class QuestionDTO {
    private Long id;  // Ajout de ce champ
    private String texte;
    private TypeQuestion type;
    private List<String> options; // Si nécessaire

    // Constructeurs
    public QuestionDTO() {}

    // Ancien constructeur (à garder pour compatibilité)
    public QuestionDTO(String texte, TypeQuestion type) {
        this.texte = texte;
        this.type = type;
    }


    // Getters and Setters
    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public TypeQuestion getType() {
        return type;
    }

    public void setType(TypeQuestion type) {
        this.type = type;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
