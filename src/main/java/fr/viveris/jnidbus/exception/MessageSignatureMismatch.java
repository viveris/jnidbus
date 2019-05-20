package fr.viveris.jnidbus.exception;

/**
 * Thrown by a Message which could not be unserialized due to an input DBusObject having a different signature than the
 * one of the Message being unserialized
 */
public class MessageSignatureMismatch extends Exception {
    public MessageSignatureMismatch(String message) {
        super(message);
    }
}
