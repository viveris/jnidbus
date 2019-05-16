package fr.viveris.vizada.jnidbus;

import fr.viveris.vizada.jnidbus.bindings.bus.Connection;
import fr.viveris.vizada.jnidbus.bindings.bus.EventLoop;
import fr.viveris.vizada.jnidbus.exception.ConnectionException;
import fr.viveris.vizada.jnidbus.message.sendingrequest.CallSendingRequest;
import fr.viveris.vizada.jnidbus.message.sendingrequest.SignalSendingRequest;
import fr.viveris.vizada.jnidbus.dispatching.*;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.message.Call;
import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.message.PendingCall;
import fr.viveris.vizada.jnidbus.message.Signal;

import java.util.HashMap;

public class Dbus implements AutoCloseable {
    static{
        System.loadLibrary("jnidbus");
    }

    private Connection connection;
    private EventLoop eventLoop;
    private HashMap<String, Dispatcher> dispatchers;

    /**
     *
     * @param type Not nullable, type of bus (Session, System, Starter)
     * @param busName Not nullable, name of the bus that will be registered to DBus, the name must be unique
     */
    public Dbus(BusType type, String busName) throws ConnectionException {
        this.connection = Connection.createConnection(type,busName);
        this.eventLoop = new EventLoop(this.connection);
        this.dispatchers = new HashMap<>();
    }

    public void addMessageHandler(GenericHandler handler){
        Handler handlerAnnotation = handler.getClass().getAnnotation(Handler.class);
        if(handlerAnnotation == null) throw new IllegalStateException("The given handler does not have the Handler annotation");
        HashMap<Criteria, HandlerMethod> criterias = handler.getAvailableCriterias();
        Dispatcher dispatcher = this.dispatchers.get(handlerAnnotation.path());

        boolean dispatcherCreated = false;
        if(dispatcher == null){
            dispatcher = new Dispatcher(handlerAnnotation.path(),this.eventLoop);
            dispatcherCreated = true;
        }

        for(Criteria c : criterias.keySet()){
            dispatcher.addCriteria(handlerAnnotation.interfaceName(),c,criterias.get(c));
        }

        if(dispatcherCreated){
            this.eventLoop.addPathHandler(dispatcher);
            try {
                dispatcher.awaitRegistration();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Dispatcher registration was interrupted");
            }
            this.dispatchers.put(handlerAnnotation.path(),dispatcher);
        }
    }

    public void sendSignal(Signal sig){
        this.eventLoop.send(new SignalSendingRequest(sig.getParams(),sig.getPath(),sig.getInterfaceName(),sig.getMember()));
    }

    public <T extends Message> PendingCall<T> call(Call<?,T> call){
        PendingCall<T> pending = new PendingCall<>(call.getReturnType());
        this.eventLoop.send(new CallSendingRequest(call.getParams(),call.getPath(),call.getInterfaceName(),call.getMember(),call.getDestination(),pending));
        return pending;
    }

    @Override
    public void close() throws Exception {
        this.eventLoop.close();
    }
}
