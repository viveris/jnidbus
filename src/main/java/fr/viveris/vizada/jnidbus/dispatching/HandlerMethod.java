package fr.viveris.vizada.jnidbus.dispatching;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.Serializable;
import fr.viveris.vizada.jnidbus.serialization.cache.CachedEntity;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Contains all the information used to call an exposed method. When instantiated, this class will try to put in
 * cache the input and output types, which will check the validity of the types
 */
public class HandlerMethod{

    /**
     * Object on which we call the methods
     */
    private Object handler;

    /**
     * Method we want to call
     */
    private Method handlerMethod;

    /**
     * cached input type
     */
    private CachedEntity inputType;

    /**
     * cached output type (can be null)
     */
    private CachedEntity outputType;

    /**
     * Create a new HandlerMethod. Will throw if the types of the methods are invalid
     *
     * @param handler object on which we want to call the method
     * @param handlerMethod method to call
     */
    public HandlerMethod(Object handler, Method handlerMethod) {
        //check the type class
        if(!Serializable.class.isAssignableFrom(handlerMethod.getReturnType()) && !Void.TYPE.isAssignableFrom(handlerMethod.getReturnType()) ){
            throw new IllegalArgumentException("The given method does not have a serializable or void output");
        }
        if(handlerMethod.getParameterTypes()[0].isAssignableFrom(Serializable.class)) throw new IllegalArgumentException("The given method does not have a serializable input");

        this.handler = handler;
        this.handlerMethod = handlerMethod;

        //put input and output in cache
        this.inputType = Message.retreiveFromCache(handlerMethod.getParameterTypes()[0].asSubclass(Serializable.class));
        if(!Void.TYPE.isAssignableFrom(handlerMethod.getReturnType())){
            this.outputType = Message.retreiveFromCache(handlerMethod.getReturnType().asSubclass(Serializable.class));
        }else{
            this.outputType = null;
        }
    }

    public Class<? extends Serializable> getReturnType(){
        return this.handlerMethod.getReturnType().asSubclass(Serializable.class);
    }

    public Class<? extends Serializable> getParamType(){
        return this.handlerMethod.getParameterTypes()[0].asSubclass(Serializable.class);
    }

    public Object call(Serializable param) throws InvocationTargetException, IllegalAccessException {
        return this.handlerMethod.invoke(this.handler,param);
    }

    public CachedEntity getInputType() {
        return inputType;
    }

    public CachedEntity getOutputType() {
        return outputType;
    }
}
