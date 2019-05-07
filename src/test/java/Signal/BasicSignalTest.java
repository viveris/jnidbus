package Signal;

import Common.DBusTestCase;
import fr.viveris.vizada.jnidbus.dispatching.Criteria;
import fr.viveris.vizada.jnidbus.dispatching.GenericHandler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.message.Signal;
import fr.viveris.vizada.jnidbus.serialization.DBusType;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BasicSignalTest extends DBusTestCase {
    public static String testString = "test";

    @Test
    public void emptySignal() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        this.sender.sendSignal(new EmptySignal());
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void signalWithWrongSignatureIsNotDispatched() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        StringMessage msg = new StringMessage();
        msg.setString(testString);
        this.sender.sendSignal(new StringSignalOnWrongEndpoint(msg));
        assertFalse(handler.barrier.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void signalIsSerializedAndUnserialized() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        StringMessage msg = new StringMessage();
        msg.setString(testString);
        this.sender.sendSignal(new StringSignal(msg));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
    }


    @Handler(
            path = "/Signal/BasicSignalTest",
            interfaceName = "Signal.BasicSignalTest"
    )
    public class SignalHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);

        @HandlerMethod(
                member = "emptySignal",
                type = Criteria.HandlerType.SIGNAL
        )
        public void emptySignal(Message.EmptyMessage emptyMessage){
            this.barrier.countDown();
        }

        @HandlerMethod(
                member = "stringSignal",
                type = Criteria.HandlerType.SIGNAL
        )
        public void stringSignal(StringMessage string){
            if(string.getString().equals(BasicSignalTest.testString)){
                this.barrier.countDown();
            }
        }
    }

    @DbusSignal(
            path = "/Signal/BasicSignalTest",
            interfaceName = "Signal.BasicSignalTest",
            member = "emptySignal"
    )
    public static class EmptySignal extends Signal<Message>{
        public EmptySignal() {
            super(Message.EMPTY);
        }
    }

    @DbusSignal(
            path = "/Signal/BasicSignalTest",
            interfaceName = "Signal.BasicSignalTest",
            member = "emptySignal"
    )
    public static class StringSignalOnWrongEndpoint extends Signal<StringMessage>{
        public StringSignalOnWrongEndpoint(StringMessage msg) {
            super(msg);
        }
    }

    @DbusSignal(
            path = "/Signal/BasicSignalTest",
            interfaceName = "Signal.BasicSignalTest",
            member = "stringSignal"
    )
    public static class StringSignal extends Signal<StringMessage>{
        public StringSignal(StringMessage msg) {
            super(msg);
        }
    }

    @DBusType(
            value = "s",
            fields = "string"
    )
    public static class StringMessage extends Message{
        private String string;

        public String getString() {
            return string;
        }
        public void setString(String string) {
            this.string = string;
        }
    }
}
