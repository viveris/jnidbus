package Serialization;

import Common.DBusTestCase;
import Serialization.DBusObjects.RecursiveObject;
import fr.viveris.vizada.jnidbus.dispatching.Criteria;
import fr.viveris.vizada.jnidbus.dispatching.GenericHandler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;
import fr.viveris.vizada.jnidbus.serialization.DBusObject;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecursiveObjectSerializationTest extends DBusTestCase {

    @Test
    public void nestedObjectTest() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        RecursiveObject typeObject = new RecursiveObject();

        //test Java serialization
        typeObject.setInteger(42);
        typeObject.setString("test1");
        typeObject.getObject().setString("test2");
        DBusObject obj = typeObject.serialize();
        assertEquals("i(s)s",obj.getSignature());
        assertEquals(42,obj.getValues()[0]);
        assertEquals("test1",obj.getValues()[2]);
        assertEquals("s",((DBusObject)obj.getValues()[1]).getSignature());
        assertEquals("test2",((DBusObject)obj.getValues()[1]).getValues()[0]);

        //send signal, which test JNI and Java unserialization
        this.sender.sendSignal(new RecursiveObjectSignal(typeObject));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        RecursiveObject received = handler.recursiveObject;
        assertEquals(42,received.getInteger());
        assertEquals("test1",received.getString());
        assertEquals("test2",received.getObject().getString());
    }

    @Handler(
            path = "/Serialization/RecursiveObjectSerializationTest",
            interfaceName = "Serialization.RecursiveObjectSerializationTest"
    )
    public class SignalHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);
        private RecursiveObject recursiveObject;

        @HandlerMethod(
                member = "recursiveObject",
                type = Criteria.HandlerType.SIGNAL
        )
        public void recursiveObject(RecursiveObject signal){
            this.recursiveObject = signal;
            this.barrier.countDown();
        }
    }

    @DbusSignal(
            path = "/Serialization/RecursiveObjectSerializationTest",
            interfaceName = "Serialization.RecursiveObjectSerializationTest",
            member = "recursiveObject"
    )
    public static class RecursiveObjectSignal extends Signal<RecursiveObject> {
        public RecursiveObjectSignal(RecursiveObject msg) {
            super(msg);
        }
    }
}
