package fr.viveris.vizada.jnidbus.message.sendingrequest;

/**
 * Represent an error to be sent in reply to a call
 */
public class ErrorReplySendingRequest extends AbstractSendingRequest {
    private Throwable error;
    private long messagePointer;

    public ErrorReplySendingRequest(Throwable error, long messagePointer) {
        super(null);
        this.error = error;
        this.messagePointer = messagePointer;
    }

    public Throwable getError() {
        return error;
    }

    public long getMessagePointer() {
        return messagePointer;
    }
}
