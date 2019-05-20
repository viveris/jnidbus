package Call;

import Common.DBusObjects.SingleStringMessage;
import Common.DBusTestCase;
import Common.Listener;
import fr.viveris.jnidbus.dispatching.GenericHandler;
import fr.viveris.jnidbus.dispatching.HandlerType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.message.*;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;

public class AsyncCallTest extends DBusTestCase {

    @Test
    public void asyncBlockingCall() throws InterruptedException {
        CallHandler handler = new CallHandler();
        this.receiver.addHandler(handler);
        PendingCall<Message.EmptyMessage> pending = this.sender.call(new EmptyCall(this.receiverBusName));
        Listener<Message.EmptyMessage> l = new Listener<>();
        pending.setListener(l);
        assertTrue(handler.barrier.await(5, TimeUnit.SECONDS));
        assertTrue(l.getBarrier().await(5, TimeUnit.SECONDS));
        assertEquals(Message.EMPTY,l.getValue());
        assertNull(l.getT());
    }

    @Test
    public void asyncBlockingCallDoNotBlockEventLoop() throws InterruptedException {
        CallHandler handler = new CallHandler();
        this.receiver.addHandler(handler);
        PendingCall<Message.EmptyMessage> pendingEmpty = this.sender.call(new EmptyCall(this.receiverBusName));
        PendingCall<SingleStringMessage> pendingString = this.sender.call(new AsyncCallTest.StringCall(this.receiverBusName));
        Listener<Message.EmptyMessage> lEmpty = new Listener<>();
        Listener<SingleStringMessage> lString = new Listener<>();
        pendingEmpty.setListener(lEmpty);
        pendingString.setListener(lString);

        assertTrue(lString.getBarrier().await(1, TimeUnit.SECONDS));
        assertTrue(lEmpty.getBarrier().await(5, TimeUnit.SECONDS));
        assertEquals(Message.EMPTY,lEmpty.getValue());
        assertEquals("test",lString.getValue().getString());
    }

    @Handler(
            path = "/Call/AsyncCallTest",
            interfaceName = "Call.AsyncCallTest"
    )
    public class CallHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);

        @HandlerMethod(
                member = "blockingCall",
                type = HandlerType.METHOD
        )
        public Promise<Message.EmptyMessage> blockingCall(Message.EmptyMessage emptyMessage){
            final Promise<Message.EmptyMessage> promise = new Promise<>();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //sleep
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //unlock barrier and resolve promise
                    CallHandler.this.barrier.countDown();
                    promise.resolve(Message.EMPTY);
                }
            }).start();
            return promise;
        }

        @HandlerMethod(
                member = "instantReturn",
                type = HandlerType.METHOD
        )
        public Promise<SingleStringMessage> instantReturn(Message.EmptyMessage msg){
            Boolean b = true;
            Promise<SingleStringMessage> promise = new Promise<>();
            this.barrier.countDown();
            SingleStringMessage ret = new SingleStringMessage();
            ret.setString("test");
            promise.resolve(ret);
            return promise;
        }
    }

    @DbusMethodCall(
            //as destination is dynamic, we override the getDestination method instead of using the annotation
            destination = "",
            path = "/Call/AsyncCallTest",
            interfaceName = "Call.AsyncCallTest",
            member = "blockingCall"

    )
    public static class EmptyCall extends Call<Message.EmptyMessage,Message.EmptyMessage> {
        private String dest;
        public EmptyCall(String dest) {
            super(Message.EMPTY,Message.EmptyMessage.class);
            this.dest = dest;
        }

        @Override
        public String getDestination() {
            return this.dest;
        }
    }

    @DbusMethodCall(
            //as destination is dynamic, we override the getDestination method instead of using the annotation
            destination = "",
            path = "/Call/AsyncCallTest",
            interfaceName = "Call.AsyncCallTest",
            member = "instantReturn"

    )
    public static class StringCall extends Call<Message.EmptyMessage, SingleStringMessage> {
        private String dest;
        public StringCall(String dest) {
            super(Message.EMPTY, SingleStringMessage.class);
            this.dest = dest;
        }

        @Override
        public String getDestination() {
            return this.dest;
        }
    }
}
