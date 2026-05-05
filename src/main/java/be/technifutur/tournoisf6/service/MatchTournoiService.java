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
import java.util.Collections;
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
                    ORDER BY m.roundNumber ASC, m.id ASC
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

            List<Joueur> joueurs = new ArrayList<>();
            for (InscriptionTournoi inscription : inscriptions) {
                if (!Boolean.TRUE.equals(inscription.getElimine())) {
                    joueurs.add(inscription.getJoueur());
                }
            }

            Collections.shuffle(joueurs);

            List<MatchTournoi> matchsPremierRound = new ArrayList<>();

            for (int i = 0; i < joueurs.size(); i += 2) {
                Joueur joueur1 = joueurs.get(i);
                Joueur joueur2 = (i + 1 < joueurs.size()) ? joueurs.get(i + 1) : null;

                MatchTournoi match = new MatchTournoi(tournoi, BracketTypeEnum.WINNERS, 1, joueur1, joueur2);

                if (joueur2 == null) {
                    match.setGagnant(joueur1);
                    match.setScoreJoueur1(1);
                    match.setScoreJoueur2(0);
                    match.setTermine(true);
                }

                em.persist(match);
                matchsPremierRound.add(match);
            }

            for (int i = 0; i < matchsPremierRound.size(); i += 2) {
                MatchTournoi futur = new MatchTournoi(tournoi, BracketTypeEnum.WINNERS, 2, null, null);
                em.persist(futur);

                matchsPremierRound.get(i).setProchainMatchGagnant(futur);
                if (i + 1 < matchsPremierRound.size()) {
                    matchsPremierRound.get(i + 1).setProchainMatchGagnant(futur);
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

        if (matchLosers == null) {
            matchLosers = new MatchTournoi(
                    match.getTournoi(),
                    BracketTypeEnum.LOSERS,
                    match.getRoundNumber(),
                    null,
                    null
            );
            em.persist(matchLosers);
            match.setProchainMatchPerdant(matchLosers);
        }

        if (matchLosers.getJoueur1() == null) {
            matchLosers.setJoueur1(perdant);
        } else if (matchLosers.getJoueur2() == null) {
            matchLosers.setJoueur2(perdant);
        } else {
            MatchTournoi autreMatchLosers = new MatchTournoi(
                    match.getTournoi(),
                    BracketTypeEnum.LOSERS,
                    match.getRoundNumber(),
                    perdant,
                    null
            );
            em.persist(autreMatchLosers);
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