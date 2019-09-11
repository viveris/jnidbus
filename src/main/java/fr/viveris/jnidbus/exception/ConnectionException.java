/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.exception;

/**
 * Thrown when an error occurs while creating or destroying the DBus connection
 */
public class ConnectionException extends Exception {
    public ConnectionException(String message) {
        super(message);
    }
}
