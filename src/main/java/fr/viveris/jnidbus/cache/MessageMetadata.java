/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.cache;

import fr.viveris.jnidbus.exception.MessageCheckException;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.serialization.DBusType;
import fr.viveris.jnidbus.serialization.Serializable;
import fr.viveris.jnidbus.serialization.signature.Signature;
import fr.viveris.jnidbus.serialization.signature.SignatureElement;
import fr.viveris.jnidbus.serialization.signature.SupportedTypes;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;

/**
 * A MessageMetadata contains all the data available through reflection that are used for the serialization and unserialization
 * process. This optimizes the process and allows the library to do a lot of checks on the Messages classes without any
 * cost at runtime.
 *
 * The class caches: getters, setters, annotation data and constructor instance for faster instantiation
 */
public class MessageMetadata {
    /**
     * Dbus signature string
     */
    private String signature;

    /**
     * Fields name bound to the Dbus signature
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

    /**
     * Message class object
     */
    private Class<? extends Serializable> clazz;

    /**
     * Create a new cache entry for the given message class. The class will be checked and an exception will be thrown if
     * an error was found. If the class contains other serializable classes, it will recursively check those as well and
     * cache them.
     *
     * @param clazz class to analyze
     * @throws MessageCheckException thrown when the class is not a valid serializable class
     */
    public MessageMetadata(Class<? extends Serializable> clazz) throws MessageCheckException {
        this.clazz = clazz;

        //check annotation
        DBusType type = clazz.getAnnotation(DBusType.class);
        if(type == null) throw new IllegalStateException("No DBusType annotation found");

        //check constructor
        Constructor<? extends Serializable> constructor;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No public empty constructor found");
        }

        //create a new cache entity
        this.signature = type.signature();
        this.fields = type.fields();
        this.constructor = constructor;

        //generate getter and setter list
        int i = 0;
        for(SignatureElement element : new Signature(type.signature())){
            String fieldName = type.fields()[i];
            //generate setter and getter name
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

            Field field;
            try{
                field = clazz.getDeclaredField(fieldName);
            }catch (Exception e){
                throw new MessageCheckException("Could not find the field "+fieldName);
            }

            //check the field against the annotation signature
            MessageMetadata.checkField(field,field.getGenericType(),element);

            try {
                Method getter = clazz.getDeclaredMethod(getterName);
                this.getters.put(fieldName,getter);
            } catch (NoSuchMethodException e) {
                throw new MessageCheckException("Could not find the getter for the field "+fieldName);
            }

            try {
                Method setter = clazz.getDeclaredMethod(setterName,field.getType());
                this.setters.put(fieldName,setter);
            } catch (NoSuchMethodException e) {
                throw new MessageCheckException("Could not find the setter for the field "+fieldName);
            }

            i++;
        }
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

    public Class<? extends Serializable> getMessageClass(){ return this.clazz; }

    /**
     * Create a new empty instances of the Message object (for unserialization).
     *
     * @return new empty instance of the message
     */
    public Serializable newInstance(){
        try {
            return this.constructor.newInstance();
        } catch (Exception e) {
           throw new IllegalStateException("Could not create instance: "+e.toString(),e);
        }
    }

    /**
     * Check if the given field is valid according to the given signature element
     *
     * @param field field to check
     * @param element signature element used to check
     * @throws MessageCheckException
     */
    private static void checkField(Field field, Type fieldType, SignatureElement element) throws MessageCheckException{
        //get primitive will catch primitive fields and primitive arrays
        if(element.getPrimitive() != null){
            boolean isList = fieldType instanceof ParameterizedType;
            //check the generic is a List and if it matched the signature
            if(isList && (!List.class.isAssignableFrom((Class)((ParameterizedType)fieldType).getRawType()) || element.getContainerType() != SupportedTypes.ARRAY))
                throw new MessageCheckException("the field "+field.getName()+" is not of a List or the signature is not expecting a list");

            //check if the generic type contains the correct type
            SupportedTypes type = element.getPrimitive();
            if(isList){
                ParameterizedType paramType = (ParameterizedType) fieldType;
                if(!paramType.getActualTypeArguments()[0].equals(type.getBoxedType()) && !paramType.getActualTypeArguments()[0].equals(type.getPrimitiveType()))
                    throw new MessageCheckException("the field "+field.getName()+" is not of a List of Integer");
            }else {
                if (!fieldType.equals(type.getBoxedType()) && !fieldType.equals(type.getPrimitiveType())) throw new MessageCheckException("the field " + field.getName() + " is not of type "+type);
            }
        }else if(element.isArray()){
            //recursively check the content of the nested list
            MessageMetadata.checkField(field,((ParameterizedType)fieldType).getActualTypeArguments()[0],element.getSignature().getFirst());

        }else if(element.isObject()){
            if(!Serializable.class.isAssignableFrom((Class)fieldType)) throw new MessageCheckException("the field "+field.getName()+" does not contain a serializable type");

            Class<? extends Serializable> clazz = ((Class)fieldType).asSubclass(Serializable.class);
            MessageMetadata testedEntity = new MessageMetadata(clazz);
            Message.addToCache(clazz,testedEntity);

            if(!testedEntity.getSignature().equals(element.getSignatureString()))
                throw new MessageCheckException("the field "+field.getName()+" signature do not match its type signature");
        }
    }
}
