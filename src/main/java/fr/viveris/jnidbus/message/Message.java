/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.message;

import fr.viveris.jnidbus.cache.Cache;
import fr.viveris.jnidbus.cache.MessageMetadata;
import fr.viveris.jnidbus.exception.MessageSignatureMismatchException;
import fr.viveris.jnidbus.serialization.DBusObject;
import fr.viveris.jnidbus.serialization.DBusType;
import fr.viveris.jnidbus.serialization.Serializable;
import fr.viveris.jnidbus.serialization.signature.Signature;
import fr.viveris.jnidbus.serialization.signature.SignatureElement;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represent anything that can be sent to dbus. This class implements serialization and unserialization methods that
 * will use reflection and the DBusType annotation to know how to perform the serialization. As the reflection is slow
 * we use a cache to store information that will not change between serializations.
 *
 * An extending class serializable fields should only be:
 *      -A string
 *      -A primitive (or boxed type)
 *      -A List, Nested lists and objects are supported
 *      -Another Message class
 */
public abstract class Message implements Serializable {
    /**
     * Special message type which has an empty signature. As message of this type can often happen, we can use this
     * static instance instead of creating your own
     */
    public static final EmptyMessage EMPTY = new EmptyMessage();

    /**
     * Cache containing the reflection data. The map use a ClassLoader as a key in order to support same name classes loaded
     * by different class loaders. In addition this map is weak so an unused class loader can be freed without issues
     * (hot reload of classes for example)
     */
    private static final Cache<Class<? extends Serializable>, MessageMetadata> CACHE = new Cache<>();


