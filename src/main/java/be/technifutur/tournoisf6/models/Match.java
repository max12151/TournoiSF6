package be.technifutur.tournoisf6.models;

import jakarta.persistence.*;

@Entity
@Table(name = "match_sf6")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String round;

    @Column(nullable = false)
    private String joueur1;

    @Column(nullable = false)
    private String joueur2;

    @Column(nullable = false)
    private String score;

    @Column(nullable = false)
    private String vainqueur;

    public Match() {
    }

    public Match(String round, String joueur1, String joueur2, String score, String vainqueur) {
        this.round = round;
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
        this.score = score;
        this.vainqueur = vainqueur;
    }

    public Long getId() {
        return id;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getJoueur1() {
        return joueur1;
    }

    public void setJoueur1(String joueur1) {
        this.joueur1 = joueur1;
    }

    public String getJoueur2() {
        return joueur2;
    }

    public void setJoueur2(String joueur2) {
        this.joueur2 = joueur2;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getVainqueur() {
        return vainqueur;
    }

    public void setVainqueur(String vainqueur) {
        this.vainqueur = vainqueur;
    }
}