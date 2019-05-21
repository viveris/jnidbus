package fr.viveris.jnidbus.remote;

import fr.viveris.jnidbus.bindings.bus.EventLoop;
import fr.viveris.jnidbus.cache.Cache;
import fr.viveris.jnidbus.cache.RemoteObjectMetadata;
import fr.viveris.jnidbus.cache.SignalMetadata;
import fr.viveris.jnidbus.exception.RemoteObjectCheck;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.message.PendingCall;
import fr.viveris.jnidbus.message.sendingrequest.CallSendingRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class RemoteObjectInterceptor implements InvocationHandler {

    /**
     * Cache containing the reflection data. The map use a ClassLoader as a key in order to support same name classes loaded
     * by different class loaders. In addition this map is weak so an unused class loader can be freed without issues
     * (hot reload of classes for example)
     */
    private static final Map<ClassLoader, Cache<Method, RemoteObjectMetadata>> METHOD_CACHE =
            Collections.synchronizedMap(new WeakHashMap<ClassLoader, Cache<Method, RemoteObjectMetadata>>());

    private static final Map<ClassLoader, Cache<Class<? extends Signal>, SignalMetadata>> SIGNAL_CACHE =
            Collections.synchronizedMap(new WeakHashMap<ClassLoader, Cache<Class<? extends Signal>, SignalMetadata>>());

    private String destinationBus;
    private String objectPath;
    private String interfaceName;

    private EventLoop eventLoop;

    public RemoteObjectInterceptor(String destinationBus, String objectPath, Class<?> remoteObjectClass, EventLoop eventLoop){
        this.destinationBus = destinationBus;
        this.objectPath = objectPath;
        this.eventLoop = eventLoop;

        RemoteInterface interfaceName = remoteObjectClass.getAnnotation(RemoteInterface.class);
        if(interfaceName == null) throw new IllegalArgumentException("The given interface is not annotated with RemoteInterface");
        this.interfaceName = interfaceName.value();

        //check class
        for(Method m : remoteObjectClass.getDeclaredMethods()){
            //check method and put in cache
            RemoteObjectInterceptor.getFromCache(m);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        //if there is no parameter, it means we have an empty call
        Message msg;
        if(objects == null){
            msg = Message.EMPTY;
        }else{
            msg = (Message) objects[0];
        }

        //get metadata from cache
        RemoteObjectMetadata meta = RemoteObjectInterceptor.getFromCache(method);

        //create a wildcard pendingCall, this wont fail as the method was checked and return the correct type
        PendingCall pendingCall = new PendingCall(meta.getOutputMetadata().getMessageClass(),this.eventLoop);

        //send call and return the PendingCall
        this.eventLoop.send(new CallSendingRequest(msg.serialize(),this.objectPath,this.interfaceName,meta.getMember(),this.destinationBus,pendingCall));
        return pendingCall;
    }

    private static RemoteObjectMetadata getFromCache(Method method){
        //check if the entity is in cache, if so everything have already been checked and cached
        ClassLoader cl = method.getDeclaringClass().getClassLoader();
        if (!METHOD_CACHE.containsKey(cl)) METHOD_CACHE.put(cl, new Cache<Method, RemoteObjectMetadata>());
        if (METHOD_CACHE.get(cl).getCachedEntity(method) != null) return METHOD_CACHE.get(cl).getCachedEntity(method);

        //the method is not in cache, build the cache entity, add it and return
        try {
            RemoteObjectMetadata meta = new RemoteObjectMetadata(method);
            METHOD_CACHE.get(cl).addCachedEntity(method,meta);
            return meta;
        } catch (RemoteObjectCheck remoteObjectCheck) {
            throw new IllegalStateException("The remote object method: "+method.getName()+" is invalid",remoteObjectCheck);
        }
    }

    public static SignalMetadata getFromCache(Class<? extends Signal> signal){
        //check if the entity is in cache, if so everything have already been checked and cached
        ClassLoader cl = signal.getDeclaringClass().getClassLoader();
        if (!SIGNAL_CACHE.containsKey(cl)) SIGNAL_CACHE.put(cl, new Cache<Class<? extends Signal>, SignalMetadata>());
        if (SIGNAL_CACHE.get(cl).getCachedEntity(signal) != null) return SIGNAL_CACHE.get(cl).getCachedEntity(signal);

        SignalMetadata meta = new SignalMetadata(signal);
        SIGNAL_CACHE.get(cl).addCachedEntity(signal,meta);
        return meta;
    }
}
