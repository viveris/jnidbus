package fr.viveris.jnidbus;

import fr.viveris.jnidbus.bindings.bus.Connection;
import fr.viveris.jnidbus.bindings.bus.EventLoop;
import fr.viveris.jnidbus.cache.SignalMetadata;
import fr.viveris.jnidbus.dispatching.Criteria;
import fr.viveris.jnidbus.dispatching.Dispatcher;
import fr.viveris.jnidbus.dispatching.GenericHandler;
import fr.viveris.jnidbus.dispatching.HandlerMethod;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.exception.ConnectionException;
import fr.viveris.jnidbus.message.sendingrequest.SignalSendingRequest;
import fr.viveris.jnidbus.remote.RemoteObjectInterceptor;
import fr.viveris.jnidbus.remote.Signal;

import java.lang.reflect.Proxy;
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
     * Create a new remote object instance which annotated methods will be translated into DBus calls and signals. The
     * given class must be annotated with the RemoteInterface annotation and all of its methods must be annotated with
     * the RemoteMember annotation. The created instance is a Java proxy registered to the current DBus instance ClassLoader.
     *
     * All the methods of the given interface should return either a Promise of a Message or void
     *
     * @param destinationBus
     * @param objectPath
     * @param objectInterface
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T createRemoteObject(String destinationBus, String objectPath, Class<T> objectInterface){
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{objectInterface},new RemoteObjectInterceptor(destinationBus,objectPath,objectInterface,this.eventLoop));
    }

    public void sendSignal(String objectPath, Signal signal){
        SignalMetadata meta = RemoteObjectInterceptor.getFromCache(signal.getClass());
        this.eventLoop.send(new SignalSendingRequest(signal.getParam().serialize(),objectPath,meta.getInterfaceName(),meta.getMember()));
    }


    @Override
    public void close() throws Exception {
        this.eventLoop.close();
    }
}
