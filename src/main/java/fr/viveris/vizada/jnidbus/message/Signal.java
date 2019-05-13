package fr.viveris.vizada.jnidbus.message;

import fr.viveris.vizada.jnidbus.serialization.DBusObject;

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
