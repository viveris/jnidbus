package fr.viveris.vizada.jnidbus.bindings.bus;

import fr.viveris.vizada.jnidbus.message.sendingrequest.*;
import fr.viveris.vizada.jnidbus.dispatching.Dispatcher;
import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.message.PendingCall;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventLoop implements Closeable {

    private long dBusContextPointer;

    private AtomicBoolean isClosed = new AtomicBoolean(true);

    /**
     * Thread running the event loop
     */
    private Thread thread;

    /**
     * Store the matching rules we want to register
     */
    private LinkedBlockingDeque<Dispatcher> handlerAddingQueue = new LinkedBlockingDeque<>();

    private LinkedBlockingDeque<AbstractSendingRequest> signalSendingQueue = new LinkedBlockingDeque<>(100);

    private Connection connection;

    //used to ensure the vent loop fully started before doing anything
    private CountDownLatch startBarrier = new CountDownLatch(1);

    public EventLoop(Connection connection){
        this.connection = connection;
        this.dBusContextPointer = connection.getdBusContextPointer();
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoop.this.run();
            }
        });
        this.thread.setName("DBus event loop");
        this.thread.start();
    }

    /**
     * This method will take care of setting up epoll and all the timeout and watch functions of dbus.
     */
    private native boolean setup(long contextPtr);

    /**
     * Will call epoll, wait for events and dispatch those events to dbus. we can interupt an ongoing tick by calling the wakeup() function.
     */
    private native void tick(long contextPtr);

    /**
     * Will send a byte on the wakeup pipe, which will make epoll_wait return, making the ongoing tick stop
     */
    public native void wakeup(long contextPtr);

    private native void sendReply(long contextPtr, Message msg, long msgPointer);
    private native void sendReplyError(long contextPtr, long msgPointer, String errorName, String errorMessage);
    private native void sendSignal(long contextPtr, String path, String interfaceName, String member, Message msg);
    private native void sendCall(long contextPtr, String path, String interfaceName, String member, Message msg, String dest, PendingCall pendingCall);

    private native void addPathHandler(long contextPtr, String path, Dispatcher handler);

    private void run(){
        this.setup(this.dBusContextPointer);
        this.isClosed.set(false);
        this.startBarrier.countDown();

        while(!this.isClosed.get()){
            //add dispatchers
            while(!this.handlerAddingQueue.isEmpty()){
                Dispatcher d = this.handlerAddingQueue.poll();
                this.addPathHandler(this.dBusContextPointer,d.getPath(),d);
            }

            //limit the number of send per tick to 100 (to avoid reading starvation)
            int i = 0;
            while(!this.signalSendingQueue.isEmpty() && i<100){
                AbstractSendingRequest abstarctRequest = this.signalSendingQueue.poll();
                if(abstarctRequest instanceof CallSendingRequest){
                    CallSendingRequest req = (CallSendingRequest)abstarctRequest;
                    this.sendCall(this.dBusContextPointer,req.getPath(),req.getInterfaceName(),req.getMember(),req.getMessage(),req.getDest(),req.getPendingCall());
                }else if(abstarctRequest instanceof ErrorReplySendingRequest){
                    ErrorReplySendingRequest req = (ErrorReplySendingRequest)abstarctRequest;
                    this.sendReplyError(this.dBusContextPointer,req.getMessagePointer(),req.getError().getClass().getName(),req.getError().getMessage());
                }else if(abstarctRequest instanceof ReplySendingRequest){
                    ReplySendingRequest req = (ReplySendingRequest)abstarctRequest;
                    this.sendReply(this.dBusContextPointer,req.getMessage(),req.getMessagePointer());
                }else if(abstarctRequest instanceof SignalSendingRequest){
                    SignalSendingRequest req = (SignalSendingRequest)abstarctRequest;
                    this.sendSignal(this.dBusContextPointer,req.getPath(),req.getInterfaceName(),req.getMember(),req.getMessage());
                }
                i++;
            }

            //launch tick
            this.tick(this.dBusContextPointer);
        }

        this.connection.close();
    }

    public void addPathHandler(Dispatcher dispatcher){
        try { this.startBarrier.await(); } catch (InterruptedException e) { }
        this.handlerAddingQueue.add(dispatcher);
        this.wakeup(this.dBusContextPointer);
    }


    public void send(AbstractSendingRequest request){
        this.signalSendingQueue.add(request);
        this.wakeup(this.dBusContextPointer);
    }

    @Override
    public void close() throws IOException {
        try { this.startBarrier.await(); } catch (InterruptedException e) { }
        this.isClosed.set(true);
        this.wakeup(this.dBusContextPointer);
    }

}
