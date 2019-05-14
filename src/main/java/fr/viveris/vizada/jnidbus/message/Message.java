package fr.viveris.vizada.jnidbus.message;

import fr.viveris.vizada.jnidbus.exception.MessageSignatureMismatch;
import fr.viveris.vizada.jnidbus.serialization.*;
import fr.viveris.vizada.jnidbus.serialization.signature.Signature;
import fr.viveris.vizada.jnidbus.serialization.signature.SignatureElement;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class Message implements Serializable {
    public static final EmptyMessage EMPTY = new EmptyMessage();


    @Override
    public DBusObject serialize() {
        DBusType type = this.getClass().getAnnotation(DBusType.class);
        Class<? extends Message> clazz = this.getClass();

        if(type == null) throw new IllegalStateException("No DBusType annotation found");

        //set the array length at the number of field and iterate on every char of the signature
        Object[] values = new Object[type.fields().length];
        int i = 0;
        for(SignatureElement element : new Signature(type.signature())){
            //generate getter name
            String getterName = "get" + Character.toUpperCase(type.fields()[i].charAt(0)) + type.fields()[i].substring(1);

            try{
                Method getter = clazz.getDeclaredMethod(getterName);
                //primitive types don't need any kind of processing beside casting
                if(element.isPrimitive()){
                    values[i] = getter.invoke(this);
                }else if(element.isArray()){
                    //check what the contained type is, if the type is primitive (ie. not a struct), serialize tight now, else iterate
                    Object value = getter.invoke(this);

                    if(value instanceof List){
                        List collection = (List)value;
                        values[i] = Message.serializeList(collection,element,((ParameterizedType)getter.getGenericReturnType()).getActualTypeArguments()[0]);
                    }else {
                        throw new IllegalArgumentException("The signature returned by " + getterName + " is not a List");
                    }

                }else if(element.isObject()){
                    values[i] = ((Serializable)getter.invoke(this)).serialize();
                }else{
                    throw new IllegalStateException("Unknown type detected: "+element.toString());
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization "+e.toString(),e);
            }
            i++;
        }

        return new DBusObject(type.signature(),values);
    }

    @Override
    public void unserialize(DBusObject obj) throws MessageSignatureMismatch {
        DBusType type = this.getClass().getAnnotation(DBusType.class);
        Class<? extends Message> clazz = this.getClass();

        if(type == null) throw new IllegalStateException("No DBusType annotation found");
        if(!type.signature().equals(obj.getSignature())) throw new MessageSignatureMismatch("Signature mismatch, expected "+type.signature()+" but got "+obj.getSignature());

        Object[] values = obj.getValues();
        int i = 0;
        for(SignatureElement element : new Signature(type.signature())){
            String getterName = "set" + Character.toUpperCase(type.fields()[i].charAt(0)) + type.fields()[i].substring(1);

            try{
                Method setter = null;
                if(element.isPrimitive()){
                    setter = clazz.getDeclaredMethod(getterName,clazz.getDeclaredField(type.fields()[i]).getType());
                    setter.invoke(this,values[i]);
                }else if(element.isArray()){
                    try{
                        setter = clazz.getDeclaredMethod(getterName,List.class);
                        setter.invoke(this,Message.unserializeList((Object[])values[i],element,((ParameterizedType)setter.getGenericParameterTypes()[0]).getActualTypeArguments()[0]));
                    }catch (NoSuchMethodException e){
                        throw new IllegalStateException("Unknown list getter "+getterName);
                    }
                }else if(element.isObject()){
                    Class<?> objClazz = clazz.getDeclaredField(type.fields()[i]).getType();
                    if(!Serializable.class.isAssignableFrom(objClazz)) throw new IllegalStateException("The setter "+setter.getName()+" takes a non-serializable type as parameter");
                    Serializable unserialized = objClazz.asSubclass(Serializable.class).newInstance();
                    unserialized.unserialize((DBusObject)values[i]);
                    clazz.getDeclaredMethod(getterName,objClazz).invoke(this,unserialized);

                }else{
                    throw new IllegalStateException("Unknown type detected: "+element);
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization",e);
            }
            i++;
        }
    }

    private static Object[] serializeList(List collection, SignatureElement signature, Type genericType) {
        if(signature.getPrimitive() != null) return collection.toArray();
        else{
            Object[] serialized = new Object[collection.size()];
            int i = 0;
            boolean isRecusiveList = genericType instanceof ParameterizedType;
            for(Object o : collection){
                if(isRecusiveList){
                    serialized[i++] = Message.serializeList((List)o,signature.getSignature().getFirst(),((ParameterizedType)genericType).getActualTypeArguments()[0]);
                }else{
                    if(!Serializable.class.isAssignableFrom((Class)genericType)) throw new IllegalStateException("The List contains a non-serializable type");
                    serialized[i++] = ((Serializable)o).serialize();
                }
            }
            return serialized;
        }
    }

    private static List unserializeList(Object[] values , SignatureElement signature, Type genericType) throws IllegalAccessException, InstantiationException, MessageSignatureMismatch {
        if(signature.getPrimitive() != null){
            if(values == null || values.length == 0) return Collections.emptyList();
            else return Arrays.asList(values);
        }else{
            boolean isRecusiveList = genericType instanceof ParameterizedType;
            List<Object> list = new ArrayList<>();
            if(values != null){
                for(Object o : values){
                    if(isRecusiveList){
                        list.add(Message.unserializeList((Object[]) o,signature.getSignature().getFirst(),((ParameterizedType)genericType).getActualTypeArguments()[0]));
                    }else{
                        if(!(o instanceof DBusObject)) throw new IllegalStateException("The values are not unserializable objects");
                        Serializable obj = ((Class<? extends Serializable>)genericType).newInstance();
                        //generate DBusObject from raw data and signature element object
                        obj.unserialize((DBusObject)o);
                        list.add(obj);
                    }
                }
            }
            return list;
        }
    }

    @DBusType(
            signature = "",
            fields = ""
    )
    public static class EmptyMessage extends Message{
        private EmptyMessage(){}
    }
}
