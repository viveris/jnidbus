/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.message.sendingrequest;

import fr.viveris.jnidbus.serialization.DBusObject;

/**
 * Represent a message waiting to be sent to Dbus
 */
public abstract class AbstractSendingRequest {
    private DBusObject message;

    public AbstractSendingRequest(DBusObject message) {
        this.message = message;
    }

    public DBusObject getMessage() {
        return message;
    }
}
