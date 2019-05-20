package Call;

import Common.DBusTestCase;
import Common.Listener;
import Common.DBusObjects.SingleStringMessage;
import fr.viveris.jnidbus.dispatching.GenericHandler;
import fr.viveris.jnidbus.dispatching.HandlerType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.message.Call;
import fr.viveris.jnidbus.message.DbusMethodCall;
import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.message.PendingCall;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;

public class BasicCallTest extends DBusTestCase {

    @Test
    public void emptyCall() throws InterruptedException {
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
    public void callIsSerializedAndUnserialized() throws InterruptedException {
        CallHandler handler = new CallHandler();
        this.receiver.addHandler(handler);
        SingleStringMessage msg = new SingleStringMessage();
        msg.setString("test");
        PendingCall<SingleStringMessage> pending = this.sender.call(new StringCall(this.receiverBusName,msg));
        Listener<SingleStringMessage> l = new Listener<>();
        pending.setListener(l);
        assertTrue(handler.barrier.await(5, TimeUnit.SECONDS));
        assertTrue(l.getBarrier().await(5, TimeUnit.SECONDS));
        assertEquals(msg.getString(),l.getValue().getString());
        assertNull(l.getT());
    }

    @Handler(
            path = "/Call/BasicCallTest",
            interfaceName = "Call.BasicCallTest"
    )
    public class CallHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);

        @HandlerMethod(
                member = "emptyCall",
                type = HandlerType.METHOD
        )
        public Message.EmptyMessage emptyCall(Message.EmptyMessage emptyMessage){
            this.barrier.countDown();
            return Message.EMPTY;
        }

        @HandlerMethod(
                member = "stringCall",
                type = HandlerType.METHOD
        )
        public SingleStringMessage stringCall(SingleStringMessage msg){
            this.barrier.countDown();
            SingleStringMessage ret = new SingleStringMessage();
            ret.setString(msg.getString());
            return ret;
        }
    }

    @DbusMethodCall(
            //as destination is dynamic, we override the getDestination method instead of using the annotation
            destination = "",
            path = "/Call/BasicCallTest",
            interfaceName = "Call.BasicCallTest",
            member = "emptyCall"

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
            path = "/Call/BasicCallTest",
            interfaceName = "Call.BasicCallTest",
            member = "stringCall"

    )
    public static class StringCall extends Call<SingleStringMessage, SingleStringMessage> {
        private String dest;
        public StringCall(String dest, SingleStringMessage msg) {
            super(msg, SingleStringMessage.class);
            this.dest = dest;
        }

        @Override
        public String getDestination() {
            return this.dest;
        }
    }
}
