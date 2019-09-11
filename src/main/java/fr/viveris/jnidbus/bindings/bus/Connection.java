/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.bindings.bus;

import fr.viveris.jnidbus.BusType;
import fr.viveris.jnidbus.exception.ConnectionException;

/**
 * Represent a DBus connection and is responsible for the management of the native context pointer. This class doesn't do much beside
 * creating and destroying DBus connections/contexts
 */
public class Connection {

    /**
     * Native pointer to the native context. This value most not be modified by Java code.
     */
    private long dBusContextPointer;

    /**
     * Bus name owned by this connection
     */
    private String busName;

    /**
     * JNI calls ignore any type of restriction, so we can make the constructor private for the java developer and let the native code
     * execute it
     * @param dBusContextPointer given by the native code
     */
    private Connection(long dBusContextPointer, String busName){
        this.dBusContextPointer = dBusContextPointer;
        this.busName = busName;
    }

    /**
     * Create a DBus connection on the desired bus.
     * @param type type of bus to connect to
     * @param busName bus name we want to own
     * @param busAddress address the dbus-daemon is listening, libdbus will default to the DBUS_SESSION_BUS_ADDRESS
     *                   environment variable of busAddress is null
     * @return connected bus
     * @throws ConnectionException thrown when libdbus could not create the connection
     */
    public static native Connection createConnection(BusType type, String busName, String busAddress) throws ConnectionException;

    /**
     * Closes the native context and DBus connection freeing all memory and releasing the registered bus name
     * @param contextPtr
     */
    private native void closeNative(long contextPtr);

    public String getBusName() {
        return busName;
    }

    /**
     * Get the pointer to the native context, the only class that should use this are the native one contained in the bindings.bus package
     * so we make this method package private
     * @return
     */
    long getDbusContextPointer() {
        return dBusContextPointer;
    }

    /**
     * This method is package private because only the event loop can decide to close the connection when its closed
     */
    void close() {
        this.closeNative(this.dBusContextPointer);
    }
}
