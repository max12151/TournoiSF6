package be.technifutur.tournoisf6.service;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.utils.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JoueurService {

    public List<Joueur> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT j FROM Joueur j ORDER BY j.id", Joueur.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean emailExiste(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(j) FROM Joueur j WHERE LOWER(j.email) = LOWER(:email)",
                            Long.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean pseudoExiste(String pseudo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(j) FROM Joueur j WHERE LOWER(j.pseudo) = LOWER(:pseudo)",
                            Long.class
                    )
                    .setParameter("pseudo", pseudo)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public void save(Joueur joueur) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(joueur);
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