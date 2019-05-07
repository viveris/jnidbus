package fr.viveris.vizada.jnidbus.dispatching;

import fr.viveris.vizada.jnidbus.serialization.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerMethod{

    private Object handler;
    private Method handlerMethod;

    /**
     * Because of Java type erasure we can't check that the given method respect the generic types
     * @param handler
     * @param handlerMethod
     */
    public HandlerMethod(Object handler, Method handlerMethod) {
        if(!Serializable.class.isAssignableFrom(handlerMethod.getReturnType()) && !Void.TYPE.isAssignableFrom(handlerMethod.getReturnType()) ){
            throw new IllegalArgumentException("The given method does not have a serializable or void output");
        }

        if(handlerMethod.getParameterTypes()[0].isAssignableFrom(Serializable.class)) throw new IllegalArgumentException("The given method does not have a serializable input");

        this.handler = handler;
        this.handlerMethod = handlerMethod;
    }

    public Class<? extends Serializable> getReturnType(){
        return this.handlerMethod.getReturnType().asSubclass(Serializable.class);
    }

    public Class<? extends Serializable> getParamType(){
        return this.handlerMethod.getParameterTypes()[0].asSubclass(Serializable.class);
    }

    public Object call(Object param) throws InvocationTargetException, IllegalAccessException {
        return this.handlerMethod.invoke(this.handler,param);
    }
}
