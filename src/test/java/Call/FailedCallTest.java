package Call;

import Common.DBusTestCase;
import Common.Listener;
import Common.DBusObjects.SingleStringMessage;
import fr.viveris.jnidbus.dispatching.GenericHandler;
import fr.viveris.jnidbus.dispatching.HandlerType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.exception.DBusException;
import fr.viveris.jnidbus.exception.MessageSignatureMismatch;
import fr.viveris.jnidbus.message.Call;
import fr.viveris.jnidbus.message.DbusMethodCall;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.message.PendingCall;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;

public class FailedCallTest extends DBusTestCase {
    @Test
    public void callOnNonExistentMethod() throws InterruptedException {
        CallHandler handler = new CallHandler();
        this.receiver.addHandler(handler);
        PendingCall<Message.EmptyMessage> pending = this.sender.call(new UnknownCall(this.receiverBusName));
        Listener<Message.EmptyMessage> l = new Listener<>();
        pending.setListener(l);
        assertFalse(handler.barrier.await(2, TimeUnit.SECONDS));
        assertTrue(l.getBarrier().await(2, TimeUnit.SECONDS));
        assertNull(l.getValue());
        assertNotNull(l.getT());
        assertEquals(DBusException.METHOD_NOT_FOUND_CODE,l.getT().getCode());
    }

    @Test
    public void callReturnsWrongSignature() throws InterruptedException {
        CallHandler handler = new CallHandler();
        this.receiver.addHandler(handler);
        PendingCall<SingleStringMessage> pending = this.sender.call(new MismatchCall(this.receiverBusName));
        Listener<SingleStringMessage> l = new Listener<>();
        pending.setListener(l);
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        assertTrue(l.getBarrier().await(2, TimeUnit.SECONDS));
        assertNull(l.getValue());
        assertNotNull(l.getT());
        assertEquals(MessageSignatureMismatch.class.getName(),l.getT().getCode());
    }

    @Test
    public void callReturnsError() throws InterruptedException {
        CallHandler handler = new CallHandler();
        this.receiver.addHandler(handler);
        PendingCall<Message.EmptyMessage> pending = this.sender.call(new FailCall(this.receiverBusName));
        Listener<Message.EmptyMessage> l = new Listener<>();
        pending.setListener(l);
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        assertTrue(l.getBarrier().await(2, TimeUnit.SECONDS));
        assertNull(l.getValue());
        assertNotNull(l.getT());
        assertEquals("test.error.code",l.getT().getCode());
        assertEquals("TestMessage",l.getT().getMessage());
    }

    @Handler(
            path = "/Call/FailedCallTest",
            interfaceName = "Call.FailedCallTest"
    )
    public class CallHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);

        @HandlerMethod(
                member = "mismatchCall",
                type = HandlerType.METHOD
        )
        public Message.EmptyMessage mismatchCall(Message.EmptyMessage emptyMessage){
            this.barrier.countDown();
            return Message.EMPTY;
        }

        @HandlerMethod(
                member = "failCall",
                type = HandlerType.METHOD
        )
        public Message.EmptyMessage failCall(Message.EmptyMessage emptyMessage) throws DBusException {
            this.barrier.countDown();
            throw new DBusException("test.error.code","TestMessage");
        }
    }

    @DbusMethodCall(
            //as destination is dynamic, we override the getDestination method instead of using the annotation
            destination = "",
            path = "/Call/FailedCallTest",
            interfaceName = "Call.FailedCallTest",
            member = "unknownCall"

    )
    public static class UnknownCall extends Call<Message.EmptyMessage,Message.EmptyMessage> {
        private String dest;
        public UnknownCall(String dest) {
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
            path = "/Call/FailedCallTest",
            interfaceName = "Call.FailedCallTest",
            member = "mismatchCall"

    )
    public static class MismatchCall extends Call<Message.EmptyMessage, SingleStringMessage> {
        private String dest;
        public MismatchCall(String dest) {
            super(Message.EMPTY, SingleStringMessage.class);
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
            path = "/Call/FailedCallTest",
            interfaceName = "Call.FailedCallTest",
            member = "failCall"

    )
    public static class FailCall extends Call<Message.EmptyMessage,Message.EmptyMessage> {
        private String dest;
        public FailCall(String dest) {
            super(Message.EMPTY, Message.EmptyMessage.class);
            this.dest = dest;
        }

        @Override
        public String getDestination() {
            return this.dest;
        }
    }
}
