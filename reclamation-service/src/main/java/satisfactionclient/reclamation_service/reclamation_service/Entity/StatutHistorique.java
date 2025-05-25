package satisfactionclient.reclamation_service.reclamation_service.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class StatutHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    private StatutReclamation ancienStatut;

    @Enumerated(EnumType.STRING)
    private StatutReclamation nouveauStatut;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "reclamation_id")
    private Reclamation reclamation;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public StatutReclamation getAncienStatut() {
        return ancienStatut;
    }

    public void setAncienStatut(StatutReclamation ancienStatut) {
        this.ancienStatut = ancienStatut;
    }

    public StatutReclamation getNouveauStatut() {
        return nouveauStatut;
    }

    public void setNouveauStatut(StatutReclamation nouveauStatut) {
        this.nouveauStatut = nouveauStatut;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }
}