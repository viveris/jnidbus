package fr.viveris.vizada.jnidbus.serialization;

public interface Serializable {
    DBusObject serialize();
    void unserialize(DBusObject obj) ;
}
