package be.technifutur.tournoisf6.models;

import be.technifutur.tournoisf6.models.enums.EtatTournoiEnum;
import be.technifutur.tournoisf6.models.enums.FormatTournoiEnum;
import be.technifutur.tournoisf6.models.enums.RankEnum;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournoi")
public class Tournoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private Integer nombreJoueursMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FormatTournoiEnum format = FormatTournoiEnum.DOUBLE_ELIMINATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EtatTournoiEnum etat = EtatTournoiEnum.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RankEnum rankMaxAutorise;

    @OneToMany(mappedBy = "tournoi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InscriptionTournoi> inscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "tournoi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchTournoi> matchs = new ArrayList<>();

    public Tournoi() {
    }

    public Tournoi(String nom, LocalDate dateDebut, LocalDate dateFin, Integer nombreJoueursMax,
                   FormatTournoiEnum format, EtatTournoiEnum etat, RankEnum rankMaxAutorise) {
        this.nom = nom;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombreJoueursMax = nombreJoueursMax;
        this.format = (format == null) ? FormatTournoiEnum.DOUBLE_ELIMINATION : format;
        this.etat = (etat == null) ? EtatTournoiEnum.EN_ATTENTE : etat;
        this.rankMaxAutorise = rankMaxAutorise;
    }

    @PrePersist
    public void prePersist() {
        if (format == null) {
            format = FormatTournoiEnum.DOUBLE_ELIMINATION;
        }
        if (etat == null) {
            etat = EtatTournoiEnum.EN_ATTENTE;
        }
    }

    public boolean isDateCoherente() {
        return dateDebut != null && dateFin != null && !dateFin.isBefore(dateDebut);
    }

    public boolean isComplet() {
        return inscriptions != null && nombreJoueursMax != null && inscriptions.size() >= nombreJoueursMax;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Integer getNombreJoueursMax() {
        return nombreJoueursMax;
    }

    public void setNombreJoueursMax(Integer nombreJoueursMax) {
        this.nombreJoueursMax = nombreJoueursMax;
    }

    public FormatTournoiEnum getFormat() {
        return format;
    }

    public void setFormat(FormatTournoiEnum format) {
        this.format = format;
    }

    public EtatTournoiEnum getEtat() {
        return etat;
    }

    public void setEtat(EtatTournoiEnum etat) {
        this.etat = etat;
    }

    public RankEnum getRankMaxAutorise() {
        return rankMaxAutorise;
    }

    public void setRankMaxAutorise(RankEnum rankMaxAutorise) {
        this.rankMaxAutorise = rankMaxAutorise;
    }

    public List<InscriptionTournoi> getInscriptions() {
        return inscriptions;
    }

    public void setInscriptions(List<InscriptionTournoi> inscriptions) {
        this.inscriptions = inscriptions;
    }

    public List<MatchTournoi> getMatchs() {
        return matchs;
    }

    public void setMatchs(List<MatchTournoi> matchs) {
        this.matchs = matchs;
    }
}