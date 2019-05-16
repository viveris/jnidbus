package fr.viveris.vizada.jnidbus.exception;

/**
 * thrown when a message is being checked and something is wrong
 */
public class MessageCheckException extends Exception{
    public MessageCheckException(String message) {
        super(message);
    }
}
