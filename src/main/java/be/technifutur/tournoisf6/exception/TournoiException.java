package be.technifutur.tournoisf6.exception;

/**
 * Exception métier checked pour les erreurs de règles tournoi.
 * Utilisée quand l'erreur doit être gérée obligatoirement par l'appelant.
 */
public class TournoiException extends Exception {

    public TournoiException(String message) {
        super(message);
    }

    public TournoiException(String message, Throwable cause) {
        super(message, cause);
    }
}