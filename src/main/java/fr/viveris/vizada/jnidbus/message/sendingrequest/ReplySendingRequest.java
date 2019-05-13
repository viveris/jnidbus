package fr.viveris.vizada.jnidbus.message.sendingrequest;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusObject;

public class ReplySendingRequest extends AbstractSendingRequest {
    long messagePointer;

    public ReplySendingRequest(DBusObject message, long messagePointer) {
        super(message);
        this.messagePointer = messagePointer;
    }

    public long getMessagePointer() {
        return messagePointer;
    }
}
