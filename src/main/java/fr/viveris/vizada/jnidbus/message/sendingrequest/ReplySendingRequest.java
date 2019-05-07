package fr.viveris.vizada.jnidbus.message.sendingrequest;

import fr.viveris.vizada.jnidbus.message.Message;

public class ReplySendingRequest extends AbstractSendingRequest {
    long messagePointer;

    public ReplySendingRequest(Message message, long messagePointer) {
        super(message);
        this.messagePointer = messagePointer;
    }

    public long getMessagePointer() {
        return messagePointer;
    }
}
