package satisfactionclient.Enquete_service.Dto;

import satisfactionclient.Enquete_service.Entity.StatutEnquete;

import java.time.LocalDateTime;
import java.util.List;

public class EnqueteResponseDTO {
    private Long id;


    private String titre;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private LocalDateTime datePublication;
    private StatutEnquete statut;
    private List<QuestionDTO> questions;

    // Getters et setters

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

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public StatutEnquete getStatut() {
        return statut;
    }

    public void setStatut(StatutEnquete statut) {
        this.statut = statut;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
