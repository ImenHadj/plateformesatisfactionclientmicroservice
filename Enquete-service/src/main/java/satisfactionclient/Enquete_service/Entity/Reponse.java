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
public class Reponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeReponse typeReponse; // Enum avec TEXTE, CHOIX, NUMERIQUE, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_id")
    private Long userId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquete_id", nullable = false)
    private Enquete enquete;



    @Column(name = "valeur_texte", columnDefinition = "TEXT")
    private String valeurTexte;

    @Column(name = "valeurs_choix", columnDefinition = "TEXT")
    private String valeursChoix;


    @Column(name = "valeur_numerique")
    private Double valeurNumerique;

    // Getter pour la liste


    public void setEnquete(Enquete enquete) {
        this.enquete = enquete;
    }

    //public void setUser(User user) {this.user = user;}

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public TypeReponse getTypeReponse() {
        return typeReponse;
    }

    public void setTypeReponse(TypeReponse typeReponse) {
        this.typeReponse = typeReponse;
    }

    public String getValeurTexte() {
        return valeurTexte;
    }

    public void setValeurTexte(String valeurTexte) {
        this.valeurTexte = valeurTexte;
    }



    public Double getValeurNumerique() {
        return valeurNumerique;
    }

    public void setValeurNumerique(Double valeurNumerique) {
        this.valeurNumerique = valeurNumerique;
    }
    public String getValeursChoix() {
        return valeursChoix;
    }

    public void setValeursChoix(String valeursChoix) {
        this.valeursChoix = valeursChoix;
    }
    // Méthode utilitaire pour enregistrer une liste sous forme de chaîne
    public void setValeursChoixFromList(List<String> choix) {
        if (choix != null && !choix.isEmpty()) {
            this.valeursChoix = String.join(",", choix);
        } else {
            this.valeursChoix = null;
        }
    }

    // Méthode utilitaire pour récupérer la liste depuis la chaîne
    public List<String> getValeursChoixAsList() {
        if (valeursChoix != null && !valeursChoix.trim().isEmpty()) {
            return List.of(valeursChoix.split("\\s*,\\s*")); // supprime les espaces autour des virgules
        }
        return new ArrayList<>();
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }
}