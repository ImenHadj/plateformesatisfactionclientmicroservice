package satisfactionclient.Enquete_service.Dto;

import satisfactionclient.Enquete_service.Entity.TypeReponse;

import java.util.List;

public class ReponseDTO {
    private Long questionId;
    private TypeReponse typeReponse;

    // Champs pour chaque type de réponse possible
    private String texteReponse;
    private List<String> choixReponses; // Pour choix unique ou multiple
    private Double valeurNumerique;

    // Getters et Setters

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public TypeReponse getTypeReponse() {
        return typeReponse;
    }

    public void setTypeReponse(TypeReponse typeReponse) {
        this.typeReponse = typeReponse;
    }

    public String getTexteReponse() {
        return texteReponse;
    }

    public void setTexteReponse(String texteReponse) {
        this.texteReponse = texteReponse;
    }

    public List<String> getChoixReponses() {
        return choixReponses;
    }

    public void setChoixReponses(List<String> choixReponses) {
        this.choixReponses = choixReponses;
    }

    public Double getValeurNumerique() {
        return valeurNumerique;
    }

    public void setValeurNumerique(Double valeurNumerique) {
        this.valeurNumerique = valeurNumerique;
    }
}