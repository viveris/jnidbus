package fr.viveris.vizada.jnidbus.message;

import fr.viveris.vizada.jnidbus.serialization.DBusObject;

/**
 * Abstract class ny call should extend. A call is a representation of a DBus call which can be instanciated and given to
 * the DBus class to be sent over to dbus. Its generic types are the signature of the call and should be valid Messages
 * classes.
 *
 * Any Call class should be annotated with the DbusMethodCall annotation which provides all the information needed for dbus
 * to send the call to the right bus
 *
 * All its methods can be overridden in order to replace the information provided by the annotation (for dynamic call or
 * testing purposes for example)
 *
 * @param <In> input type of the call
 * @param <Out> return type of the call
 */
public abstract class Call<In extends Message,Out extends Message> {
    private DbusMethodCall annotation;
    private Class<Out> returnType;
    private In params;

    public Call(In params, Class<Out> returnType){
        this.annotation = this.getClass().getAnnotation(DbusMethodCall.class);
        if(this.annotation== null) throw new IllegalStateException("A DBus method call must be annotated with DbusMethodCall");
        this.returnType = returnType;
        this.params = params;
    }

    public String getDestination(){
        return this.annotation.destination();
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

    public Class<Out> getReturnType() {
        return returnType;
    }

    public DBusObject getParams() {
        return params.serialize();
    }
}
