package fr.viveris.jnidbus.exception;

/**
 * Thrown by a Message which could not be unserialized due to an input DBusObject having a different signature than the
 * one of the Message being unserialized
 */
public class MessageSignatureMismatchException extends Exception {
    public MessageSignatureMismatchException(String message) {
        super(message);
    }
}
