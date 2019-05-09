package fr.viveris.vizada.jnidbus.bindings.bus;

import fr.viveris.vizada.jnidbus.BusType;
import fr.viveris.vizada.jnidbus.exception.ConnectionException;

public class Connection {

    private long dBusContextPointer;

    /**
     * Bus name owned bu this connection
     */
    private String busName;

    /**
     * JNI calls ignore any type of restriction, so we can make the constructor private for the java developer
     * @param dBusContextPointer
     */
    private Connection(long dBusContextPointer, String busName){
        this.dBusContextPointer = dBusContextPointer;
        this.busName = busName;
    }

    /**
     * Create a DBus connection on the desired bus
     * @param type
     * @return
     */
    public static native Connection createConnection(BusType type, String busName) throws ConnectionException;

    private native void closeNative(long contextPtr);

    public long getdBusContextPointer() {
        return dBusContextPointer;
    }

    void close() {
        this.closeNative(this.dBusContextPointer);
    }
}
