package be.technifutur.tournoisf6.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("tournoiSF6PU");

    private JpaUtil() {
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}