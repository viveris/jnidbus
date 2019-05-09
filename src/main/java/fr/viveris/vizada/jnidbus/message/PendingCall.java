package fr.viveris.vizada.jnidbus.message;

import fr.viveris.vizada.jnidbus.exception.DBusException;
import fr.viveris.vizada.jnidbus.serialization.DBusObject;
import fr.viveris.vizada.jnidbus.serialization.Serializable;

public class PendingCall<T extends Serializable> {
    private Class<T> clazz;
    private Listener<T> listener;
    private T result;
    private DBusException error;


    public PendingCall(Class<T> clazz){
        this.clazz = clazz;
    }

    public void setListener(Listener<T> listener){
        //if the result is already here, notify instantly
        if(this.result != null){
            listener.notify(this.result);
        } else if(this.error != null){
            listener.error(this.error);
        }
        this.listener = listener;
    }

    public void notify(DBusObject response){
        try {
            T value;
            if(this.clazz.equals(Message.EmptyMessage.class)){
                value = (T) Message.EMPTY;
            }else{
                value = this.clazz.newInstance();
                value.unserialize(response);
            }
            this.result = value;
            if(this.listener != null){
                this.listener.notify(value);
            }
        } catch (Exception e) {
            this.error = new DBusException(e.getClass().getName(),e.getMessage());
            if(this.listener != null){
                this.listener.error(this.error);
            }
        }
    }

    public void fail(String errorName, String message){
        DBusException exc = new DBusException(errorName,message);
        this.listener.error(exc);
    }

    public interface Listener<T>{
        void notify(T value);
        void error(DBusException t);
    }
}
