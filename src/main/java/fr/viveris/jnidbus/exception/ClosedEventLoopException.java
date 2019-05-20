package fr.viveris.jnidbus.exception;

/**
 * Thrown when calling a method on a closed event loop
 */
public class ClosedEventLoopException extends Error {
    public ClosedEventLoopException(String message) {
        super(message);
    }
}
