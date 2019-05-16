package fr.viveris.vizada.jnidbus.exception;

/**
 * Thrown when an error occurs while creating or destroying the DBus connection
 */
public class ConnectionException extends Exception {
    public ConnectionException(String message) {
        super(message);
    }
}
