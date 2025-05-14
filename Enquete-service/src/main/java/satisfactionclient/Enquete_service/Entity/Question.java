package satisfactionclient.Enquete_service.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String texte;
    private Integer ordre; // Pour trier les questions
  //  private boolean estObligatoire = false;

    @Enumerated(EnumType.STRING)
    private TypeQuestion type; // OUVERT, CHOIX_SIMPLE, CHOIX_MULTIPLE

    // Utilisez @ElementCollection pour simplifier (si pas besoin de métadonnées supplémentaires)
    @ElementCollection
    @CollectionTable(name = "question_option", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_texte")
    private List<String> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquete_id")
    private Enquete enquete;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Reponse> reponses = new ArrayList<>();

    // Getters/Setters (garder ceux existants + nouveaux)

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setType(TypeQuestion type) {
        this.type = type;
    }

    public Enquete getEnquete() {
        return enquete;
    }

    public void setEnquete(Enquete enquete) {
        this.enquete = enquete;
    }

    public String getTexte() {
        return texte;
    }
    public TypeQuestion getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public List<String> getOptions() {
        return options;
    }

}

