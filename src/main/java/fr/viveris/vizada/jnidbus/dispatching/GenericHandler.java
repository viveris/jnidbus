package fr.viveris.vizada.jnidbus.dispatching;

import fr.viveris.vizada.jnidbus.serialization.DBusType;
import fr.viveris.vizada.jnidbus.serialization.Serializable;

import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class GenericHandler {

    public HashMap<Criteria,HandlerMethod> getAvailableCriterias(){
        Method[] methods = this.getClass().getMethods();
        HashMap<Criteria,HandlerMethod> returned = new HashMap<>();

        for(Method m : methods){
            fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod annotation = m.getAnnotation(fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod.class);
            if(annotation == null) continue;

            Class<?>[] params = m.getParameterTypes();
            Class<?> returnType = m.getReturnType();
            if(params.length != 1) throw new IllegalArgumentException("Incorrect number of parameter on an handler method");

            DBusType paramAnnotation = params[0].getAnnotation(DBusType.class);
            DBusType returnAnnotation = returnType.getAnnotation(DBusType.class);
            String ouputSignature = "";

            if(paramAnnotation == null || !Serializable.class.isAssignableFrom(params[0])){
                throw new IllegalArgumentException("A handler method parameter must have the DBusType annotation and be Serializable");
            }

            if(annotation.type() == Criteria.HandlerType.METHOD && (returnAnnotation == null || !Serializable.class.isAssignableFrom(returnType))){
                throw new IllegalArgumentException("A handler method return type must have the DBusType annotation and be Serializable");
            }else if(returnAnnotation != null){
                ouputSignature = returnAnnotation.signature();
            }

            returned.put(new Criteria(annotation.member(),paramAnnotation.signature(),ouputSignature,annotation.type()),new HandlerMethod(this,m));

        }
        return returned;
    }
}
