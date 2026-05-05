package be.technifutur.tournoisf6.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inscription_tournoi",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tournoi_id", "joueur_id"})
        }
)
public class InscriptionTournoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tournoi_id", nullable = false)
    private Tournoi tournoi;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @Column(nullable = false)
    private LocalDateTime dateInscription;

    @Column(nullable = false)
    private Boolean elimine = false;

    public InscriptionTournoi() {
    }

    public InscriptionTournoi(Tournoi tournoi, Joueur joueur) {
        this.tournoi = tournoi;
        this.joueur = joueur;
        this.dateInscription = LocalDateTime.now();
        this.elimine = false;
    }

    @PrePersist
    public void prePersist() {
        if (dateInscription == null) {
            dateInscription = LocalDateTime.now();
        }
        if (elimine == null) {
            elimine = false;
        }
    }

    public Long getId() {
        return id;
    }

    public Tournoi getTournoi() {
        return tournoi;
    }

    public void setTournoi(Tournoi tournoi) {
        this.tournoi = tournoi;
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public void setJoueur(Joueur joueur) {
        this.joueur = joueur;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public Boolean getElimine() {
        return elimine;
    }

    public void setElimine(Boolean elimine) {
        this.elimine = elimine;
    }
}