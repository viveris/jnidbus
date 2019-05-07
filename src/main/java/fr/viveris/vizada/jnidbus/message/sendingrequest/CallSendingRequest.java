package fr.viveris.vizada.jnidbus.message.sendingrequest;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.message.PendingCall;

public class CallSendingRequest extends AbstractSendingRequest {
    private String path;
    private String interfaceName;
    private String member;
    private String dest;
    private PendingCall pendingCall;

    public CallSendingRequest(Message message, String path, String interfaceName, String member, String dest, PendingCall pendingCall) {
        super(message);
        this.path = path;
        this.interfaceName = interfaceName;
        this.member = member;
        this.dest = dest;
        this.pendingCall = pendingCall;
    }

    public String getPath() {
        return path;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMember() {
        return member;
    }

    public String getDest() {
        return dest;
    }

    public PendingCall getPendingCall() {
        return pendingCall;
    }
}
