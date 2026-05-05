package be.technifutur.tournoisf6.service;

import be.technifutur.tournoisf6.models.InscriptionTournoi;
import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.models.Tournoi;
import be.technifutur.tournoisf6.models.enums.EtatTournoiEnum;
import be.technifutur.tournoisf6.utils.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class InscriptionTournoiService {

    public List<InscriptionTournoi> findByTournoi(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("""
                    SELECT i FROM InscriptionTournoi i
                    JOIN FETCH i.joueur
                    WHERE i.tournoi.id = :tournoiId
                    ORDER BY i.joueur.pseudo ASC
                    """, InscriptionTournoi.class)
                    .setParameter("tournoiId", tournoiId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean estDejaInscrit(Long tournoiId, Long joueurId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery("""
                    SELECT COUNT(i) FROM InscriptionTournoi i
                    WHERE i.tournoi.id = :tournoiId AND i.joueur.id = :joueurId
                    """, Long.class)
                    .setParameter("tournoiId", tournoiId)
                    .setParameter("joueurId", joueurId)
                    .getSingleResult();

            return count > 0;
        } finally {
            em.close();
        }
    }

    public void inscrire(Long tournoiId, Long joueurId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Tournoi tournoi = em.find(Tournoi.class, tournoiId);
            Joueur joueur = em.find(Joueur.class, joueurId);

            if (tournoi == null) {
                throw new IllegalArgumentException("Tournoi introuvable.");
            }
            if (joueur == null) {
                throw new IllegalArgumentException("Joueur introuvable.");
            }

            if (tournoi.getEtat() != EtatTournoiEnum.EN_ATTENTE) {
                throw new IllegalStateException("Les inscriptions sont fermées pour ce tournoi.");
            }

            Long dejaInscrit = em.createQuery("""
                    SELECT COUNT(i) FROM InscriptionTournoi i
                    WHERE i.tournoi.id = :tournoiId AND i.joueur.id = :joueurId
                    """, Long.class)
                    .setParameter("tournoiId", tournoiId)
                    .setParameter("joueurId", joueurId)
                    .getSingleResult();

            if (dejaInscrit > 0) {
                throw new IllegalStateException("Ce joueur est déjà inscrit à ce tournoi.");
            }

            Long nbInscrits = em.createQuery("""
                    SELECT COUNT(i) FROM InscriptionTournoi i
                    WHERE i.tournoi.id = :tournoiId
                    """, Long.class)
                    .setParameter("tournoiId", tournoiId)
                    .getSingleResult();

            if (nbInscrits >= tournoi.getNombreJoueursMax()) {
                throw new IllegalStateException("Le tournoi est complet.");
            }

            if (joueur.getRank().ordinal() > tournoi.getRankMaxAutorise().ordinal()) {
                throw new IllegalStateException("Le rank du joueur est trop élevé pour ce tournoi.");
            }

            InscriptionTournoi inscription = new InscriptionTournoi(tournoi, joueur);
            em.persist(inscription);

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

    public void eliminerInscription(Long inscriptionId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            InscriptionTournoi inscription = em.find(InscriptionTournoi.class, inscriptionId);
            if (inscription == null) {
                throw new IllegalArgumentException("Inscription introuvable.");
            }

            inscription.setElimine(true);

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

    public boolean joueurEstElimine(Long tournoiId, Long joueurId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery("""
                    SELECT COUNT(i) FROM InscriptionTournoi i
                    WHERE i.tournoi.id = :tournoiId
                      AND i.joueur.id = :joueurId
                      AND i.elimine = true
                    """, Long.class)
                    .setParameter("tournoiId", tournoiId)
                    .setParameter("joueurId", joueurId)
                    .getSingleResult();

            return count > 0;
        } finally {
            em.close();
        }
    }
}