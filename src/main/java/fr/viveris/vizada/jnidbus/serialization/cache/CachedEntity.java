package fr.viveris.vizada.jnidbus.serialization.cache;

import fr.viveris.vizada.jnidbus.message.Message;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

public class CachedEntity {
    private String signature;
    private String[] fields;
    private Constructor<? extends Message> constructor;
    private HashMap<String, Method> setters = new HashMap<>();
    private HashMap<String, Method> getters = new HashMap<>();

    public CachedEntity(String signature, String[] fields, Constructor<? extends Message> constructor) {
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

    public Message newInstance(){
        try {
            return this.constructor.newInstance();
        } catch (Exception e) {
           throw new IllegalStateException("Could not create instance: "+e.toString(),e);
        }
    }
}
