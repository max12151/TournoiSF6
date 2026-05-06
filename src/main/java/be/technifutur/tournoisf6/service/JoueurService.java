package be.technifutur.tournoisf6.service;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.utils.JpaUtil;
import be.technifutur.tournoisf6.utils.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

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
                            Long.class)
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
                            Long.class)
                    .setParameter("pseudo", pseudo)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }


    public void save(Joueur joueur) {
        String mdp = joueur.getMotDePasse();
        if (mdp != null && !mdp.startsWith("$2a$") && !mdp.startsWith("$2b$")) {
            joueur.setMotDePasse(PasswordUtil.hashPassword(mdp));
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(joueur);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Joueur findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT j FROM Joueur j WHERE LOWER(j.email) = LOWER(:email)",
                            Joueur.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Joueur authentifier(String email, String motDePasse) {
        Joueur joueur = findByEmail(email);
        if (joueur == null) return null;
        if (!PasswordUtil.checkPassword(motDePasse, joueur.getMotDePasse())) return null;
        return joueur;
    }
}