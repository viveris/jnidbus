package fr.viveris.jnidbus.message;

import fr.viveris.jnidbus.serialization.DBusObject;

/**
 * Abstract class any signal class should extend. A signal is a representation of a DBus signal which can be instantiated
 * and given to the DBus class to be sent. Its generic type is the signature of the signal and should be valid Message
 * class.
 *
 * Any Signal class should be annotated with the DbusSignal annotation which provides all the information needed for dbus
 * to send the signal
 *
 * All its methods can be overridden in order to replace the information provided by the annotation (for dynamic signals or
 * testing purposes for example)
 *
 * @param <In> type of the signal data
 */
public abstract class Signal<In extends Message> {
    private DbusSignal annotation;
    private In params;

    public Signal(In params){
        this.annotation = this.getClass().getAnnotation(DbusSignal.class);
        if(this.annotation== null) throw new IllegalStateException("A DBus method call must be annotated with DbusMethodCall");
        this.params = params;
    }

    public String getPath(){
        return this.annotation.path();
    }

    public String getInterfaceName(){
        return this.annotation.interfaceName();
    }

    public String getMember(){
        return this.annotation.member();
    }

    public DBusObject getParams() {
        return params.serialize();
    }
}
