package satisfactionclient.Enquete_service.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Enquete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;

    private LocalDateTime datePublication; // Date et heure de publication

    @Enumerated(EnumType.STRING)
    private StatutEnquete statut; // BROUILLON, PUBLIÉE, FERMÉE

    @Column(name = "admin_id")
    private Long adminId;

    @OneToMany(mappedBy = "enquete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();


    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public void setStatut(StatutEnquete statut) {
        this.statut = statut;
    }

    //public void setAdmin(User admin) {this.admin = admin;}

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    //public User getAdmin() {return admin;}

    public List<Question> getQuestions() {
        return questions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    } public StatutEnquete getStatut() {
        return statut;
    }
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }


    }
