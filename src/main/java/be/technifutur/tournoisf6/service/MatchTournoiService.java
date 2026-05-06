package be.technifutur.tournoisf6.service;

import be.technifutur.tournoisf6.models.InscriptionTournoi;
import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.models.MatchTournoi;
import be.technifutur.tournoisf6.models.Tournoi;
import be.technifutur.tournoisf6.models.enums.BracketTypeEnum;
import be.technifutur.tournoisf6.models.enums.EtatTournoiEnum;
import be.technifutur.tournoisf6.utils.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchTournoiService {

    public List<MatchTournoi> findByTournoi(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("""
                    SELECT m FROM MatchTournoi m
                    LEFT JOIN FETCH m.joueur1
                    LEFT JOIN FETCH m.joueur2
                    LEFT JOIN FETCH m.gagnant
                    WHERE m.tournoi.id = :tournoiId
                    ORDER BY m.bracketType ASC, m.roundNumber ASC, m.id ASC
                    """, MatchTournoi.class)
                    .setParameter("tournoiId", tournoiId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void lancerTournoi(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Tournoi tournoi = em.find(Tournoi.class, tournoiId);
            if (tournoi == null) {
                throw new IllegalArgumentException("Tournoi introuvable.");
            }

            if (tournoi.getEtat() != EtatTournoiEnum.EN_ATTENTE) {
                throw new IllegalStateException("Le tournoi n'est pas dans un état lançable.");
            }

            List<InscriptionTournoi> inscriptions = em.createQuery("""
                    SELECT i FROM InscriptionTournoi i
                    JOIN FETCH i.joueur
                    WHERE i.tournoi.id = :tournoiId
                    ORDER BY i.id ASC
                    """, InscriptionTournoi.class)
                    .setParameter("tournoiId", tournoiId)
                    .getResultList();

            if (inscriptions.size() < 2) {
                throw new IllegalStateException("Il faut au moins 2 joueurs pour lancer le tournoi.");
            }

            // Récupération des joueurs non éliminés
            List<Joueur> joueurs = new ArrayList<>();
            for (InscriptionTournoi inscription : inscriptions) {
                if (!Boolean.TRUE.equals(inscription.getElimine())) {
                    joueurs.add(inscription.getJoueur());
                }
            }

            if (joueurs.size() < 2) {
                throw new IllegalStateException("Il faut au moins 2 joueurs non éliminés pour lancer le tournoi.");
            }

            // Seeding par rank : meilleurs en premier (MASTER, DIAMOND_V, ...)
            joueurs.sort(
                    Comparator
                            .comparing((Joueur j) -> j.getRank().ordinal())
                            .reversed()
                            .thenComparing(Joueur::getId)
            );

            int n = joueurs.size();

            // Prochaine puissance de 2 >= n
            int bracketSize = 1;
            while (bracketSize < n) {
                bracketSize <<= 1;
            }

            int nbByes = bracketSize - n;

            // Nombre de rounds du winner bracket = log2(bracketSize)
            int nbRoundsWinners = 0;
            int tmp = bracketSize;
            while (tmp > 1) {
                tmp >>= 1;
                nbRoundsWinners++;
            }

            // Création de tous les rounds du winner bracket
            List<List<MatchTournoi>> roundsWinners = new ArrayList<>();

            for (int r = 1; r <= nbRoundsWinners; r++) {
                int nbMatchsThisRound = bracketSize / (1 << r);
                List<MatchTournoi> round = new ArrayList<>();

                for (int i = 0; i < nbMatchsThisRound; i++) {
                    MatchTournoi m = new MatchTournoi(
                            tournoi,
                            BracketTypeEnum.WINNERS,
                            r,
                            null,
                            null
                    );
                    em.persist(m);
                    round.add(m);
                }

                roundsWinners.add(round);
            }

            // Lier chaque match winners à son prochain match gagnant
            for (int r = 0; r < nbRoundsWinners - 1; r++) {
                List<MatchTournoi> currentRound = roundsWinners.get(r);
                List<MatchTournoi> nextRound = roundsWinners.get(r + 1);

                for (int i = 0; i < currentRound.size(); i++) {
                    MatchTournoi current = currentRound.get(i);
                    MatchTournoi next = nextRound.get(i / 2);
                    current.setProchainMatchGagnant(next);
                }
            }

            // Génération du loser bracket : un match losers pré‑lié à chaque match winners
            // (même roundNumber, bracketType = LOSERS)
            for (int r = 0; r < nbRoundsWinners; r++) {
                List<MatchTournoi> winnersRound = roundsWinners.get(r);
                for (MatchTournoi w : winnersRound) {
                    MatchTournoi l = new MatchTournoi(
                            tournoi,
                            BracketTypeEnum.LOSERS,
                            r + 1, // round losers "parallèle" au round winners
                            null,
                            null
                    );
                    em.persist(l);
                    w.setProchainMatchPerdant(l);
                }
            }

            // Remplir le 1er round winners avec les joueurs + byes
            List<MatchTournoi> firstRound = roundsWinners.get(0);
            int indexMatch = 0;
            int left = 0;        // meilleurs joueurs
            int right = n - 1;   // moins bons joueurs
            int remainingByes = nbByes;

            // 1) Byes pour les meilleurs joueurs
            while (remainingByes > 0 && indexMatch < firstRound.size() && left <= right) {
                Joueur top = joueurs.get(left++);
                MatchTournoi m = firstRound.get(indexMatch++);

                m.setJoueur1(top);
                // adversaire null => bye
                m.setGagnant(top);
                m.setScoreJoueur1(1);
                m.setScoreJoueur2(0);
                m.setTermine(true);

                // propager immédiatement dans le prochain match winners
                propagerGagnant(m, top);

                // ATTENTION : ce match n'aura jamais de perdant réel,
                // mais on garde quand même le match losers lié (qu'on ne remplira jamais).
                remainingByes--;
            }

            // 2) Appariement des joueurs restants : meilleur vs moins bon
            while (left <= right && indexMatch < firstRound.size()) {
                Joueur p1 = joueurs.get(left++);
                Joueur p2 = (left <= right) ? joueurs.get(right--) : null;

                MatchTournoi m = firstRound.get(indexMatch++);

                m.setJoueur1(p1);
                if (p2 != null) {
                    m.setJoueur2(p2);
                } else {
                    // Dernier joueur sans adversaire => bye
                    m.setGagnant(p1);
                    m.setScoreJoueur1(1);
                    m.setScoreJoueur2(0);
                    m.setTermine(true);
                    propagerGagnant(m, p1);
                    // idem : pas de perdant réel, donc le match losers lié restera vide
                }
            }

            tournoi.setEtat(EtatTournoiEnum.EN_COURS);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void encoderResultat(Long matchId, int scoreJoueur1, int scoreJoueur2) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            MatchTournoi match = em.find(MatchTournoi.class, matchId);
            if (match == null) {
                throw new IllegalArgumentException("Match introuvable.");
            }

            if (Boolean.TRUE.equals(match.getTermine())) {
                throw new IllegalStateException("Ce match est déjà terminé.");
            }

            if (match.getJoueur1() == null || match.getJoueur2() == null) {
                throw new IllegalStateException("Les deux joueurs doivent être définis.");
            }

            if (scoreJoueur1 == scoreJoueur2) {
                throw new IllegalArgumentException("Un match ne peut pas se terminer sur une égalité.");
            }

            match.setScoreJoueur1(scoreJoueur1);
            match.setScoreJoueur2(scoreJoueur2);

            Joueur gagnant = scoreJoueur1 > scoreJoueur2 ? match.getJoueur1() : match.getJoueur2();
            Joueur perdant = scoreJoueur1 > scoreJoueur2 ? match.getJoueur2() : match.getJoueur1();

            match.setGagnant(gagnant);
            match.setTermine(true);

            // propagation dans le bracket approprié
            propagerGagnant(match, gagnant);

            if (match.getBracketType() == BracketTypeEnum.WINNERS) {
                gererPerdantWinners(em, match, perdant);
            } else if (match.getBracketType() == BracketTypeEnum.LOSERS) {
                eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
            }

            verifierFinTournoi(em, match.getTournoi().getId());

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void propagerGagnant(MatchTournoi match, Joueur gagnant) {
        MatchTournoi prochain = match.getProchainMatchGagnant();
        if (prochain == null) {
            return;
        }

        if (prochain.getJoueur1() == null) {
            prochain.setJoueur1(gagnant);
        } else if (prochain.getJoueur2() == null) {
            prochain.setJoueur2(gagnant);
        }
    }

    private void gererPerdantWinners(EntityManager em, MatchTournoi match, Joueur perdant) {
        MatchTournoi matchLosers = match.getProchainMatchPerdant();

        // Si aucun match losers n'est défini (cas théorique), on élimine directement
        if (matchLosers == null) {
            eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
            return;
        }

        if (matchLosers.getJoueur1() == null) {
            matchLosers.setJoueur1(perdant);
        } else if (matchLosers.getJoueur2() == null) {
            matchLosers.setJoueur2(perdant);
        } else {
            throw new IllegalStateException("Match losers cible déjà complet");
        }
    }

    private void eliminerJoueur(EntityManager em, Long tournoiId, Long joueurId) {
        InscriptionTournoi inscription = em.createQuery("""
                SELECT i FROM InscriptionTournoi i
                WHERE i.tournoi.id = :tournoiId AND i.joueur.id = :joueurId
                """, InscriptionTournoi.class)
                .setParameter("tournoiId", tournoiId)
                .setParameter("joueurId", joueurId)
                .getSingleResult();

        inscription.setElimine(true);
    }

    private void verifierFinTournoi(EntityManager em, Long tournoiId) {
        Tournoi tournoi = em.find(Tournoi.class, tournoiId);

        Long joueursEncoreEnVie = em.createQuery("""
                SELECT COUNT(i) FROM InscriptionTournoi i
                WHERE i.tournoi.id = :tournoiId AND i.elimine = false
                """, Long.class)
                .setParameter("tournoiId", tournoiId)
                .getSingleResult();

        if (joueursEncoreEnVie <= 1) {
            tournoi.setEtat(EtatTournoiEnum.TERMINE);
        }
    }
}