/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.exception;

/**
 * Thrown when calling a method on a closed event loop
 */
public class ClosedEventLoopException extends Error {
    public ClosedEventLoopException(String message) {
        super(message);
    }
}
