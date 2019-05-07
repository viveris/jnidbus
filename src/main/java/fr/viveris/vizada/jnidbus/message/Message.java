package fr.viveris.vizada.jnidbus.message;

import fr.viveris.vizada.jnidbus.serialization.DBusObject;
import fr.viveris.vizada.jnidbus.serialization.DBusType;
import fr.viveris.vizada.jnidbus.serialization.Serializable;
import fr.viveris.vizada.jnidbus.serialization.SupportedTypes;

import java.lang.reflect.Method;

public abstract class Message implements Serializable {
    public static final Message EMPTY = new EmptyMessage();


    @Override
    public DBusObject serialize() {
        DBusType type = this.getClass().getAnnotation(DBusType.class);
        Class<? extends Message> clazz = this.getClass();

        if(type == null) throw new IllegalStateException("No DBusType annotation found");

        //get values
        Object[] values = new Object[type.fields().length];
        for (int i = 0; i < type.value().length(); i++){
            char c = type.value().charAt(i);
            String getterName = "get" + Character.toUpperCase(type.fields()[i].charAt(0)) + type.fields()[i].substring(1);

            try{
                Method getter = clazz.getDeclaredMethod(getterName);
                if(c == SupportedTypes.STRING.getValue()){
                    values[i] = (String) getter.invoke(this);
                }else if(c == SupportedTypes.INTEGER.getValue()){
                    values[i] = (Integer) getter.invoke(this);
                }else{
                    throw new IllegalStateException("Unknown type detected: "+c);
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization",e);
            }
        }

        return new DBusObject(type.value(),values);
    }

    @Override
    public void unserialize(DBusObject obj) {
        DBusType type = this.getClass().getAnnotation(DBusType.class);
        Class<? extends Message> clazz = this.getClass();

        if(type == null) throw new IllegalStateException("No DBusType annotation found");
        if(!type.value().equals(obj.getSignature())) throw new IllegalArgumentException("Signature mismatch, unserialization impossible");

        for (int i = 0; i < type.value().length(); i++){
            char c = type.value().charAt(i);
            String getterName = "set" + Character.toUpperCase(type.fields()[i].charAt(0)) + type.fields()[i].substring(1);

            try{
                Method setter;
                if(c == SupportedTypes.STRING.getValue()){
                    setter = clazz.getDeclaredMethod(getterName,String.class);
                    setter.invoke(this,(String) obj.getValues()[i]);
                }else if(c == SupportedTypes.INTEGER.getValue()){
                    setter = clazz.getDeclaredMethod(getterName,Integer.class);
                    setter.invoke(this,(Integer) obj.getValues()[i]);
                }else{
                    throw new IllegalStateException("Unknown type detected: "+c);
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization",e);
            }
        }
    }

    @DBusType(
            value = "",
            fields = ""
    )
    public static class EmptyMessage extends Message{
        private EmptyMessage(){}
    }
}
