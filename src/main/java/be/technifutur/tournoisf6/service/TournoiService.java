package be.technifutur.tournoisf6.service;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.models.Match;
import be.technifutur.tournoisf6.models.Tournoi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TournoiService {
    private static final TournoiService INSTANCE = new TournoiService();

    private final List<Joueur> joueurs = new ArrayList<>();
    private final List<Tournoi> tournois = new ArrayList<>();
    private final List<Match> matchs = new ArrayList<>();
    private final AtomicInteger joueurId = new AtomicInteger(1);

    private TournoiService() {
        joueurs.add(new Joueur(joueurId.getAndIncrement(), "Daigo", "Ken", "Japon"));
        joueurs.add(new Joueur(joueurId.getAndIncrement(), "AngryBird", "Ken", "EAU"));
        joueurs.add(new Joueur(joueurId.getAndIncrement(), "MenaRD", "Blanka", "République dominicaine"));
        joueurs.add(new Joueur(joueurId.getAndIncrement(), "Big Bird", "Marisa", "EAU"));

        tournois.add(new Tournoi(1, "Wallonia Clash SF6", "Double Elimination", "2026-06-01", 8));

        matchs.add(new Match(1, "Quart de finale", "Daigo", "Big Bird", "2 - 1", "Daigo"));
        matchs.add(new Match(2, "Quart de finale", "AngryBird", "MenaRD", "2 - 0", "AngryBird"));
    }

    public static TournoiService getInstance() {
        return INSTANCE;
    }

    public List<Joueur> getJoueurs() {
        return Collections.unmodifiableList(joueurs);
    }

    public List<Tournoi> getTournois() {
        return Collections.unmodifiableList(tournois);
    }

    public List<Match> getMatchs() {
        return Collections.unmodifiableList(matchs);
    }

    public void ajouterJoueur(String pseudo, String personnagePrincipal, String pays) {
        joueurs.add(new Joueur(joueurId.getAndIncrement(), pseudo, personnagePrincipal, pays));
    }
}