    @Override
    public DBusObject serialize() {
        //retrieve reflection data from the cache
        Class<? extends Message> clazz = this.getClass();
        MessageMetadata messageMetadata = Message.retrieveFromCache(clazz);

        //set the array length at the number of field and iterate on the signature
        Object[] values = new Object[messageMetadata.getFields().length];
        int i = 0;
        for(SignatureElement element : new Signature(messageMetadata.getSignature())){
            //get the field name for the current signature element
            String fieldName = messageMetadata.getFields()[i];

            try{
                //retrieve the getter from the cache
                Method getter = messageMetadata.getGetter(fieldName);
                Object returnValue = getter.invoke(this);
                if(returnValue == null) throw new NullPointerException("A DBus value can not be nullable");

                if(element.isPrimitive()){
                    //primitive types don't need any kind of processing
                    values[i] = returnValue;
                }else if(element.isArray()){
                    //we can directly cas as List as the cached entity checked this when created then serialize it
                    List value = (List) returnValue;
                    values[i] = Message.serializeList(value,element,((ParameterizedType)getter.getGenericReturnType()).getActualTypeArguments()[0]);

                }else if(element.isObject()){
                    //recursively serialize
                    values[i] = ((Serializable)returnValue).serialize();
                }else{
                    throw new IllegalStateException("Unknown type detected: "+element.toString());
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization "+e.toString(),e);
            }
            //go to the next field
            i++;
        }

        return new DBusObject(messageMetadata.getSignature(),values);
    }

    @Override
    public void unserialize(DBusObject obj) throws MessageSignatureMismatchException {
        //retrieve reflection data from the cache
        Class<? extends Message> clazz = this.getClass();
        MessageMetadata messageMetadata = Message.retrieveFromCache(clazz);

        //check if the given pre-unserialized object have the same signature as this class
        if(!messageMetadata.getSignature().equals(obj.getSignature())) throw new MessageSignatureMismatchException("Signature mismatch, expected "+ messageMetadata.getSignature()+" but got "+obj.getSignature());

        //get the value sand iterate through the signature
        Object[] values = obj.getValues();
        int i = 0;
        for(SignatureElement element : new Signature(messageMetadata.getSignature())){
            //get the field name for the current signature element
            String fieldName = messageMetadata.getFields()[i];

            try{
                //retrieve the setter from the cache
                Method setter = messageMetadata.getSetter(fieldName);

                if(element.isPrimitive()){
                    //primitive types do not need any processing
                    setter.invoke(this,values[i]);
                }else if(element.isArray()){
                    setter.invoke(this,Message.unserializeList((Object[])values[i],element,((ParameterizedType)setter.getGenericParameterTypes()[0]).getActualTypeArguments()[0]));
                }else if(element.isObject()){
                    //retrieve the needed object class from cache and create a new instance
                    Serializable unserialized = Message.retrieveFromCache(setter.getParameterTypes()[0].asSubclass(Serializable.class)).newInstance();
                    //recursively unserialize
                    unserialized.unserialize(new DBusObject(element.getSignatureString(),((DBusObject)values[i]).getValues()));
                    setter.invoke(this,unserialized);

                }else{
                    throw new IllegalStateException("Unknown type detected: "+element);
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization",e);
            }
            //go to the next value
            i++;
        }
    }

    /**
     * Takes a List and serialize it into a raw Object array. This method supports nested list serialization
     *
     * @param collection List to serialize
     * @param signature signature of the list, used to determine if the list contains primitive types
     * @param genericType type of the given List items, used to determine if the list contains other lists
     * @return the list serialized as a raw object array
     */
    private static Object[] serializeList(List collection, SignatureElement signature, Type genericType) {
        //if the list is primitive, transform it into an array and return
        if(signature.getPrimitive() != null) return collection.toArray();
        else{
            Object[] serialized = new Object[collection.size()];
            int i = 0;
            boolean isRecusiveList = genericType instanceof ParameterizedType;
            for(Object o : collection){
                if(isRecusiveList){
                    //if the list contains other lists, recursively serialize it
                    serialized[i++] = Message.serializeList((List)o,signature.getSignature().getFirst(),((ParameterizedType)genericType).getActualTypeArguments()[0]);
                }else{
                    //else serialize the object
                    serialized[i++] = ((Serializable)o).serialize();
                }
            }
            return serialized;
        }
    }

    /**
     * Transform the given raw Object array into a List. This method supports nested arrays
     *
     * @param values
     * @param signature
     * @param genericType
     * @return
     * @throws MessageSignatureMismatchException
     */
    private static List unserializeList(Object[] values , SignatureElement signature, Type genericType) throws MessageSignatureMismatchException {
        //if the array si primitive, transform it in a list
        if(signature.getPrimitive() != null){
            //when the list is empty or null, the asList() method consider it to be an element of an array and not the array, the next line fixes this behavior
            if(values == null || values.length == 0) return Collections.emptyList();
            else return Arrays.asList(values);
        }else{
            boolean isRecusiveList = genericType instanceof ParameterizedType;
            List<Object> list = new ArrayList<>();
            String elementSignatureString = signature.getSignature().getFirst().getSignatureString();

            //iterate through the values an unserialize
            if(values != null){
                for(Object o : values){
                    if(isRecusiveList){
                        list.add(Message.unserializeList((Object[]) o,signature.getSignature().getFirst(),((ParameterizedType)genericType).getActualTypeArguments()[0]));
                    }else{
                        Serializable obj = Message.retrieveFromCache((Class<? extends Serializable>)genericType).newInstance();
                        //generate DBusObject from raw data and signature element object
                        obj.unserialize(new DBusObject(elementSignatureString,((DBusObject)o).getValues()));
                        list.add(obj);
                    }
                }
            }
            return list;
        }
    }

    /**
     * Try to retrieve the cached metadata for the given class, if the cache entity does not exists, check the class,
     * try to create one and return it. If the class is invalid, it will throw
     *
     * @param clazz class to retrieve
     * @return the cached entity
     */
    public static MessageMetadata retrieveFromCache(Class<? extends Serializable> clazz){
        //if the cache is null, make the entity null, the cache will be created when the clazz is processed
        MessageMetadata meta = CACHE.getCachedEntity(clazz);
        if(meta != null){
            return meta;
        }

        try {
            meta = new MessageMetadata(clazz);
            Message.addToCache(clazz,meta);
            return meta;
        } catch (Exception e) {
            throw new IllegalStateException("Message validity check failed: " + e, e);
        }
    }

    public static void addToCache(Class<? extends Serializable> clazz, MessageMetadata meta){
        CACHE.addCachedEntity(clazz, meta);
    }

    public static void clearCache(){
        CACHE.clear();
    }

    /**
     * Special type of message that do not need serialization or unserialization.
     */
    @DBusType(
            signature = "",
            fields = ""
    )
    public static class EmptyMessage extends Message{
        public EmptyMessage(){}
    }
}
