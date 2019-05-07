package fr.viveris.vizada.jnidbus.bindings.bus.sendingrequest;

import fr.viveris.vizada.jnidbus.message.Message;

public abstract class AbstractSendingRequest {
    private Message message;

    public AbstractSendingRequest(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
