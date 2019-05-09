package fr.viveris.vizada.jnidbus.serialization;

import fr.viveris.vizada.jnidbus.exception.MessageSignatureMismatch;

public interface Serializable {
    DBusObject serialize();
    void unserialize(DBusObject obj) throws MessageSignatureMismatch;
}
