package fr.viveris.jnidbus;

import fr.viveris.jnidbus.bindings.bus.EventLoop;
import fr.viveris.jnidbus.dispatching.Criteria;
import fr.viveris.jnidbus.dispatching.Dispatcher;
import fr.viveris.jnidbus.dispatching.GenericHandler;
import fr.viveris.jnidbus.dispatching.HandlerMethod;
import fr.viveris.jnidbus.exception.ConnectionException;
import fr.viveris.jnidbus.message.PendingCall;
import fr.viveris.jnidbus.message.Signal;
import fr.viveris.jnidbus.bindings.bus.Connection;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.message.Call;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.message.sendingrequest.CallSendingRequest;
import fr.viveris.jnidbus.message.sendingrequest.SignalSendingRequest;

import java.util.HashMap;

/**
 * Public API of the library. contains all the primitives to interact with Dbus.
 */
public class Dbus implements AutoCloseable {
    /**
     * First of all, load the JNI library
     */
    static{
        System.loadLibrary("jnidbus");
    }

    /**
     * DBus connection object
     */
    private Connection connection;

    /**
     * Event loop to which send the messages
     */
    private EventLoop eventLoop;

    /**
     * List of the registered dispatchers mapped by their object path
     */
    private HashMap<String, Dispatcher> dispatchers;

    /**
     * Create a new DBus connection with the given type and name.
     *
     * @param type bus type, please refer to the DBus documentation for more info on them
     * @param busName bus name, should respect the Dbus name format (similar to the java namespace format)
     * @throws ConnectionException thrown id something goes wrong with dbus (bus name already in used, bus unavailable, etc...)
     */
    public Dbus(BusType type, String busName) throws ConnectionException {
        this.connection = Connection.createConnection(type,busName);
        this.eventLoop = new EventLoop(this.connection);
        this.dispatchers = new HashMap<>();
    }

    /**
     * Add a handler object, this method will create and register a new dispatcher if needed. All the Messages classes used
     * by the object will be checked and cached in the process, allowing the developer to quickly detect mapping mistakes.
     *
     * If one of the given handler object methods clash with another handler method, an exception will be thrown
     *
     * @param handler handler to register
     */
    public void addHandler(GenericHandler handler){
        //get annotation
        Handler handlerAnnotation = handler.getClass().getAnnotation(Handler.class);
        if(handlerAnnotation == null) throw new IllegalStateException("The given handler does not have the Handler annotation");

        //get all criteria provided by this handler
        HashMap<Criteria, HandlerMethod> criterias = handler.getAvailableCriterias();

        //try to get the dispatcher, if no dispatcher are found, create one
        Dispatcher dispatcher = this.dispatchers.get(handlerAnnotation.path());
        boolean dispatcherCreated = false;
        if(dispatcher == null){
            dispatcher = new Dispatcher(handlerAnnotation.path(),this.eventLoop);
            dispatcherCreated = true;
        }

        //put the criteria in the dispatcher
        for(Criteria c : criterias.keySet()){
            dispatcher.addCriteria(handlerAnnotation.interfaceName(),c,criterias.get(c));
        }

        //if the dispatcher was created, register it to Dbus a block until it is effectively registered
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

    /**
     * Send a Signal to the bus. This method is asynchronous and will throw an exception if the sending queue is full.
     * If the queue is full you should wait for it to drain and try to send again.
     *
     * @param sig signal to send
     */
    public void sendSignal(Signal sig){
        this.eventLoop.send(new SignalSendingRequest(sig.getParams(),sig.getPath(),sig.getInterfaceName(),sig.getMember()));
    }

    /**
     * Call a dbus method. This method is asynchronous and will throw an exception if the sending queue is full. If the
     * queue is full you should wait for it to drain and try to send again. The returned PendingCall provides to needed
     * API to wait for the result or error to be received.
     *
     * @param call call to send
     * @param <T> return type of the call
     * @return PendingCall for the sent call
     */
    public <T extends Message> PendingCall<T> call(Call<?,T> call){
        PendingCall<T> pending = new PendingCall<>(call.getReturnType(),this.eventLoop);
        this.eventLoop.send(new CallSendingRequest(call.getParams(),call.getPath(),call.getInterfaceName(),call.getMember(),call.getDestination(),pending));
        return pending;
    }

    @Override
    public void close() throws Exception {
        this.eventLoop.close();
    }
}
