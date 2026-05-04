package be.technifutur.tournoisf6.models;

public class Match {
    private final int id;
    private String round;
    private String joueur1;
    private String joueur2;
    private String score;
    private String vainqueur;

    public Match(int id, String round, String joueur1, String joueur2, String score, String vainqueur) {
        this.id = id;
        this.round = round;
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
        this.score = score;
        this.vainqueur = vainqueur;
    }

    public int getId() { return id; }
    public String getRound() { return round; }
    public String getJoueur1() { return joueur1; }
    public String getJoueur2() { return joueur2; }
    public String getScore() { return score; }
    public String getVainqueur() { return vainqueur; }
}