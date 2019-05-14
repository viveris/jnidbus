package fr.viveris.vizada.jnidbus.bindings.bus;

import fr.viveris.vizada.jnidbus.exception.DBusException;
import fr.viveris.vizada.jnidbus.exception.EventLoopSetupException;
import fr.viveris.vizada.jnidbus.message.sendingrequest.*;
import fr.viveris.vizada.jnidbus.dispatching.Dispatcher;
import fr.viveris.vizada.jnidbus.message.PendingCall;
import fr.viveris.vizada.jnidbus.serialization.DBusObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EventLoop implements Closeable {
    public static final int SENDING_QUEUE_SIZE = 100;

    private long dBusContextPointer;

    private AtomicBoolean isClosed = new AtomicBoolean(true);

    private AtomicInteger inQueue = new AtomicInteger(0);

    /**
     * Thread running the event loop
     */
    private Thread thread;

    /**
     * Store the matching rules we want to register
     */
    private LinkedBlockingDeque<Dispatcher> handlerAddingQueue = new LinkedBlockingDeque<>();

    private LinkedBlockingDeque<AbstractSendingRequest> signalSendingQueue = new LinkedBlockingDeque<>(SENDING_QUEUE_SIZE);

    private Connection connection;

    //used to ensure the vent loop fully started before doing anything
    private CountDownLatch startBarrier = new CountDownLatch(1);

    public EventLoop(Connection connection){
        this.connection = connection;
        this.dBusContextPointer = connection.getdBusContextPointer();
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EventLoop.this.run();
                } catch (EventLoopSetupException e) {
                    EventLoop.this.isClosed.set(true);
                    EventLoop.this.startBarrier.countDown();
                }
            }
        });
        this.thread.setName("DBus event loop");
        this.thread.start();
    }

    /**
     * This method will take care of setting up epoll and all the timeout and watch functions of dbus.
     */
    private native boolean setup(long contextPtr) throws EventLoopSetupException;

    /**
     * Will call epoll, wait for events and dispatch those events to dbus. we can interupt an ongoing tick by calling the wakeup() function.
     */
    private native void tick(long contextPtr, int timeout);

    /**
     * Will send a byte on the wakeup pipe, which will make epoll_wait return, making the ongoing tick stop
     */
    public native void wakeup(long contextPtr);

    private native void sendReply(long contextPtr, DBusObject msg, long msgPointer);
    private native void sendReplyError(long contextPtr, long msgPointer, String errorName, String errorMessage);
    private native void sendSignal(long contextPtr, String path, String interfaceName, String member, DBusObject msg);
    private native void sendCall(long contextPtr, String path, String interfaceName, String member, DBusObject msg, String dest, PendingCall pendingCall);

    private native void addPathHandler(long contextPtr, String path, Dispatcher handler);

    private void run() throws EventLoopSetupException {
        this.setup(this.dBusContextPointer);
        this.isClosed.set(false);
        this.startBarrier.countDown();

        while(!this.isClosed.get()){
            //add dispatchers
            while(!this.handlerAddingQueue.isEmpty()){
                Dispatcher d = this.handlerAddingQueue.poll();
                this.addPathHandler(this.dBusContextPointer,d.getPath(),d);
                this.inQueue.decrementAndGet();
            }

            //limit the number of send per tick to 100 (to avoid reading starvation)
            int i = 0;
            while(!this.signalSendingQueue.isEmpty() && i<SENDING_QUEUE_SIZE){
                AbstractSendingRequest abstarctRequest = this.signalSendingQueue.poll();
                if(abstarctRequest instanceof CallSendingRequest){
                    CallSendingRequest req = (CallSendingRequest)abstarctRequest;
                    this.sendCall(this.dBusContextPointer,req.getPath(),req.getInterfaceName(),req.getMember(),req.getMessage(),req.getDest(),req.getPendingCall());

                }else if(abstarctRequest instanceof ErrorReplySendingRequest){
                    ErrorReplySendingRequest req = (ErrorReplySendingRequest)abstarctRequest;
                    if(req.getError() instanceof DBusException){
                        DBusException cast = (DBusException)req.getError();
                        this.sendReplyError(this.dBusContextPointer,req.getMessagePointer(),cast.getCode(),cast.getMessage());
                    }else{
                        this.sendReplyError(this.dBusContextPointer,req.getMessagePointer(),req.getError().getClass().getName(),req.getError().getMessage());
                    }

                }else if(abstarctRequest instanceof ReplySendingRequest){
                    ReplySendingRequest req = (ReplySendingRequest)abstarctRequest;
                    this.sendReply(this.dBusContextPointer,req.getMessage(),req.getMessagePointer());

                }else if(abstarctRequest instanceof SignalSendingRequest){
                    SignalSendingRequest req = (SignalSendingRequest)abstarctRequest;
                    this.sendSignal(this.dBusContextPointer,req.getPath(),req.getInterfaceName(),req.getMember(),req.getMessage());
                }
                i++;
                this.inQueue.decrementAndGet();
            }

            //launch tick
            if(this.inQueue.get() > 0){
                this.tick(this.dBusContextPointer,0);
            }else{
                this.tick(this.dBusContextPointer,-1);
            }
        }

        this.connection.close();
    }

    public void addPathHandler(Dispatcher dispatcher){
        this.inQueue.incrementAndGet();
        if(this.checkEventLoop()){
            this.handlerAddingQueue.add(dispatcher);
            this.wakeup(this.dBusContextPointer);
        }
    }


    public void send(AbstractSendingRequest request){
        this.inQueue.incrementAndGet();
        if(this.checkEventLoop()){
            this.signalSendingQueue.add(request);
            this.wakeup(this.dBusContextPointer);
        }
    }

    private boolean checkEventLoop(){
        if(this.isClosed.get()){
            try { this.startBarrier.await(); } catch (InterruptedException e) { }
        }
        return !this.isClosed.get();
    }

    @Override
    public void close() throws IOException {
        try { this.startBarrier.await(); } catch (InterruptedException e) { }
        this.isClosed.set(true);
        this.wakeup(this.dBusContextPointer);
    }

}
