package fr.viveris.vizada.jnidbus.message.sendingrequest;

import fr.viveris.vizada.jnidbus.serialization.DBusObject;

public abstract class AbstractSendingRequest {
    private DBusObject message;

    public AbstractSendingRequest(DBusObject message) {
        this.message = message;
    }

    public DBusObject getMessage() {
        return message;
    }
}
