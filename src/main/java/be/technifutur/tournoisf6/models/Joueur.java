package be.technifutur.tournoisf6.models;

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

    @Column(nullable = false)
    private Integer elo;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String personnagePrincipal;

    public Joueur() {
    }

    public Joueur(String pseudo, String email, String motDePasse, LocalDate dateNaissance,
                  String genre, Integer elo, String role, String personnagePrincipal) {
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.elo = elo;
        this.role = role;
        this.personnagePrincipal = personnagePrincipal;
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

    public Integer getElo() {
        return elo;
    }

    public void setElo(Integer elo) {
        this.elo = elo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPersonnagePrincipal() {
        return personnagePrincipal;
    }

    public void setPersonnagePrincipal(String personnagePrincipal) {
        this.personnagePrincipal = personnagePrincipal;
    }
}