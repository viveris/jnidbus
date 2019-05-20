package fr.viveris.jnidbus.message;

import fr.viveris.jnidbus.serialization.DBusObject;
import fr.viveris.jnidbus.serialization.DBusType;
import fr.viveris.jnidbus.serialization.Serializable;
import fr.viveris.jnidbus.serialization.signature.Signature;
import fr.viveris.jnidbus.serialization.signature.SupportedTypes;
import fr.viveris.jnidbus.exception.MessageCheckException;
import fr.viveris.jnidbus.exception.MessageSignatureMismatch;
import fr.viveris.jnidbus.serialization.cache.Cache;
import fr.viveris.jnidbus.serialization.cache.CachedEntity;
import fr.viveris.jnidbus.serialization.signature.SignatureElement;

import java.lang.reflect.*;
import java.util.*;

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
    private static final WeakHashMap<ClassLoader, Cache> CACHE = new WeakHashMap<>();


    @Override
    public DBusObject serialize() {
        //retrieve reflection data from the cache
        Class<? extends Message> clazz = this.getClass();
        CachedEntity cachedEntity = Message.retreiveFromCache(clazz);

        //set the array length at the number of field and iterate on the signature
        Object[] values = new Object[cachedEntity.getFields().length];
        int i = 0;
        for(SignatureElement element : new Signature(cachedEntity.getSignature())){
            //get the field name for the current signature element
            String fieldName = cachedEntity.getFields()[i];

            try{
                //retrieve the getter from the cache
                Method getter = cachedEntity.getGetter(fieldName);

                if(element.isPrimitive()){
                    //primitive types don't need any kind of processing
                    values[i] = getter.invoke(this);
                }else if(element.isArray()){
                    //we can directly cas as List as the cached entity checked this when created then serialize it
                    List value = (List) getter.invoke(this);
                    values[i] = Message.serializeList(value,element,((ParameterizedType)getter.getGenericReturnType()).getActualTypeArguments()[0]);

                }else if(element.isObject()){
                    //recursively serialize
                    values[i] = ((Serializable)getter.invoke(this)).serialize();
                }else{
                    throw new IllegalStateException("Unknown type detected: "+element.toString());
                }
            }catch (Exception e){
                throw new IllegalStateException("An exception was raised during serialization "+e.toString(),e);
            }
            //go to the next field
            i++;
        }

        return new DBusObject(cachedEntity.getSignature(),values);
    }

    @Override
    public void unserialize(DBusObject obj) throws MessageSignatureMismatch {
        //retrieve reflection data from the cache
        Class<? extends Message> clazz = this.getClass();
        CachedEntity cachedEntity = Message.retreiveFromCache(clazz);

        //check if the given pre-unserialized object have the same signature as this class
        if(!cachedEntity.getSignature().equals(obj.getSignature())) throw new MessageSignatureMismatch("Signature mismatch, expected "+cachedEntity.getSignature()+" but got "+obj.getSignature());

        //get the value sand iterate through the signature
        Object[] values = obj.getValues();
        int i = 0;
        for(SignatureElement element : new Signature(cachedEntity.getSignature())){
            //get the field name for the current signature element
            String fieldName = cachedEntity.getFields()[i];

            try{
                //retrieve the setter from the cache
                Method setter = cachedEntity.getSetter(fieldName);

                if(element.isPrimitive()){
                    //primitive types do not need any processing
                    setter.invoke(this,values[i]);
                }else if(element.isArray()){
                    setter.invoke(this,Message.unserializeList((Object[])values[i],element,((ParameterizedType)setter.getGenericParameterTypes()[0]).getActualTypeArguments()[0]));
                }else if(element.isObject()){
                    //retrieve the needed object class from cache and create a new instance
                    Serializable unserialized = Message.retreiveFromCache(setter.getParameterTypes()[0].asSubclass(Serializable.class)).newInstance();
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
     * @throws MessageSignatureMismatch
     */
    private static List unserializeList(Object[] values , SignatureElement signature, Type genericType) throws MessageSignatureMismatch {
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
                        Serializable obj = Message.retreiveFromCache((Class<? extends Serializable>)genericType).newInstance();
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
     * Test if the message is valid according to its signature and fields and if so, cache its reflection data for a
     * faster processing later
     *
     * @param clazz class of the message to cache
     * @return the cached representation of the class
     * @throws Exception
     */
    private static CachedEntity testAndCache(Class<? extends Serializable> clazz) throws MessageCheckException {
        //check if the entity is in cache, if so everything have already been checked and cached
        if(!CACHE.containsKey(clazz.getClassLoader())) CACHE.put(clazz.getClassLoader(),new Cache());
        if(CACHE.get(clazz.getClassLoader()).getCachedEntity(clazz.getName()) != null) return CACHE.get(clazz.getClassLoader()).getCachedEntity(clazz.getName());

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
        CachedEntity cachedEntity = new CachedEntity(type.signature(),type.fields(),constructor);

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
            Message.checkField(field,field.getGenericType(),element);

            try {
                Method getter = clazz.getDeclaredMethod(getterName);
                cachedEntity.addGetter(fieldName,getter);
            } catch (NoSuchMethodException e) {
                throw new MessageCheckException("Could not find the getter for the field "+fieldName);
            }

            try {
                Method setter = clazz.getDeclaredMethod(setterName,field.getType());
                cachedEntity.addSetter(fieldName,setter);
            } catch (NoSuchMethodException e) {
                throw new MessageCheckException("Could not find the setter for the field "+fieldName);
            }

            i++;
        }

        //put cached entity in global cache
        Cache cacheContainer = CACHE.get(clazz.getClassLoader());
        cacheContainer.addCachedEntity(clazz.getName(),cachedEntity);
        return cachedEntity;
    }

    /**
     * Check if the given field is valid according to the given signature element
     *
     * @param field field to check
     * @param element signature element used to check
     * @throws MessageCheckException
     */
    private static void checkField(Field field,Type fieldType, SignatureElement element) throws MessageCheckException{
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
            Message.checkField(field,((ParameterizedType)fieldType).getActualTypeArguments()[0],element.getSignature().getFirst());

        }else if(element.isObject()){
            if(!Serializable.class.isAssignableFrom((Class)fieldType)) throw new MessageCheckException("the field "+field.getName()+" does not contain a serializable type");
            CachedEntity testedEntity = Message.testAndCache(((Class)fieldType).asSubclass(Serializable.class));
            if(!testedEntity.getSignature().equals(element.getSignatureString()))
                throw new MessageCheckException("the field "+field.getName()+" signature do not match its type signature");
        }
    }

    /**
     * Try to retrieve the cached metadata for the given class, if the cache entity does not exists, check the class,
     * try to create one and return it. If the class is invalid, it will throw
     *
     * @param clazz class to retrieve
     * @return the cached entity
     */
    public static CachedEntity retreiveFromCache(Class<? extends Serializable> clazz){
        //try to retrieve from cache or create cache
        Cache cache = CACHE.get(clazz.getClassLoader());
        CachedEntity cachedEntity;
        //if the cache is null, make the entity null, the cache will be created when the clazz is processed
        if(cache == null){
            cachedEntity = null;
        }else{
            cachedEntity = cache.getCachedEntity(clazz.getName());
        }
        if(cachedEntity == null){
            try {
                cachedEntity = Message.testAndCache(clazz);
            } catch (Exception e) {
                throw new IllegalStateException("Message validity check failed: "+e,e);
            }
        }
        return cachedEntity;
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
