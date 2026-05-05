package be.technifutur.tournoisf6.models;

import be.technifutur.tournoisf6.models.enums.BracketTypeEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "match_tournoi")
public class MatchTournoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tournoi_id", nullable = false)
    private Tournoi tournoi;

    @Enumerated(EnumType.STRING)
    @Column(name = "bracket_type",nullable = false, length = 20)
    private BracketTypeEnum bracketType;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur1_id")
    private Joueur joueur1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur2_id")
    private Joueur joueur2;

    @Column(name = "score_joueur1")
    private Integer scoreJoueur1;

    @Column(name = "score_joueur2")
    private Integer scoreJoueur2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gagnant_id")
    private Joueur gagnant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prochain_match_gagnant_id")
    private MatchTournoi prochainMatchGagnant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prochain_match_perdant_id")
    private MatchTournoi prochainMatchPerdant;

    @Column(nullable = false)
    private Boolean termine = false;

    public MatchTournoi() {
    }

    public MatchTournoi(Tournoi tournoi, BracketTypeEnum bracketType, Integer roundNumber, Joueur joueur1, Joueur joueur2) {
        this.tournoi = tournoi;
        this.bracketType = bracketType;
        this.roundNumber = roundNumber;
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
        this.termine = false;
    }

    @PrePersist
    public void prePersist() {
        if (termine == null) {
            termine = false;
        }
    }

    public boolean isReadyToPlay() {
        return joueur1 != null && joueur2 != null;
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

    public BracketTypeEnum getBracketType() {
        return bracketType;
    }

    public void setBracketType(BracketTypeEnum bracketType) {
        this.bracketType = bracketType;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Joueur getJoueur1() {
        return joueur1;
    }

    public void setJoueur1(Joueur joueur1) {
        this.joueur1 = joueur1;
    }

    public Joueur getJoueur2() {
        return joueur2;
    }

    public void setJoueur2(Joueur joueur2) {
        this.joueur2 = joueur2;
    }

    public Integer getScoreJoueur1() {
        return scoreJoueur1;
    }

    public void setScoreJoueur1(Integer scoreJoueur1) {
        this.scoreJoueur1 = scoreJoueur1;
    }

    public Integer getScoreJoueur2() {
        return scoreJoueur2;
    }

    public void setScoreJoueur2(Integer scoreJoueur2) {
        this.scoreJoueur2 = scoreJoueur2;
    }

    public Joueur getGagnant() {
        return gagnant;
    }

    public void setGagnant(Joueur gagnant) {
        this.gagnant = gagnant;
    }

    public MatchTournoi getProchainMatchGagnant() {
        return prochainMatchGagnant;
    }

    public void setProchainMatchGagnant(MatchTournoi prochainMatchGagnant) {
        this.prochainMatchGagnant = prochainMatchGagnant;
    }

    public MatchTournoi getProchainMatchPerdant() {
        return prochainMatchPerdant;
    }

    public void setProchainMatchPerdant(MatchTournoi prochainMatchPerdant) {
        this.prochainMatchPerdant = prochainMatchPerdant;
    }

    public Boolean getTermine() {
        return termine;
    }

    public void setTermine(Boolean termine) {
        this.termine = termine;
    }
}