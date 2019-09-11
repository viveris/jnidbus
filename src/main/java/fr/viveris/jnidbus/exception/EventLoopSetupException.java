/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.exception;

/**
 * Thrown when an error occurs during event loop setup phase
 */
public class EventLoopSetupException extends Exception {
    public EventLoopSetupException(String message) {
        super(message);
    }
}
