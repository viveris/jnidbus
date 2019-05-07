package fr.viveris.vizada.jnidbus.message.sendingrequest;

import fr.viveris.vizada.jnidbus.message.Message;

public class SignalSendingRequest extends AbstractSendingRequest{
    private String path;
    private String interfaceName;
    private String member;

    public SignalSendingRequest(Message message, String path, String interfaceName, String member) {
        super(message);
        this.path = path;
        this.interfaceName = interfaceName;
        this.member = member;
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
}
