/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.exception;

/**
 * thrown when a message is being checked and something is wrong
 */
public class MessageCheckException extends Exception{
    public MessageCheckException(String message) {
        super(message);
    }
}
