package fr.viveris.jnidbus.exception;

/**
 * Thrown when an error occurs during event loop setup phase
 */
public class EventLoopSetupException extends Exception {
    public EventLoopSetupException(String message) {
        super(message);
    }
}
