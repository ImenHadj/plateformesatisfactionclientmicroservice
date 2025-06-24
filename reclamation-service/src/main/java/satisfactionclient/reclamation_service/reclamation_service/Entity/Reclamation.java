package satisfactionclient.reclamation_service.reclamation_service.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReclamation statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeReclamation type;

    private LocalDateTime dateSoumission;

    private Long userId;

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatutHistorique> historique = new ArrayList<>(); // ✅ IMPORTANT



    public void soumettre(Long userId) {
        this.userId = userId;
        this.dateSoumission = LocalDateTime.now();
        this.statut = StatutReclamation.EN_ATTENTE;
    }

    public void mettreAJourStatut(StatutReclamation nouveauStatut) {
        this.statut = nouveauStatut;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public StatutReclamation getStatut() {
        return statut;
    }

    public void setStatut(StatutReclamation statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<StatutHistorique> getHistorique() {
        return historique;
    }

    public void setHistorique(List<StatutHistorique> historique) {
        this.historique = historique;
    }

    public TypeReclamation getType() {
        return type;
    }

    public void setType(TypeReclamation type) {
        this.type = type;
    }
}

