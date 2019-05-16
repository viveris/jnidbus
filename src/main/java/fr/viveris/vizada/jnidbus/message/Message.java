package fr.viveris.vizada.jnidbus.message;

import fr.viveris.vizada.jnidbus.exception.MessageSignatureMismatch;
import fr.viveris.vizada.jnidbus.serialization.*;
import fr.viveris.vizada.jnidbus.serialization.cache.Cache;
import fr.viveris.vizada.jnidbus.serialization.cache.CachedEntity;
import fr.viveris.vizada.jnidbus.serialization.signature.Signature;
import fr.viveris.vizada.jnidbus.serialization.signature.SignatureElement;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class Message implements Serializable {
    public static final EmptyMessage EMPTY = new EmptyMessage();

    //the reflection cache is stored in a weak hash map in order to correctly free a class loader when not needed anymore.
    //this si also made so classes with the same name but loaded by two different class loader can have their own cache
    private static final WeakHashMap<ClassLoader, Cache> CACHE = new WeakHashMap<>();


    @Override
    public DBusObject serialize() {
        Class<? extends Message> clazz = this.getClass();
        CachedEntity cachedEntity = Message.retreiveFromCache(clazz);

        //set the array length at the number of field and iterate on every char of the signature
        Object[] values = new Object[cachedEntity.getFields().length];
        int i = 0;
        for(SignatureElement element : new Signature(cachedEntity.getSignature())){
            String fieldName = cachedEntity.getFields()[i];

            try{
                Method getter = cachedEntity.getGetter(fieldName);
                //primitive types don't need any kind of processing beside casting
                if(element.isPrimitive()){
                    values[i] = getter.invoke(this);
                }else if(element.isArray()){
                    //we can directly cas as List as the cached entity checked this
                    List value = (List) getter.invoke(this);
                    values[i] = Message.serializeList(value,element,((ParameterizedType)getter.getGenericReturnType()).getActualTypeArguments()[0]);

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

        return new DBusObject(cachedEntity.getSignature(),values);
    }

    /**
     * Unserialize the object from the raw DBusObject. Please not that nested DBusObject wont have their signature set and we have to do it ourself
     * @param obj
     * @throws MessageSignatureMismatch
     */
    @Override
    public void unserialize(DBusObject obj) throws MessageSignatureMismatch {
        Class<? extends Message> clazz = this.getClass();
        CachedEntity cachedEntity = Message.retreiveFromCache(clazz);

        if(!cachedEntity.getSignature().equals(obj.getSignature())) throw new MessageSignatureMismatch("Signature mismatch, expected "+cachedEntity.getSignature()+" but got "+obj.getSignature());

        Object[] values = obj.getValues();
        int i = 0;
        for(SignatureElement element : new Signature(cachedEntity.getSignature())){
            String fieldName = cachedEntity.getFields()[i];

            try{
                Method setter = cachedEntity.getSetter(fieldName);
                if(element.isPrimitive()){
                    setter.invoke(this,values[i]);
                }else if(element.isArray()){
                    setter.invoke(this,Message.unserializeList((Object[])values[i],element,((ParameterizedType)setter.getGenericParameterTypes()[0]).getActualTypeArguments()[0]));
                }else if(element.isObject()){
                    Class<?> objClazz = clazz.getDeclaredField(fieldName).getType();
                    if(!Serializable.class.isAssignableFrom(objClazz)) throw new IllegalStateException("The setter "+setter.getName()+" takes a non-serializable type as parameter");
                    Serializable unserialized = objClazz.asSubclass(Serializable.class).newInstance();
                    unserialized.unserialize(new DBusObject(element.getSignatureString(),((DBusObject)values[i]).getValues()));
                    setter.invoke(this,unserialized);

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
                String elementSignatureString = signature.getSignature().getFirst().getSignatureString();
                for(Object o : values){
                    if(isRecusiveList){
                        list.add(Message.unserializeList((Object[]) o,signature.getSignature().getFirst(),((ParameterizedType)genericType).getActualTypeArguments()[0]));
                    }else{
                        if(!(o instanceof DBusObject)) throw new IllegalStateException("The values are not unserializable objects");
                        Serializable obj = ((Class<? extends Serializable>)genericType).newInstance();
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
     * This method will test that its own signature and the one of its child entities match. If so the metadata will be cached for faster
     * serialization/unserialization.
     */
    private static CachedEntity testAndCache(Class<? extends Serializable> clazz) throws Exception{

        //check if the entity is in cache, if so everything have already been chacked and cached
        if(!CACHE.containsKey(clazz.getClassLoader())) CACHE.put(clazz.getClassLoader(),new Cache());
        if(CACHE.get(clazz.getClassLoader()).getCachedEntity(clazz.getName()) != null) return CACHE.get(clazz.getClassLoader()).getCachedEntity(clazz.getName());

        DBusType type = clazz.getAnnotation(DBusType.class);
        if(type == null) throw new IllegalStateException("No DBusType annotation found");
        //TODO check signature

        Constructor<? extends Serializable> constructor;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No public empty constructor found");
        }

        CachedEntity cachedEntity = new CachedEntity(type.signature(),type.fields(),constructor);

        //generate getter and setter list
        int i = 0;
        for(SignatureElement element : new Signature(type.signature())){
            String fieldName = type.fields()[i];
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

            //TODO: checks

            Method getter = clazz.getDeclaredMethod(getterName);
            cachedEntity.addGetter(fieldName,getter);

            Method setter = clazz.getDeclaredMethod(setterName,clazz.getDeclaredField(fieldName).getType());
            cachedEntity.addSetter(fieldName,setter);

            i++;
        }

        //put cached entity in global cache
        Cache cacheContainer = CACHE.get(clazz.getClassLoader());
        cacheContainer.addCachedEntity(clazz.getName(),cachedEntity);
        return cachedEntity;
    }

    public static CachedEntity retreiveFromCache(Class<? extends Serializable> clazz){
        //try to retrieve from cache or create cache
        Cache cache = CACHE.get(clazz.getClassLoader());
        CachedEntity cachedEntity;
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

    @DBusType(
            signature = "",
            fields = ""
    )
    public static class EmptyMessage extends Message{
        public EmptyMessage(){}
    }
}
