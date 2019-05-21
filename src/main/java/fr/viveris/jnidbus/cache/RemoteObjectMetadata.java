package fr.viveris.jnidbus.cache;

import fr.viveris.jnidbus.dispatching.MemberType;
import fr.viveris.jnidbus.exception.RemoteObjectCheck;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.message.PendingCall;
import fr.viveris.jnidbus.remote.RemoteMember;
import fr.viveris.jnidbus.serialization.Serializable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RemoteObjectMetadata {
    private Method method;
    private String member;
    private MessageMetadata inputMetadata;
    private MessageMetadata outputMetadata;

    /**
     * Create a new
     * @param method
     */
    @SuppressWarnings("unchecked")
    public RemoteObjectMetadata(Method method) throws RemoteObjectCheck {
        this.method = method;

        RemoteMember annotation = method.getAnnotation(RemoteMember.class);
        if(annotation == null) throw new RemoteObjectCheck("No RemoteMember annotation were found");

        this.member = annotation.value();

        //process input type
        Class<?>[] params = method.getParameterTypes();
        if(params.length == 0){
            this.inputMetadata = Message.retreiveFromCache(Message.EmptyMessage.class);
        } else if(params.length == 1){
            if(!Serializable.class.isAssignableFrom(params[0])) throw new RemoteObjectCheck("A remote method parameter must be serializable");
            Class<? extends Serializable> inputClass = params[0].asSubclass(Serializable.class);
            this.inputMetadata = Message.retreiveFromCache(inputClass);
        }else{
            throw new RemoteObjectCheck("A remote method can only have one parameter");
        }

        //process output type
        Type output = method.getGenericReturnType();
        if(!(output instanceof ParameterizedType)) throw new RemoteObjectCheck("The return type of the method is not generic or its generic type is not explicit");
        if(!PendingCall.class.equals(((ParameterizedType)output).getRawType())) throw new RemoteObjectCheck("The return type is not a PendingCall");

        Type realOutputType = ((ParameterizedType)output).getActualTypeArguments()[0];
        this.outputMetadata = Message.retreiveFromCache(((Class)realOutputType).asSubclass(Serializable.class));

    }

    public Method getMethod() {
        return method;
    }

    public String getMember() {
        return member;
    }

    public MessageMetadata getInputMetadata() {
        return inputMetadata;
    }

    public MessageMetadata getOutputMetadata() {
        return outputMetadata;
    }
}