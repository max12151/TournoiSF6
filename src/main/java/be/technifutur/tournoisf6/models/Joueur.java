package be.technifutur.tournoisf6.models;

import be.technifutur.tournoisf6.models.enums.RankEnum;
import be.technifutur.tournoisf6.models.enums.RoleEnum;
import jakarta.persistence.*;

        import java.time.LocalDate;

@Entity
@Table(name = "joueur")
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pseudo;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private LocalDate dateNaissance;

    @Column(nullable = false)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RankEnum rank = RankEnum.ROOKIE_I;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleEnum role = RoleEnum.JOUEUR;

    @Column(nullable = false)
    private String personnagePrincipal;

    @Column(nullable = false)
    private String pays;

    public Joueur() {
    }

    public Joueur(String pseudo, String email, String motDePasse, LocalDate dateNaissance,
                  String genre, RankEnum rank, RoleEnum role, String personnagePrincipal, String pays) {
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.rank = (rank == null) ? RankEnum.ROOKIE_I : rank;
        this.role = (role == null) ? RoleEnum.JOUEUR : role;
        this.personnagePrincipal = personnagePrincipal;
        this.pays = pays;
    }

    @PrePersist
    public void prePersist() {
        if (rank == null) {
            rank = RankEnum.ROOKIE_I;
        }
        if (role == null) {
            role = RoleEnum.JOUEUR;
        }
    }

    public Long getId() {
        return id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public RankEnum getRank() {
        return rank;
    }

    public void setRank(RankEnum rank) {
        this.rank = rank;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public String getPersonnagePrincipal() {
        return personnagePrincipal;
    }

    public void setPersonnagePrincipal(String personnagePrincipal) {
        this.personnagePrincipal = personnagePrincipal;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }
}