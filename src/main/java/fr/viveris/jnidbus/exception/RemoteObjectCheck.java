package fr.viveris.jnidbus.exception;

/**
 * thrown when a message is being checked and something is wrong
 */
public class RemoteObjectCheck extends Exception{
    public RemoteObjectCheck(String message) {
        super(message);
    }
}
