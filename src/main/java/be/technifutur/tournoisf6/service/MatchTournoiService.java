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
import java.util.Comparator;
import java.util.List;

public class MatchTournoiService {

    // =========================================================================
    // LECTURE
    // =========================================================================

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

    // =========================================================================
    // LANCER TOURNOI
    // =========================================================================

    public void lancerTournoi(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Tournoi tournoi = em.find(Tournoi.class, tournoiId);
            if (tournoi == null) throw new IllegalArgumentException("Tournoi introuvable.");
            if (tournoi.getEtat() != EtatTournoiEnum.EN_ATTENTE)
                throw new IllegalStateException("Le tournoi n'est pas dans un état lançable.");

            List<InscriptionTournoi> inscriptions = em.createQuery("""
                    SELECT i FROM InscriptionTournoi i
                    JOIN FETCH i.joueur
                    WHERE i.tournoi.id = :tournoiId
                    ORDER BY i.id ASC
                    """, InscriptionTournoi.class)
                    .setParameter("tournoiId", tournoiId)
                    .getResultList();

            List<Joueur> joueurs = new ArrayList<>();
            for (InscriptionTournoi ins : inscriptions) {
                if (!Boolean.TRUE.equals(ins.getElimine())) {
                    joueurs.add(ins.getJoueur());
                }
            }

            if (joueurs.size() < 2)
                throw new IllegalStateException("Il faut au moins 2 joueurs pour lancer le tournoi.");

            if (joueurs.size() == 2) {
                lancerTournoiDeuxJoueurs(em, tournoi, joueurs);
            } else {
                lancerTournoiDoubleElimination(em, tournoi, joueurs);
            }

            tournoi.setEtat(EtatTournoiEnum.EN_COURS);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // =========================================================================
    // CAS SPÉCIAL : 2 JOUEURS
    // =========================================================================

    private void lancerTournoiDeuxJoueurs(EntityManager em, Tournoi tournoi, List<Joueur> joueurs) {
        MatchTournoi gf = new MatchTournoi(
                tournoi, BracketTypeEnum.GRAND_FINAL, 1,
                joueurs.get(0), joueurs.get(1)
        );
        em.persist(gf);
    }

    // =========================================================================
    // CAS GÉNÉRAL : DOUBLE ÉLIMINATION
    // =========================================================================

    private void lancerTournoiDoubleElimination(EntityManager em, Tournoi tournoi, List<Joueur> joueurs) {
        int n = joueurs.size();
        joueurs = seederJoueurs(joueurs);

        int bracketSize     = prochaineDeuxPuissanceDe(n);
        int nbByes          = bracketSize - n;
        int nbRoundsWinners = log2(bracketSize);
        // LB = 2*(nbRoundsWinners-1) rounds : alterné chute/interne, le dernier étant la finale LB
        int nbRoundsLB      = 2 * (nbRoundsWinners - 1);

        // ── Création WB ──
        List<List<MatchTournoi>> roundsWB = new ArrayList<>();
        for (int r = 1; r <= nbRoundsWinners; r++) {
            int nb = bracketSize / (1 << r);
            List<MatchTournoi> round = new ArrayList<>();
            for (int i = 0; i < nb; i++) {
                MatchTournoi m = new MatchTournoi(tournoi, BracketTypeEnum.WINNERS, r, null, null);
                em.persist(m);
                round.add(m);
            }
            roundsWB.add(round);
        }

        // Liaison interne WB
        for (int r = 0; r < nbRoundsWinners - 1; r++) {
            List<MatchTournoi> cur  = roundsWB.get(r);
            List<MatchTournoi> next = roundsWB.get(r + 1);
            for (int i = 0; i < cur.size(); i++) {
                cur.get(i).setProchainMatchGagnant(next.get(i / 2));
                cur.get(i).setSlotProchainMatchGagnant((i % 2 == 0) ? 1 : 2);
            }
        }

        // ── Création LB ──
        // nbLB[r] = nombre de matchs au round r du LB (1-based)
        // R1      : bracketSize/4  (chute WB R1, les perdants s'affrontent entre eux)
        // R pair  : round interne  = moitié du round précédent
        // R impair: round de chute = même nb que le round interne précédent
        int[] nbLB = new int[nbRoundsLB + 1];
        nbLB[1] = Math.max(bracketSize / 4, 1);
        for (int r = 2; r <= nbRoundsLB; r++) {
            if (r % 2 == 0) {
                nbLB[r] = Math.max(nbLB[r - 1] / 2, 1);
            } else {
                nbLB[r] = Math.max(nbLB[r - 1], 1);
            }
        }

        List<List<MatchTournoi>> roundsLB = new ArrayList<>();
        for (int r = 1; r <= nbRoundsLB; r++) {
            List<MatchTournoi> round = new ArrayList<>();
            for (int i = 0; i < nbLB[r]; i++) {
                MatchTournoi m = new MatchTournoi(tournoi, BracketTypeEnum.LOSERS, r, null, null);
                em.persist(m);
                round.add(m);
            }
            roundsLB.add(round);
        }

        // Liaison interne LB
        for (int r = 0; r < nbRoundsLB - 1; r++) {
            List<MatchTournoi> cur  = roundsLB.get(r);
            List<MatchTournoi> next = roundsLB.get(r + 1);
            int roundNum = r + 1; // 1-based
            boolean estInterne = (roundNum % 2 == 0);
            for (int i = 0; i < cur.size(); i++) {
                MatchTournoi c = cur.get(i);
                int idxNext;
                if (estInterne) {
                    idxNext = i / 2;
                    c.setSlotProchainMatchGagnant((i % 2 == 0) ? 1 : 2);
                } else {
                    idxNext = i;
                    c.setSlotProchainMatchGagnant(1);
                }
                if (idxNext < next.size()) {
                    c.setProchainMatchGagnant(next.get(idxNext));
                }
            }
        }

        // ── Liaison WB → LB (perdants) ──
        // WB R1        → LB R1    : paires (2 perdants → 1 match LB)
        // WB Rk (k≥2) → LB R(2k-2)  : 1 perdant → slot 2 du match LB correspondant
        for (int wbR = 1; wbR <= nbRoundsWinners; wbR++) {
            List<MatchTournoi> wbRound = roundsWB.get(wbR - 1);

            int lbR = (wbR == 1) ? 1 : 2 * (wbR - 1);

            if (lbR > nbRoundsLB || lbR < 1) continue;
            List<MatchTournoi> lbRound = roundsLB.get(lbR - 1);

            if (wbR == 1) {
                for (int i = 0; i < wbRound.size(); i++) {
                    int idxLB = i / 2;
                    if (idxLB >= lbRound.size()) continue;
                    wbRound.get(i).setProchainMatchPerdant(lbRound.get(idxLB));
                    wbRound.get(i).setSlotProchainMatchPerdant((i % 2 == 0) ? 1 : 2);
                }
            } else {
                for (int i = 0; i < wbRound.size(); i++) {
                    if (i >= lbRound.size()) continue;
                    wbRound.get(i).setProchainMatchPerdant(lbRound.get(i));
                    wbRound.get(i).setSlotProchainMatchPerdant(2);
                }
            }
        }

        // ── Grand Final ──
        MatchTournoi finaleWB = roundsWB.get(nbRoundsWinners - 1).get(0);
        MatchTournoi finaleLB = roundsLB.get(nbRoundsLB - 1).get(0);

        MatchTournoi gf1 = new MatchTournoi(tournoi, BracketTypeEnum.GRAND_FINAL, 1, null, null);
        em.persist(gf1);
        MatchTournoi gf2 = new MatchTournoi(tournoi, BracketTypeEnum.GRAND_FINAL, 2, null, null);
        em.persist(gf2);

        finaleWB.setProchainMatchGagnant(gf1);
        finaleWB.setSlotProchainMatchGagnant(1);
        finaleLB.setProchainMatchGagnant(gf1);
        finaleLB.setSlotProchainMatchGagnant(2);
        gf1.setProchainMatchGagnant(gf2);

        // ── Remplissage Round 1 WB ──
        remplirRound1Winners(joueurs, n, nbByes, roundsWB.get(0));

        // ── Auto-complétion byes LB ──
        autoCompleterByesLoser(roundsLB);
    }

    // =========================================================================
    // REMPLISSAGE ROUND 1 WB
    // =========================================================================

    private void remplirRound1Winners(List<Joueur> joueurs, int n, int nbByes,
                                      List<MatchTournoi> firstRound) {
        int indexMatch = 0;
        int left  = 0;
        int right = n - 1;

        for (int b = 0; b < nbByes && indexMatch < firstRound.size() && left <= right; b++) {
            Joueur top = joueurs.get(left++);
            MatchTournoi m = firstRound.get(indexMatch++);
            m.setJoueur1(top);
            m.setGagnant(top);
            m.setScoreJoueur1(1);
            m.setScoreJoueur2(0);
            m.setTermine(true);
            propagerGagnant(m, top);
        }

        while (left < right && indexMatch < firstRound.size()) {
            Joueur p1 = joueurs.get(left++);
            Joueur p2 = joueurs.get(right--);
            MatchTournoi m = firstRound.get(indexMatch++);
            m.setJoueur1(p1);
            m.setJoueur2(p2);
        }

        if (left == right && indexMatch < firstRound.size()) {
            Joueur seul = joueurs.get(left);
            MatchTournoi m = firstRound.get(indexMatch);
            m.setJoueur1(seul);
            m.setGagnant(seul);
            m.setScoreJoueur1(1);
            m.setScoreJoueur2(0);
            m.setTermine(true);
            propagerGagnant(m, seul);
        }
    }

    // =========================================================================
    // AUTO-COMPLÉTION BYES LB
    // =========================================================================

    private void autoCompleterByesLoser(List<List<MatchTournoi>> roundsLosers) {
        for (List<MatchTournoi> round : roundsLosers) {
            for (MatchTournoi m : round) {
                boolean j1 = m.getJoueur1() != null;
                boolean j2 = m.getJoueur2() != null;
                if (!j1 && !j2) continue;
                if (j1 && !j2) {
                    m.setGagnant(m.getJoueur1());
                    m.setScoreJoueur1(1);
                    m.setScoreJoueur2(0);
                    m.setTermine(true);
                    propagerGagnant(m, m.getJoueur1());
                } else if (!j1) {
                    m.setGagnant(m.getJoueur2());
                    m.setScoreJoueur1(0);
                    m.setScoreJoueur2(1);
                    m.setTermine(true);
                    propagerGagnant(m, m.getJoueur2());
                }
            }
        }
    }

    // =========================================================================
    // ENCODER RÉSULTAT
    // =========================================================================

    public void encoderResultat(Long matchId, int scoreJoueur1, int scoreJoueur2) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            MatchTournoi match = em.find(MatchTournoi.class, matchId);
            if (match == null) throw new IllegalArgumentException("Match introuvable.");
            if (Boolean.TRUE.equals(match.getTermine()))
                throw new IllegalStateException("Ce match est déjà terminé.");
            if (match.getJoueur1() == null || match.getJoueur2() == null)
                throw new IllegalStateException("Les deux joueurs doivent être définis.");
            if (scoreJoueur1 == scoreJoueur2)
                throw new IllegalArgumentException("Un match ne peut pas se terminer sur une égalité.");

            match.setScoreJoueur1(scoreJoueur1);
            match.setScoreJoueur2(scoreJoueur2);

            Joueur gagnant = scoreJoueur1 > scoreJoueur2 ? match.getJoueur1() : match.getJoueur2();
            Joueur perdant = scoreJoueur1 > scoreJoueur2 ? match.getJoueur2() : match.getJoueur1();

            match.setGagnant(gagnant);
            match.setTermine(true);

            switch (match.getBracketType()) {
                case WINNERS -> {
                    propagerGagnant(match, gagnant);
                    gererPerdantWinners(em, match, perdant);
                }
                case LOSERS -> {
                    propagerGagnant(match, gagnant);
                    eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
                }
                case GRAND_FINAL -> {
                    gererGrandFinal(em, match, gagnant, perdant);
                }
            }

            verifierFinTournoi(em, match.getTournoi().getId());
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // =========================================================================
    // GESTION GRAND FINAL
    // =========================================================================

    private void gererGrandFinal(EntityManager em, MatchTournoi match,
                                 Joueur gagnant, Joueur perdant) {
        if (match.getRoundNumber() == 1) {
            Joueur joueurWB = match.getJoueur1();
            Joueur joueurLB = match.getJoueur2();

            if (gagnant.getId().equals(joueurWB.getId())) {
                MatchTournoi bracketReset = match.getProchainMatchGagnant();
                if (bracketReset != null) bracketReset.setTermine(true);
                eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
            } else {
                MatchTournoi bracketReset = match.getProchainMatchGagnant();
                if (bracketReset == null)
                    throw new IllegalStateException("Bracket reset introuvable.");
                bracketReset.setJoueur1(joueurWB);
                bracketReset.setJoueur2(joueurLB);
                bracketReset.setTermine(false);
            }
        } else if (match.getRoundNumber() == 2) {
            eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
        }
    }

    // =========================================================================
    // HELPERS PRIVÉS
    // =========================================================================

    private List<Joueur> seederJoueurs(List<Joueur> joueurs) {
        List<Joueur> sorted = new ArrayList<>(joueurs);
        sorted.sort(Comparator.comparingInt((Joueur j) -> j.getRank().ordinal()).reversed());

        int debut = 0;
        for (int i = 1; i <= sorted.size(); i++) {
            boolean finGroupe = (i == sorted.size())
                    || (sorted.get(i).getRank().ordinal() != sorted.get(debut).getRank().ordinal());
            if (finGroupe && i - debut > 1) Collections.shuffle(sorted.subList(debut, i));
            if (finGroupe) debut = i;
        }
        return sorted;
    }

    private void propagerGagnant(MatchTournoi match, Joueur gagnant) {
        MatchTournoi prochain = match.getProchainMatchGagnant();
        if (prochain == null) return;

        Integer slot = match.getSlotProchainMatchGagnant();
        if (slot != null && slot == 1) {
            prochain.setJoueur1(gagnant);
        } else if (slot != null && slot == 2) {
            prochain.setJoueur2(gagnant);
        } else {
            if (prochain.getJoueur1() == null) prochain.setJoueur1(gagnant);
            else if (prochain.getJoueur2() == null) prochain.setJoueur2(gagnant);
        }
    }

    private void gererPerdantWinners(EntityManager em, MatchTournoi match, Joueur perdant) {
        MatchTournoi matchLosers = match.getProchainMatchPerdant();

        if (matchLosers == null) {
            eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
            return;
        }

        Integer slot = match.getSlotProchainMatchPerdant();
        if (slot != null && slot == 1) {
            if (matchLosers.getJoueur1() != null)
                throw new IllegalStateException("Slot 1 du match LB déjà occupé.");
            matchLosers.setJoueur1(perdant);
        } else if (slot != null && slot == 2) {
            if (matchLosers.getJoueur2() != null)
                throw new IllegalStateException("Slot 2 du match LB déjà occupé.");
            matchLosers.setJoueur2(perdant);
        } else {
            if (matchLosers.getJoueur1() == null) matchLosers.setJoueur1(perdant);
            else if (matchLosers.getJoueur2() == null) matchLosers.setJoueur2(perdant);
            else throw new IllegalStateException("Match losers cible déjà complet.");
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

        List<MatchTournoi> matchsGF = em.createQuery("""
                SELECT m FROM MatchTournoi m
                WHERE m.tournoi.id = :tournoiId
                  AND m.bracketType = :type
                ORDER BY m.roundNumber DESC
                """, MatchTournoi.class)
                .setParameter("tournoiId", tournoiId)
                .setParameter("type", BracketTypeEnum.GRAND_FINAL)
                .getResultList();

        if (matchsGF.isEmpty()) return;

        MatchTournoi gf1 = null, gf2 = null;
        for (MatchTournoi m : matchsGF) {
            if (m.getRoundNumber() == 1) gf1 = m;
            if (m.getRoundNumber() == 2) gf2 = m;
        }

        boolean termine = false;
        if (gf2 != null && Boolean.TRUE.equals(gf2.getTermine())) {
            termine = true;
        } else if (gf1 != null && Boolean.TRUE.equals(gf1.getTermine())) {
            if (gf2 == null) {
                termine = true;
            } else if (Boolean.TRUE.equals(gf2.getTermine())
                    && gf2.getJoueur1() == null && gf2.getJoueur2() == null) {
                termine = true;
            }
        }

        if (termine) tournoi.setEtat(EtatTournoiEnum.TERMINE);
    }

    // =========================================================================
    // UTILITAIRES MATHÉMATIQUES
    // =========================================================================

    private int prochaineDeuxPuissanceDe(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    private int log2(int n) {
        int r = 0;
        while (n > 1) { n >>= 1; r++; }
        return r;
    }
}