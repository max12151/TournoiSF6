package be.technifutur.tournoisf6.service;

import be.technifutur.tournoisf6.models.Tournoi;
import be.technifutur.tournoisf6.models.enums.EtatTournoiEnum;
import be.technifutur.tournoisf6.utils.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TournoiService {

    public List<Tournoi> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM Tournoi t ORDER BY t.dateDebut ASC", Tournoi.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Tournoi findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Tournoi.class, id);
        } finally {
            em.close();
        }
    }

    public void save(Tournoi tournoi) {
        if (tournoi == null) {
            throw new IllegalArgumentException("Le tournoi est obligatoire.");
        }
        if (tournoi.getNom() == null || tournoi.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du tournoi est obligatoire.");
        }
        if (tournoi.getDateDebut() == null || tournoi.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates du tournoi sont obligatoires.");
        }
        if (!tournoi.isDateCoherente()) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        if (tournoi.getNombreJoueursMax() == null || tournoi.getNombreJoueursMax() < 2) {
            throw new IllegalArgumentException("Le nombre maximum de joueurs doit être au moins 2.");
        }
        if (tournoi.getRankMaxAutorise() == null) {
            throw new IllegalArgumentException("Le rank maximum autorisé est obligatoire.");
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tournoi);
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

    public Tournoi update(Tournoi tournoi) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Tournoi merged = em.merge(tournoi);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void passerEnCours(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Tournoi tournoi = em.find(Tournoi.class, tournoiId);
            if (tournoi == null) {
                throw new IllegalArgumentException("Tournoi introuvable.");
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

    public void terminer(Long tournoiId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Tournoi tournoi = em.find(Tournoi.class, tournoiId);
            if (tournoi == null) {
                throw new IllegalArgumentException("Tournoi introuvable.");
            }

            tournoi.setEtat(EtatTournoiEnum.TERMINE);

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
}