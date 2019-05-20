package fr.viveris.jnidbus.serialization.cache;

import fr.viveris.jnidbus.serialization.Serializable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * A CachedEntity contains all the data available through reflection that are used for the serialization and unserialization
 * process. This optimizes the process and allows the library to do a lot of checks on the Messages classes without any
 * cost at runtime.
 *
 * The class caches: getters, setters, annotation data and constructor instance for faster instantiation
 */
public class CachedEntity {
    /**
     * Dbus signature string
     */
    private String signature;

    /**
     * Fields anme bound to the Dbus signature
     */
    private String[] fields;

    /**
     * Public constructor
     */
    private Constructor<? extends Serializable> constructor;

    /**
     * Setters and getters, mapped by field name
     */
    private HashMap<String, Method> setters = new HashMap<>();
    private HashMap<String, Method> getters = new HashMap<>();

    public CachedEntity(String signature, String[] fields, Constructor<? extends Serializable> constructor) {
        this.signature = signature;
        this.fields = fields;
        this.constructor = constructor;
    }

    public void addGetter(String fieldName, Method getter){
        this.getters.put(fieldName,getter);
    }

    public void addSetter(String fieldName, Method setter){
        this.setters.put(fieldName,setter);
    }

    public Method getGetter(String fieldName){
        return this.getters.get(fieldName);
    }

    public Method getSetter(String fieldName){
        return this.setters.get(fieldName);
    }

    public String getSignature() {
        return signature;
    }

    public String[] getFields() {
        return fields;
    }

    /**
     * Create a new empty instances of the Message object (for unserialization).
     *
     * @return
     */
    public Serializable newInstance(){
        try {
            return this.constructor.newInstance();
        } catch (Exception e) {
           throw new IllegalStateException("Could not create instance: "+e.toString(),e);
        }
    }
}
