package be.technifutur.tournoisf6.exception;

/**
 * Exception runtime pour les erreurs de programmation (état incohérent,
 * données introuvables). L'appelant n'est pas obligé de la catcher.
 */
public class TournoiRuntimeException extends RuntimeException {

    public TournoiRuntimeException(String message) {
        super(message);
    }

    public TournoiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}