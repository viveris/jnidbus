package Serialization;

import Common.DBusTestCase;
import Serialization.DBusObjects.CollectionArray;
import Serialization.DBusObjects.CollectionOfCollectionArray;
import fr.viveris.vizada.jnidbus.dispatching.Criteria;
import fr.viveris.vizada.jnidbus.dispatching.GenericHandler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;
import fr.viveris.vizada.jnidbus.serialization.DBusObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ArraySerializationTest extends DBusTestCase {

    @Test
    public void collectionOfPrimitiveSerializationAndUnserialization() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        CollectionArray typeObject = new CollectionArray();

        //test empty collection is correctly serialized
        DBusObject obj = typeObject.serialize();
        assertEquals("ai",obj.getSignature());
        assertTrue(obj.getValues()[0] instanceof Object[]);
        assertEquals(0,((Object[])obj.getValues()[0]).length);

        //test if JNI code can as well
        this.sender.sendSignal(new CollectionArraySignal(typeObject));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        CollectionArray received = handler.collectionArraySignal;
        handler.barrier = new CountDownLatch(1);
        assertNotNull(receiverBusName);
        assertEquals(0,received.getArray().size());

        //test non-empty collection is correctly serialized
        typeObject.getArray().add(42);
        obj = typeObject.serialize();
        assertEquals("ai",obj.getSignature());
        assertTrue(obj.getValues()[0] instanceof Object[]);
        assertEquals(1,((Object[])obj.getValues()[0]).length);
        assertEquals(42,((Object[])obj.getValues()[0])[0]);

        //test if JNI code can as well
        this.sender.sendSignal(new CollectionArraySignal(typeObject));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        received = handler.collectionArraySignal;
        handler.barrier = new CountDownLatch(1);
        assertNotNull(receiverBusName);
        assertEquals(1,received.getArray().size());
        assertEquals(42,received.getArray().get(0).intValue());
    }

    @Test
    public void recursiveCollectionOfPrimitiveSerializationAndUnserialization() throws InterruptedException {
        SignalHandler handler = new SignalHandler();
        this.receiver.addMessageHandler(handler);
        CollectionOfCollectionArray typeObject = new CollectionOfCollectionArray();

        //test empty collection is correctly serialized
        DBusObject obj = typeObject.serialize();
        assertEquals("aai",obj.getSignature());
        assertTrue(obj.getValues()[0] instanceof Object[]);
        assertEquals(0,((Object[])obj.getValues()[0]).length);

        //test JNI code
        sender.sendSignal(new CollectionOfCollectionArraySignal(typeObject));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        CollectionOfCollectionArray received = handler.collectionOfCollectionArraySignal;
        handler.barrier = new CountDownLatch(1);
        assertNotNull(receiverBusName);
        assertEquals(0,received.getArray().size());

        //test non-empty collection is correctly serialized
        ArrayList<Integer> ints = new ArrayList<>();
        ints.add(42);
        typeObject.getArray().add(ints);
        obj = typeObject.serialize();
        assertEquals("aai",obj.getSignature());
        assertTrue(obj.getValues()[0] instanceof Object[]);
        assertEquals(1,((Object[])obj.getValues()[0]).length);
        Object[] recursiveArray = (Object[])obj.getValues()[0];
        assertEquals(1,((Object[])recursiveArray[0]).length);
        assertEquals(42,((Object[])recursiveArray[0])[0]);

        //test if JNI code can as well
        this.sender.sendSignal(new CollectionOfCollectionArraySignal(typeObject));
        assertTrue(handler.barrier.await(2, TimeUnit.SECONDS));
        received = handler.collectionOfCollectionArraySignal;
        handler.barrier = new CountDownLatch(1);
        assertNotNull(receiverBusName);
        assertEquals(1,received.getArray().size());
        assertEquals(1,received.getArray().get(0).size());
        assertEquals(42,received.getArray().get(0).get(0).intValue());


    }

    @Handler(
            path = "/Serialization/ArraySerializationTest",
            interfaceName = "Serialization.ArraySerializationTest"
    )
    public class SignalHandler extends GenericHandler {
        private CountDownLatch barrier = new CountDownLatch(1);
        private CollectionArray collectionArraySignal;
        private CollectionOfCollectionArray collectionOfCollectionArraySignal;

        @HandlerMethod(
                member = "collectionArray",
                type = Criteria.HandlerType.SIGNAL
        )
        public void collectionArray(CollectionArray signal){
            this.collectionArraySignal = signal;
            this.barrier.countDown();
        }

        @HandlerMethod(
                member = "collectionOfCollectionArraySignal",
                type = Criteria.HandlerType.SIGNAL
        )
        public void collectionOfCollectionArraySignal(CollectionOfCollectionArray signal){
            this.collectionOfCollectionArraySignal = signal;
            this.barrier.countDown();
        }
    }

    @DbusSignal(
            path = "/Serialization/ArraySerializationTest",
            interfaceName = "Serialization.ArraySerializationTest",
            member = "collectionArray"
    )
    public static class CollectionArraySignal extends Signal<CollectionArray> {
        public CollectionArraySignal(CollectionArray msg) {
            super(msg);
        }
    }

    @DbusSignal(
            path = "/Serialization/ArraySerializationTest",
            interfaceName = "Serialization.ArraySerializationTest",
            member = "collectionOfCollectionArraySignal"
    )
    public static class CollectionOfCollectionArraySignal extends Signal<CollectionOfCollectionArray> {
        public CollectionOfCollectionArraySignal(CollectionOfCollectionArray msg) {
            super(msg);
        }
    }
}