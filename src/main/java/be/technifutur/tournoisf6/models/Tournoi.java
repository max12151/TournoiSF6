package be.technifutur.tournoisf6.models;

public class Tournoi {
    private final int id;
    private String nom;
    private String format;
    private String date;
    private int nombreJoueurs;

    public Tournoi(int id, String nom, String format, String date, int nombreJoueurs) {
        this.id = id;
        this.nom = nom;
        this.format = format;
        this.date = date;
        this.nombreJoueurs = nombreJoueurs;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getNombreJoueurs() { return nombreJoueurs; }
    public void setNombreJoueurs(int nombreJoueurs) { this.nombreJoueurs = nombreJoueurs; }
}