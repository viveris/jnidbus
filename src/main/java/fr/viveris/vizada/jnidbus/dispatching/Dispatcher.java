package fr.viveris.vizada.jnidbus.dispatching;

import fr.viveris.vizada.jnidbus.bindings.bus.EventLoop;
import fr.viveris.vizada.jnidbus.bindings.bus.sendingrequest.ErrorReplySendingRequest;
import fr.viveris.vizada.jnidbus.bindings.bus.sendingrequest.ReplySendingRequest;
import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusObject;
import fr.viveris.vizada.jnidbus.serialization.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class Dispatcher {
    private String path;
    private HashMap<String,ArrayList<Criteria>> handlersCriterias;
    private HashMap<Criteria,HandlerMethod> handlers;
    private EventLoop eventLoop;

    public Dispatcher(String path, EventLoop eventLoop){
        this.path = path;
        this.handlersCriterias = new HashMap<>();
        this.handlers = new HashMap<>();
        this.eventLoop = eventLoop;
    }

    public void addCriteria(String interfaceName, Criteria criteria, HandlerMethod handlerMethod){
        ArrayList<Criteria> interfaceCriterias = this.handlersCriterias.get(interfaceName);

        if(interfaceCriterias == null){
            interfaceCriterias = new ArrayList<>();
            this.handlersCriterias.put(interfaceName,interfaceCriterias);
        }

        if(interfaceCriterias.contains(criteria)) throw new IllegalArgumentException("The criteria "+criteria+" is conflicting with an already registered criteria");
        interfaceCriterias.add(criteria);
        this.handlers.put(criteria,handlerMethod);
    }

    /**
     * Method called by JNI when a message is received
     */
    public void dispatch(DBusObject args, String type, String member, long msgPointer) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ArrayList<Criteria> availableHandlers = this.handlersCriterias.get(type);
        Criteria requestCriteria;
        if(msgPointer == 0){
            requestCriteria = new Criteria(member,args.getSignature(),null, Criteria.HandlerType.SIGNAL);
        }else{
            requestCriteria = new Criteria(member,args.getSignature(),null, Criteria.HandlerType.METHOD);
        }
        if(availableHandlers == null){
            //TODO: Log that a message was received but not dispatched
            return;
        }

        for(Criteria c: availableHandlers){
            if(c.equals(requestCriteria)){
                //match found, try to unserialize
                HandlerMethod handler = this.handlers.get(c);
                Serializable param = handler.getParamType().newInstance();
                param.unserialize(args);
                try{
                    Message returnObject = (Message) handler.call(param);
                    if(returnObject != null){
                        this.eventLoop.send(new ReplySendingRequest(returnObject,msgPointer));
                    }
                }catch (Exception e){
                    this.eventLoop.send(new ErrorReplySendingRequest(e,msgPointer));
                }
                return;
            }
        }

        //TODO: Log that a message was received but not dispatched
    }

    public String getPath() {
        return path;
    }
}
