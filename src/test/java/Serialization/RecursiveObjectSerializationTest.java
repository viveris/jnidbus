package Serialization;

import Common.DBusTestCase;
import Common.DBusObjects.ArrayRecursiveObject;
import Common.DBusObjects.RecursiveObject;
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

    @Test
    public void recursiveNestedObject() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        ArrayRecursiveObject typeObject = new ArrayRecursiveObject();

        //test Java serialization
        ArrayRecursiveObject.SubArrayRecursiveObject subObject1 = new ArrayRecursiveObject.SubArrayRecursiveObject();
        ArrayRecursiveObject.SubArrayRecursiveObject subObject2 = new ArrayRecursiveObject.SubArrayRecursiveObject();
        subObject1.setInteger(42);
        subObject2.setInteger(24);
        subObject1.getStrings().add("test1");
        subObject2.getStrings().add("test2");
        typeObject.getObjects().add(subObject1);
        typeObject.getObjects().add(subObject2);
        DBusObject obj = typeObject.serialize();
        DBusObject subObj1 = (DBusObject)((Object[])obj.getValues()[0])[0];
        DBusObject subObj2 = (DBusObject)((Object[])obj.getValues()[0])[1];

        assertEquals("a(asi)",obj.getSignature());
        assertEquals(42,subObj1.getValues()[1]);
        assertEquals(24,subObj2.getValues()[1]);
        assertEquals("test1",((Object[])subObj1.getValues()[0])[0]);
        assertEquals("test2",((Object[])subObj2.getValues()[0])[0]);
        assertEquals("asi",subObj1.getSignature());
        assertEquals("asi",subObj2.getSignature());

        //send signal, which test JNI and Java unserialization
        this.sender.sendSignal(new ArrayRecursiveObjectSignal(typeObject));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        ArrayRecursiveObject received = handler.arrayRecursiveObject;
        assertEquals(2,received.getObjects().size());
        assertEquals(42,received.getObjects().get(0).getInteger());
        assertEquals(24,received.getObjects().get(1).getInteger());
        assertEquals("test1",received.getObjects().get(0).getStrings().get(0));
        assertEquals("test2",received.getObjects().get(1).getStrings().get(0));
    }

    @Handler(
            path = "/Serialization/RecursiveObjectSerializationTest",
            interfaceName = "Serialization.RecursiveObjectSerializationTest"
    )
    public class SignalHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);
        private RecursiveObject recursiveObject;
        private ArrayRecursiveObject arrayRecursiveObject;

        @HandlerMethod(
                member = "recursiveObject",
                type = Criteria.HandlerType.SIGNAL
        )
        public void recursiveObject(RecursiveObject signal){
            this.recursiveObject = signal;
            this.barrier.countDown();
        }

        @HandlerMethod(
                member = "arrayRecursiveObject",
                type = Criteria.HandlerType.SIGNAL
        )
        public void arrayRecursiveObject(ArrayRecursiveObject signal){
            this.arrayRecursiveObject = signal;
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

    @DbusSignal(
            path = "/Serialization/RecursiveObjectSerializationTest",
            interfaceName = "Serialization.RecursiveObjectSerializationTest",
            member = "arrayRecursiveObject"
    )
    public static class ArrayRecursiveObjectSignal extends Signal<ArrayRecursiveObject> {
        public ArrayRecursiveObjectSignal(ArrayRecursiveObject msg) {
            super(msg);
        }
    }
}
