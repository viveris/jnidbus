package fr.viveris.vizada.jnidbus.serialization;

import fr.viveris.vizada.jnidbus.exception.MessageSignatureMismatch;

/**
 * Define the two methods used for the serialization process.
 */
public interface Serializable {
    /**
     * Transform the current instance into a pre-serialized representation usable by the JNI code
     * @return pre-serialized object
     */
    DBusObject serialize();

    /**
     * Transfer the pre-serialized object into the current instance. If the parameter do not have the same signature as
     * current instance we want to populate, an exception will be thrown.
     *
     * @param obj values to transfer
     * @throws MessageSignatureMismatch thrown if the param does not contain the right signature for this instance
     */
    void unserialize(DBusObject obj) throws MessageSignatureMismatch;
}
