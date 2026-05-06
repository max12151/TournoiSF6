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

/**
 * Service responsable de la gestion des matchs de tournoi.
 * Implémente le format Double Élimination avec Winner Bracket,
 * Loser Bracket, Grand Final et Bracket Reset.
 */
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
    // LANCER TOURNOI — Point d'entrée principal
    // =========================================================================

    /**
     * Génère l'intégralité de la structure de matchs en base de données
     * au moment du lancement du tournoi.
     * Gère le cas spécial à 2 joueurs (Grand Final directe) et le cas
     * général 3+ joueurs (double élimination complète).
     */
    public void lancerTournoi(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // --- Validation du tournoi ---
            Tournoi tournoi = em.find(Tournoi.class, tournoiId);
            if (tournoi == null) {
                throw new IllegalArgumentException("Tournoi introuvable.");
            }
            if (tournoi.getEtat() != EtatTournoiEnum.EN_ATTENTE) {
                throw new IllegalStateException("Le tournoi n'est pas dans un état lançable.");
            }

            // --- Récupération des joueurs inscrits non éliminés ---
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

            if (joueurs.size() < 2) {
                throw new IllegalStateException("Il faut au moins 2 joueurs pour lancer le tournoi.");
            }

            // ─────────────────────────────────────────────────
            // CAS SPÉCIAL : 2 JOUEURS → Grand Final directe
            // ─────────────────────────────────────────────────
            if (joueurs.size() == 2) {
                lancerTournoiDeuxJoueurs(em, tournoi, joueurs);
            } else {
                // CAS GÉNÉRAL : 3+ joueurs → double élimination complète
                lancerTournoiDoubleElimination(em, tournoi, joueurs);
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

    // =========================================================================
    // CAS SPÉCIAL : 2 JOUEURS
    // =========================================================================

    /**
     * Pour exactement 2 joueurs : crée un unique match GRAND_FINAL round 1.
     * Pas de winner/loser bracket, pas de bracket reset.
     */
    private void lancerTournoiDeuxJoueurs(EntityManager em, Tournoi tournoi, List<Joueur> joueurs) {
        MatchTournoi gf = new MatchTournoi(
                tournoi,
                BracketTypeEnum.GRAND_FINAL,
                1,
                joueurs.get(0),
                joueurs.get(1)
        );
        em.persist(gf);
        // Pas de prochainMatchGagnant : fin du tournoi après ce match
    }

    // =========================================================================
    // CAS GÉNÉRAL : DOUBLE ÉLIMINATION
    // =========================================================================

    /**
     * Orchestre la création complète du bracket double élimination :
     * Winner Bracket → Loser Bracket → Grand Final + Bracket Reset.
     */
    private void lancerTournoiDoubleElimination(EntityManager em, Tournoi tournoi, List<Joueur> joueurs) {

        int n = joueurs.size();

        // ── A. SEEDING ──
        // Tri décroissant par rank (MASTER = ordinal le plus élevé, donc en premier)
        // En cas d'égalité de rang : ordre aléatoire (shuffle du sous-groupe)
        joueurs = seederJoueurs(joueurs);

        // ── B. CALCUL DU BRACKET ──
        int bracketSize = prochaineDeuxPuissanceDe(n);
        int nbByes = bracketSize - n;
        int nbRoundsWinners = log2(bracketSize); // ex: bracketSize=8 → 3 rounds

        // ── C. GÉNÉRATION DU WINNER BRACKET ──
        // roundsWinners.get(r) = liste des matchs du round r+1 (index 0 = round 1)
        List<List<MatchTournoi>> roundsWinners = creerWinnerBracket(em, tournoi, bracketSize, nbRoundsWinners);

        // ── D. GÉNÉRATION DU LOSER BRACKET ──
        // Retourne la liste de TOUS les rounds LB, indexés 0..nbRoundsLB-1
        // nbRoundsLB = 2*(nbRoundsWinners - 1) + 1 = 2*nbRoundsWinners - 1
        List<List<MatchTournoi>> roundsLosers = creerLoserBracket(em, tournoi, bracketSize, nbRoundsWinners, roundsWinners);

        // ── E. GRAND FINAL + BRACKET RESET ──
        creerGrandFinal(em, tournoi, roundsWinners, roundsLosers, nbRoundsWinners);

        // ── F. REMPLISSAGE DU ROUND 1 WB (joueurs + byes) ──
        remplirRound1Winners(joueurs, n, nbByes, roundsWinners.get(0));

        // ── G. AUTO-COMPLÉTION DES BYES LB ──
        // Après le placement WB R1, certains matchs LB R1 peuvent avoir un seul joueur
        autoCompleterByesLoser(roundsLosers);
    }

    // =========================================================================
    // ── C. WINNER BRACKET
    // =========================================================================

    /**
     * Crée tous les matchs du winner bracket et établit les liaisons
     * "prochain match gagnant" entre rounds consécutifs.
     * IMPORTANT : tous les em.persist() AVANT les liaisons (contrainte JPA).
     */
    private List<List<MatchTournoi>> creerWinnerBracket(
            EntityManager em, Tournoi tournoi, int bracketSize, int nbRoundsWinners) {

        List<List<MatchTournoi>> rounds = new ArrayList<>();

        // --- Création des matchs de chaque round ---
        for (int r = 1; r <= nbRoundsWinners; r++) {
            int nbMatchs = bracketSize / (1 << r); // bracketSize / 2^r
            List<MatchTournoi> round = new ArrayList<>();

            for (int i = 0; i < nbMatchs; i++) {
                MatchTournoi m = new MatchTournoi(tournoi, BracketTypeEnum.WINNERS, r, null, null);
                em.persist(m); // persist AVANT les liaisons
                round.add(m);
            }
            rounds.add(round);
        }

        // --- Liaison prochain match gagnant : match[r][i] → match[r+1][i/2] ---
        for (int r = 0; r < nbRoundsWinners - 1; r++) {
            List<MatchTournoi> currentRound = rounds.get(r);
            List<MatchTournoi> nextRound    = rounds.get(r + 1);

            for (int i = 0; i < currentRound.size(); i++) {
                MatchTournoi current = currentRound.get(i);
                MatchTournoi next    = nextRound.get(i / 2);

                current.setProchainMatchGagnant(next);
                // Slot 1 pour les matchs d'index pair, slot 2 pour les impairs
                current.setSlotProchainMatchGagnant((i % 2 == 0) ? 1 : 2);
            }
        }

        return rounds;
    }

    // =========================================================================
    // ── D. LOSER BRACKET
    // =========================================================================

    /**
     * Crée les matchs du loser bracket et établit les liaisons :
     *   - depuis les matchs WB → matchs LB correspondants (perdants WB)
     *   - liaisons internes LB entre rounds consécutifs
     *
     * Structure LB pour nbRoundsWinners = N :
     *   nbRoundsLB = 2*N - 1 rounds
     *   Rounds impairs (1, 3, 5...) = rounds "de chute" : arrivent les perdants WB
     *   Rounds pairs   (2, 4, 6...) = rounds "internes" : survivants LB seulement
     *
     *   LB Round (2r-1) reçoit les perdants de WB Round r
     *   Nombre de matchs par round LB (pour bracketSize S) :
     *     LB R1 : S/4 matchs, LB R2 : S/4, LB R3 : S/8, LB R4 : S/8, ...
     *     En général : nbMatchsLB[r] = bracketSize / (2 * 2^ceil(r/2))
     *
     * Méthode de calcul simplifiée :
     *   Pour chaque WB round r (0-indexed), les perdants tombent dans LB round (2r).
     *   Le nombre de matchs LB round (2r) = nombre de matchs WB round r.
     *   Les rounds LB impairs (internes) ont la moitié du round de chute précédent.
     */
    private List<List<MatchTournoi>> creerLoserBracket(
            EntityManager em, Tournoi tournoi,
            int bracketSize, int nbRoundsWinners,
            List<List<MatchTournoi>> roundsWinners) {

        // nbRoundsLB = 2*(N-1) + 1 = 2N-1
        int nbRoundsLB = 2 * nbRoundsWinners - 1;
        List<List<MatchTournoi>> roundsLosers = new ArrayList<>();

        // --- Calcul du nombre de matchs par round LB ---
        // roundLB 1-indexed. On calcule d'abord tous les comptes.
        int[] nbMatchsParRoundLB = new int[nbRoundsLB + 1]; // index 1..nbRoundsLB

        // Le LB Round 1 reçoit les perdants du WB Round 1 → nbMatchs WB R1 / 2
        // (car les perdants du WB R1 jouent entre eux)
        // WB R1 a bracketSize/2 matchs → LB R1 a bracketSize/4 matchs
        // En fait LB R1 = nbMatchsWBR1 / 2 = (bracketSize/2) / 2 = bracketSize/4
        //
        // Règle générale :
        //   - LB Round (2k-1) [round de chute, k=1..N-1] :
        //       nbMatchs = nbMatchsWBRound(k)   (perdants WB Rk jouent vs gagnants LB round précédent)
        //       Sauf LB R1 : perdants WB R1 jouent ENTRE EUX → nbMatchs = nbMatchsWBR1 / 2
        //   - LB Round (2k) [round interne, k=1..N-1] :
        //       nbMatchs = nbMatchsLBRoundPrecedent = nbMatchsLBRound(2k-1)
        //       (les gagnants du round de chute s'affrontent)
        //
        // LB R1  (de chute) : bracketSize/4
        // LB R2  (interne)  : bracketSize/4
        // LB R3  (de chute) : bracketSize/4   ← perdants WB R2 vs gagnants LB R2
        // LB R4  (interne)  : bracketSize/8
        // LB R5  (de chute) : bracketSize/8   ← perdants WB R3 vs gagnants LB R4
        // LB R6  (interne)  : bracketSize/16
        // ...
        // LB R(2N-1) finale losers : 1 match

        // Calcul par paires de rounds (chute + interne)
        int nbMatchsCourant = bracketSize / 4;
        // LB R1
        nbMatchsParRoundLB[1] = nbMatchsCourant;
        for (int k = 1; k <= nbRoundsWinners - 1; k++) {
            int lbRonde_chute  = 2 * k - 1;
            int lbRonde_interne = 2 * k;

            if (lbRonde_chute > nbRoundsLB) break;

            // Le round de chute a autant de matchs que le round interne précédent
            // (sauf pour R1 déjà calculé)
            if (lbRonde_chute > 1) {
                nbMatchsParRoundLB[lbRonde_chute] = nbMatchsParRoundLB[lbRonde_chute - 1];
            }
            // Le round interne a la moitié du round de chute
            if (lbRonde_interne <= nbRoundsLB) {
                nbMatchsParRoundLB[lbRonde_interne] = nbMatchsParRoundLB[lbRonde_chute] / 2;
                if (nbMatchsParRoundLB[lbRonde_interne] < 1) {
                    nbMatchsParRoundLB[lbRonde_interne] = 1;
                }
            }
        }
        // La finale LB est toujours 1 match
        nbMatchsParRoundLB[nbRoundsLB] = 1;

        // --- Création des matchs LB (tous les persist d'abord) ---
        for (int r = 1; r <= nbRoundsLB; r++) {
            int nb = nbMatchsParRoundLB[r];
            if (nb < 1) nb = 1;
            List<MatchTournoi> round = new ArrayList<>();

            for (int i = 0; i < nb; i++) {
                MatchTournoi m = new MatchTournoi(tournoi, BracketTypeEnum.LOSERS, r, null, null);
                em.persist(m);
                round.add(m);
            }
            roundsLosers.add(round); // index 0 = LB round 1
        }

        // --- Liaison interne LB : gagnant d'un match LB → match LB suivant ---
        for (int r = 0; r < nbRoundsLB - 1; r++) {
            List<MatchTournoi> currentRound = roundsLosers.get(r);
            List<MatchTournoi> nextRound    = roundsLosers.get(r + 1);

            for (int i = 0; i < currentRound.size(); i++) {
                MatchTournoi current = currentRound.get(i);

                // Dans les rounds internes (index pair), les 2 matchs alimentent 1 match
                // Dans les rounds de chute (index impair), 1 match alimente 1 match
                int indexNext;
                int roundNumLB = r + 1; // 1-indexed
                boolean estRoundInterne = (roundNumLB % 2 == 0);

                if (estRoundInterne) {
                    // Round interne : 2 gagnants → 1 match suivant (comme WB)
                    indexNext = i / 2;
                    current.setSlotProchainMatchGagnant((i % 2 == 0) ? 1 : 2);
                } else {
                    // Round de chute : 1 gagnant → 1 match du round interne (1 pour 1)
                    indexNext = i;
                    current.setSlotProchainMatchGagnant(1); // slot 1 = survivant LB
                }

                if (indexNext < nextRound.size()) {
                    current.setProchainMatchGagnant(nextRound.get(indexNext));
                }
            }
        }

        // --- Liaison WB → LB : perdants WB tombent dans le bon round LB ---
        // Perdants WB Round r → LB Round (2r-1)
        // Ex : perdants WB R1 → LB R1, perdants WB R2 → LB R3, perdants WB R3 → LB R5
        //
        // LB R1 : perdants WB R1 jouent entre eux
        //   → match WB R1 [i] → son perdant va dans LB R1 [i/2], slot selon parité
        // LB R(2r-1) pour r >= 2 : perdants WB Rr vs gagnants LB round précédent
        //   → perdant WB R r [i] arrive dans LB R(2r-1) [i], slot 2 (slot 1 = gagnant LB)
        for (int wbRoundIndex = 0; wbRoundIndex < roundsWinners.size(); wbRoundIndex++) {
            int wbRound = wbRoundIndex + 1; // 1-indexed
            int lbRoundCible = 2 * wbRound - 1; // LB round où tombent les perdants
            if (lbRoundCible > nbRoundsLB) break;

            List<MatchTournoi> wbRoundMatchs = roundsWinners.get(wbRoundIndex);
            List<MatchTournoi> lbRoundMatchs = roundsLosers.get(lbRoundCible - 1); // 0-indexed

            if (wbRound == 1) {
                // Perdants WB R1 jouent entre eux en LB R1
                // WB R1[0] et WB R1[1] → LB R1[0]
                // WB R1[2] et WB R1[3] → LB R1[1]
                // etc.
                for (int i = 0; i < wbRoundMatchs.size(); i++) {
                    MatchTournoi wbMatch  = wbRoundMatchs.get(i);
                    int indexLB = i / 2;
                    if (indexLB < lbRoundMatchs.size()) {
                        MatchTournoi lbMatch = lbRoundMatchs.get(indexLB);
                        wbMatch.setProchainMatchPerdant(lbMatch);
                        wbMatch.setSlotProchainMatchPerdant((i % 2 == 0) ? 1 : 2);
                    }
                }
            } else {
                // Perdants WB Rr arrivent dans LB R(2r-1) en slot 2
                // (slot 1 est réservé au survivant du LB round précédent)
                for (int i = 0; i < wbRoundMatchs.size(); i++) {
                    MatchTournoi wbMatch = wbRoundMatchs.get(i);
                    if (i < lbRoundMatchs.size()) {
                        MatchTournoi lbMatch = lbRoundMatchs.get(i);
                        wbMatch.setProchainMatchPerdant(lbMatch);
                        wbMatch.setSlotProchainMatchPerdant(2); // slot 2 = perdant WB
                    }
                }
            }
        }

        return roundsLosers;
    }

    // =========================================================================
    // ── E. GRAND FINAL + BRACKET RESET
    // =========================================================================

    /**
     * Crée les deux matchs de la Grand Final :
     *   - GF Round 1 : gagnant WB finale (joueur1) vs gagnant LB finale (joueur2)
     *   - GF Round 2 (bracket reset) : créé à l'avance, joueurs null, termine=false
     * Établit les liaisons depuis la finale WB et la finale LB vers la GF.
     */
    private void creerGrandFinal(EntityManager em, Tournoi tournoi,
                                 List<List<MatchTournoi>> roundsWinners,
                                 List<List<MatchTournoi>> roundsLosers,
                                 int nbRoundsWinners) {

        // --- Finale WB = dernier round du winner bracket ---
        MatchTournoi finaleWB = roundsWinners.get(nbRoundsWinners - 1).get(0);

        // --- Finale LB = dernier round du loser bracket ---
        List<MatchTournoi> dernierRoundLB = roundsLosers.get(roundsLosers.size() - 1);
        MatchTournoi finaleLB = dernierRoundLB.get(0);

        // --- Création Grand Final Round 1 ---
        MatchTournoi gf1 = new MatchTournoi(tournoi, BracketTypeEnum.GRAND_FINAL, 1, null, null);
        em.persist(gf1);

        // --- Création Bracket Reset (Grand Final Round 2) ---
        MatchTournoi gf2 = new MatchTournoi(tournoi, BracketTypeEnum.GRAND_FINAL, 2, null, null);
        em.persist(gf2);

        // --- Liaisons vers la GF Round 1 ---
        // Gagnant WB finale → joueur1 de la GF (invaincu)
        finaleWB.setProchainMatchGagnant(gf1);
        finaleWB.setSlotProchainMatchGagnant(1);

        // Gagnant LB finale → joueur2 de la GF
        finaleLB.setProchainMatchGagnant(gf1);
        finaleLB.setSlotProchainMatchGagnant(2);

        // --- Liaison GF Round 1 → GF Round 2 (bracket reset) ---
        // La propagation vers GF R2 est gérée dynamiquement dans encoderResultat()
        // selon qui gagne la GF R1. On lie quand même pour que l'entité existe.
        gf1.setProchainMatchGagnant(gf2);
        // Le slot sera défini dans encoderResultat() si le reset est activé
    }

    // =========================================================================
    // ── F. REMPLISSAGE DU ROUND 1 WB
    // =========================================================================

    /**
     * Place les joueurs dans les matchs du round 1 WB :
     *   - Les nbByes premiers joueurs (les meilleurs) reçoivent un bye automatique
     *   - Les joueurs restants sont appariés en snake seeding (meilleur vs moins bon)
     * Appelle propagerGagnant() immédiatement pour les byes.
     */
    private void remplirRound1Winners(List<Joueur> joueurs, int n, int nbByes,
                                      List<MatchTournoi> firstRound) {

        int indexMatch = 0;
        int left  = 0;       // pointeur vers les meilleurs
        int right = n - 1;   // pointeur vers les moins bons

        // 1) Byes pour les nbByes meilleurs joueurs
        for (int b = 0; b < nbByes && indexMatch < firstRound.size() && left <= right; b++) {
            Joueur top = joueurs.get(left++);
            MatchTournoi m = firstRound.get(indexMatch++);

            m.setJoueur1(top);
            // joueur2 = null → bye
            m.setGagnant(top);
            m.setScoreJoueur1(1);
            m.setScoreJoueur2(0);
            m.setTermine(true);

            // Propager immédiatement le gagnant vers le match suivant WB
            propagerGagnant(m, top);
            // Note : le match LB lié à ce bye garde joueur1/joueur2 = null (bye LB)
        }

        // 2) Snake seeding pour les joueurs restants : meilleur vs moins bon
        while (left < right && indexMatch < firstRound.size()) {
            Joueur p1 = joueurs.get(left++);
            Joueur p2 = joueurs.get(right--);

            MatchTournoi m = firstRound.get(indexMatch++);
            m.setJoueur1(p1);
            m.setJoueur2(p2);
        }

        // Cas où il reste un joueur impair (ne peut pas arriver avec les byes
        // correctement calculés, mais sécurité)
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
    // ── G. AUTO-COMPLÉTION DES BYES LB
    // =========================================================================

    /**
     * Après le remplissage du WB R1, certains matchs LB R1 peuvent avoir
     * un seul joueur (parce que l'autre était un bye WB).
     * - joueur1 null ET joueur2 null → slot fantôme, rien à faire
     * - un seul joueur présent → bye LB : le joueur est gagnant automatique,
     *   le match est marqué terminé et le gagnant propagé
     */
    // ── G. AUTO-COMPLÉTION DES BYES LB (cascade sur tous les rounds) ──
    private void autoCompleterByesLoser(List<List<MatchTournoi>> roundsLosers) {
        for (List<MatchTournoi> round : roundsLosers) {
            for (MatchTournoi m : round) {
                boolean j1 = m.getJoueur1() != null;
                boolean j2 = m.getJoueur2() != null;

                if (!j1 && !j2) continue; // slot fantôme

                if (j1 && !j2) {
                    m.setGagnant(m.getJoueur1());
                    m.setScoreJoueur1(1);
                    m.setScoreJoueur2(0);
                    m.setTermine(true);
                    propagerGagnant(m, m.getJoueur1()); // peut remplir le match suivant
                } else if (!j1 && j2) {
                    m.setGagnant(m.getJoueur2());
                    m.setScoreJoueur1(0);
                    m.setScoreJoueur2(1);
                    m.setTermine(true);
                    propagerGagnant(m, m.getJoueur2());
                }
                // Les deux présents → match normal
            }
        }
    }

    // =========================================================================
    // ENCODER RÉSULTAT
    // =========================================================================

    /**
     * Encode le résultat d'un match et propage les joueurs dans le bracket.
     * Gère les cas spéciaux :
     *   - Match WINNERS : le perdant descend en LB
     *   - Match LOSERS  : le perdant est éliminé
     *   - Match GRAND_FINAL Round 1 : bracket reset si le joueur LB gagne
     *   - Match GRAND_FINAL Round 2 : le gagnant est champion, tournoi terminé
     */
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

            // Propagation selon le type de bracket
            switch (match.getBracketType()) {

                case WINNERS -> {
                    // Gagnant monte dans le WB
                    propagerGagnant(match, gagnant);
                    // Perdant descend en LB
                    gererPerdantWinners(em, match, perdant);
                }

                case LOSERS -> {
                    // Gagnant continue en LB (ou va en GF)
                    propagerGagnant(match, gagnant);
                    // Perdant LB → éliminé définitivement
                    eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
                }

                case GRAND_FINAL -> {
                    gererGrandFinal(em, match, gagnant, perdant);
                }
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

    // =========================================================================
    // GESTION GRAND FINAL
    // =========================================================================

    /**
     * Gère la logique spécifique à la Grand Final :
     *
     * GF Round 1 :
     *   - joueur1 = invaincu (vient du WB), joueur2 = survivant LB
     *   - Si joueur1 (WB) gagne → tournoi terminé, pas de bracket reset
     *   - Si joueur2 (LB) gagne → bracket reset activé (GF R2 avec les 2 joueurs)
     *
     * GF Round 2 (bracket reset) :
     *   - Le gagnant est le champion incontesté du tournoi
     */
    private void gererGrandFinal(EntityManager em, MatchTournoi match,
                                 Joueur gagnant, Joueur perdant) {

        if (match.getRoundNumber() == 1) {
            // --- GF Round 1 ---
            Joueur joueurWB = match.getJoueur1(); // invaincu, vient du WB
            Joueur joueurLB = match.getJoueur2(); // a perdu une fois, vient du LB

            if (gagnant.getId().equals(joueurWB.getId())) {
                // Le joueur WB (invaincu) gagne → pas de bracket reset
                // Marquer le bracket reset comme terminé (ne sera jamais joué)
                MatchTournoi bracketReset = match.getProchainMatchGagnant();
                if (bracketReset != null) {
                    bracketReset.setTermine(true); // désactivé, ne sera pas joué
                }
                // Le perdant LB est éliminé
                eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());

            } else {
                // Le joueur LB gagne → bracket reset activé !
                // Propager les deux joueurs dans le bracket reset (GF R2)
                MatchTournoi bracketReset = match.getProchainMatchGagnant();
                if (bracketReset == null) {
                    throw new IllegalStateException("Bracket reset introuvable pour la Grand Final.");
                }
                // Convention : joueur1 = ex-joueur WB, joueur2 = ex-joueur LB
                bracketReset.setJoueur1(joueurWB);
                bracketReset.setJoueur2(joueurLB);
                bracketReset.setTermine(false); // le match doit bien être joué
            }

        } else if (match.getRoundNumber() == 2) {
            // --- GF Round 2 (bracket reset) ---
            // Le gagnant est champion, le perdant est éliminé
            eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
            // verifierFinTournoi() sera appelé juste après dans encoderResultat()
        }
    }

    // =========================================================================
    // HELPERS PRIVÉS
    // =========================================================================

    /**
     * Trie les joueurs par rank décroissant (MASTER en premier).
     * En cas d'égalité de rang, l'ordre au sein du sous-groupe est aléatoire.
     */
    private List<Joueur> seederJoueurs(List<Joueur> joueurs) {
        // Copie pour ne pas modifier la liste originale
        List<Joueur> sorted = new ArrayList<>(joueurs);

        // Tri par rank ordinal décroissant
        sorted.sort(Comparator.comparingInt((Joueur j) -> j.getRank().ordinal()).reversed());

        // Shuffle des sous-groupes à rank égal pour l'aléatoire à rang identique
        int debut = 0;
        for (int i = 1; i <= sorted.size(); i++) {
            boolean finGroupe = (i == sorted.size())
                    || (sorted.get(i).getRank().ordinal() != sorted.get(debut).getRank().ordinal());
            if (finGroupe && i - debut > 1) {
                Collections.shuffle(sorted.subList(debut, i));
            }
            if (finGroupe) debut = i;
        }

        return sorted;
    }

    /**
     * Propage le gagnant dans le slot approprié du prochain match.
     * Utilise slotProchainMatchGagnant (1 → joueur1, 2 → joueur2).
     */
    private void propagerGagnant(MatchTournoi match, Joueur gagnant) {
        MatchTournoi prochain = match.getProchainMatchGagnant();
        if (prochain == null) return;

        Integer slot = match.getSlotProchainMatchGagnant();

        if (slot != null && slot == 1) {
            prochain.setJoueur1(gagnant);
        } else if (slot != null && slot == 2) {
            prochain.setJoueur2(gagnant);
        } else {
            // Fallback : remplir le premier slot libre
            if (prochain.getJoueur1() == null) {
                prochain.setJoueur1(gagnant);
            } else if (prochain.getJoueur2() == null) {
                prochain.setJoueur2(gagnant);
            }
        }
    }

    /**
     * Envoie le perdant d'un match WB dans le match LB correspondant.
     * Utilise slotProchainMatchPerdant.
     */
    private void gererPerdantWinners(EntityManager em, MatchTournoi match, Joueur perdant) {
        MatchTournoi matchLosers = match.getProchainMatchPerdant();

        if (matchLosers == null) {
            // Aucun match LB lié (ne devrait pas arriver normalement)
            eliminerJoueur(em, match.getTournoi().getId(), perdant.getId());
            return;
        }

        Integer slot = match.getSlotProchainMatchPerdant();

        if (slot != null && slot == 1) {
            if (matchLosers.getJoueur1() != null) {
                throw new IllegalStateException("Slot 1 du match LB déjà occupé.");
            }
            matchLosers.setJoueur1(perdant);
        } else if (slot != null && slot == 2) {
            if (matchLosers.getJoueur2() != null) {
                throw new IllegalStateException("Slot 2 du match LB déjà occupé.");
            }
            matchLosers.setJoueur2(perdant);
        } else {
            // Fallback
            if (matchLosers.getJoueur1() == null) {
                matchLosers.setJoueur1(perdant);
            } else if (matchLosers.getJoueur2() == null) {
                matchLosers.setJoueur2(perdant);
            } else {
                throw new IllegalStateException("Match losers cible déjà complet.");
            }
        }
    }

    /**
     * Marque un joueur comme éliminé dans son inscription au tournoi.
     */
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

    /**
     * Vérifie si le tournoi est terminé.
     * En double élimination, le tournoi se termine quand le dernier match
     * GRAND_FINAL (round 1 si pas de reset, round 2 sinon) est joué.
     *
     * On détecte la fin via le dernier match GRAND_FINAL terminé dont le
     * prochainMatchGagnant est null (ou le bracket reset lui-même terminé).
     */
    private void verifierFinTournoi(EntityManager em, Long tournoiId) {
        Tournoi tournoi = em.find(Tournoi.class, tournoiId);

        // Le tournoi est terminé si tous les matchs GRAND_FINAL pertinents sont terminés.
        // On cherche le match GRAND_FINAL de round le plus élevé qui est terminé.
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

        MatchTournoi gf1 = null;
        MatchTournoi gf2 = null;

        for (MatchTournoi m : matchsGF) {
            if (m.getRoundNumber() == 1) gf1 = m;
            if (m.getRoundNumber() == 2) gf2 = m;
        }

        boolean tournoisTermine = false;

        if (gf2 != null && Boolean.TRUE.equals(gf2.getTermine())) {
            // GF Round 2 (bracket reset) terminé → champion désigné
            tournoisTermine = true;
        } else if (gf1 != null && Boolean.TRUE.equals(gf1.getTermine())) {
            // GF Round 1 terminé : vérifier si le bracket reset est désactivé
            // (= le joueur WB a gagné → pas de reset nécessaire)
            if (gf2 != null && Boolean.TRUE.equals(gf2.getTermine())) {
                tournoisTermine = true;
            } else if (gf2 == null) {
                // Pas de bracket reset créé (cas 2 joueurs ou logique sans reset)
                tournoisTermine = true;
            } else if (gf2.getJoueur1() == null && gf2.getJoueur2() == null
                    && Boolean.TRUE.equals(gf2.getTermine())) {
                // Bracket reset désactivé car joueur WB a gagné GF R1
                tournoisTermine = true;
            }
        }

        if (tournoisTermine) {
            tournoi.setEtat(EtatTournoiEnum.TERMINE);
        }
    }

    // =========================================================================
    // UTILITAIRES MATHÉMATIQUES
    // =========================================================================

    /** Retourne la prochaine puissance de 2 supérieure ou égale à n. */
    private int prochaineDeuxPuissanceDe(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    /** Retourne log2(n), en supposant que n est une puissance de 2. */
    private int log2(int n) {
        int r = 0;
        while (n > 1) {
            n >>= 1;
            r++;
        }
        return r;
    }
}