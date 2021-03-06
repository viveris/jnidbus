/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.dispatching;

import fr.viveris.jnidbus.cache.MessageMetadata;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.serialization.Serializable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Contains all the information used to call an exposed method. When instantiated, this class will try to put in
 * cache the input and output types, which will check the validity of the types
 */
public class HandlerMethod{
    static MethodInvocator kotlinInvocator;

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
    private MessageMetadata inputType;

    /**
     * cached output type (can be null)
     */
    private MessageMetadata outputType;

    /**
     * Return type class
     */
    private Class<? extends Serializable> returnClass;

    /**
     * Is the current method a kotlin method
     */
    private boolean isKotlinMethod;

    /**
     * Create a new HandlerMethod. Will throw if the types of the methods are invalid
     *
     * @param handler object on which we want to call the method
     * @param handlerMethod method to call
     * @param returnType return type class of the method
     */
    public HandlerMethod(Object handler, Method handlerMethod, Class<? extends Serializable> returnType) {
        this.returnClass = returnType;
        this.handler = handler;
        this.handlerMethod = handlerMethod;

        //put input and output in cache
        this.inputType = Message.retrieveFromCache(handlerMethod.getParameterTypes()[0].asSubclass(Serializable.class));
        if( returnType != null){
            this.outputType = Message.retrieveFromCache(returnType.asSubclass(Serializable.class));
        }else{
            this.outputType = null;
        }

        Class handlerClass = handler.getClass();
        ClassLoader handlerClassLoader = handlerClass.getClassLoader();
        try {
            this.isKotlinMethod = handler.getClass().isAnnotationPresent(handlerClassLoader.loadClass("kotlin.Metadata").asSubclass(Annotation.class));
        } catch (ClassNotFoundException e) {
            this.isKotlinMethod = false;
        }
    }

    public Class<? extends Serializable> getReturnType(){
        return this.returnClass;
    }

    public Class<? extends Serializable> getParamType(){
        return this.handlerMethod.getParameterTypes()[0].asSubclass(Serializable.class);
    }

    public Object call(Serializable param) throws InvocationTargetException, IllegalAccessException {
        if(this.isKotlinMethod && kotlinInvocator != null){
            return kotlinInvocator.call(this.handler,this.handlerMethod,param);
        }else{
            return this.handlerMethod.invoke(this.handler,param);
        }
    }

    public MessageMetadata getInputType() {
        return inputType;
    }

    public MessageMetadata getOutputType() {
        return outputType;
    }

    public interface MethodInvocator{
        <T extends Serializable> Object call(Object handler, Method method, Serializable param);
    }
}
