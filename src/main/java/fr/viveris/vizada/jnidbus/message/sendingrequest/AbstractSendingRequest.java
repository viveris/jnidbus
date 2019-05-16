package fr.viveris.vizada.jnidbus.message.sendingrequest;

import fr.viveris.vizada.jnidbus.serialization.DBusObject;

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
