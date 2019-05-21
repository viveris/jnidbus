package fr.viveris.jnidbus.cache;

import fr.viveris.jnidbus.remote.RemoteInterface;
import fr.viveris.jnidbus.remote.RemoteMember;
import fr.viveris.jnidbus.remote.Signal;

public class SignalMetadata {
    private String interfaceName;
    private String member;

    /**
     * Create a new cache entry for the given signal class. The class will be checked and an exception will be thrown if
     * an error was found
     * @param signal
     */
    public SignalMetadata(Class<? extends Signal> signal){
        Class<?> enclosing = signal.getEnclosingClass();
        if(enclosing == null) throw new IllegalArgumentException("The signal class must be enclosed by an interface annotated with RemoteInterface");

        RemoteInterface remoteInterface = enclosing.getAnnotation(RemoteInterface.class);
        if(remoteInterface == null) throw new IllegalArgumentException("The enclosing interface must be annotated with RemoteInterface");

        RemoteMember remoteMember = signal.getAnnotation(RemoteMember.class);
        if(remoteMember == null) throw new IllegalArgumentException("The signal class must be annotated with RemoteMember");

        this.interfaceName = remoteInterface.value();
        this.member = remoteMember.value();
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMember() {
        return member;
    }
}
