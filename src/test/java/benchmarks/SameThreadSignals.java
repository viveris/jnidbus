package benchmarks;

import Common.DBusObjects.ArrayRecursiveObject;
import Common.DBusObjects.SimpleMessage;
import Common.DBusTestCase;
import fr.viveris.vizada.jnidbus.BusType;
import fr.viveris.vizada.jnidbus.Dbus;
import fr.viveris.vizada.jnidbus.bindings.bus.EventLoop;
import fr.viveris.vizada.jnidbus.dispatching.Criteria;
import fr.viveris.vizada.jnidbus.dispatching.GenericHandler;
import fr.viveris.vizada.jnidbus.dispatching.HandlerType;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.exception.ConnectionException;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.message.Signal;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(time = 3)
@Fork(2)
public class SameThreadSignals {
    private Dbus sender;
    private String busName;
    private SignalHandler handler;

    public SameThreadSignals(){
        this.busName = "fr.viveris.vizada.jnidbus.benchmarks.SingleThreadSignals."+ DBusTestCase.generateRandomString();
        try {
            this.sender = new Dbus(BusType.SESSION,this.busName);
            this.handler = new SignalHandler();
            sender.addMessageHandler(this.handler);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    @TearDown
    public void teardown() throws Exception {
        this.sender.close();
    }

    @Benchmark
    @OperationsPerInvocation(EventLoop.SENDING_QUEUE_SIZE)
    public void singleThreadSendReceiveEmpty() throws InterruptedException {
        this.handler.latch = new CountDownLatch(EventLoop.SENDING_QUEUE_SIZE);
        for(int i = 0; i < EventLoop.SENDING_QUEUE_SIZE; i++){
            this.sender.sendSignal(new EmptySignal());
        }
        this.handler.latch.await();
    }

    @Benchmark
    @OperationsPerInvocation(EventLoop.SENDING_QUEUE_SIZE)
    public void singleThreadSendReceiveSimple() throws InterruptedException {
        this.handler.latch = new CountDownLatch(EventLoop.SENDING_QUEUE_SIZE);
        SimpleMessage msg = new SimpleMessage();
        msg.setInt1(45000);
        msg.setInt2(684000);
        msg.setString1("string 1");
        msg.setString2("string 2");
        for(int i = 0; i < EventLoop.SENDING_QUEUE_SIZE; i++){
            this.sender.sendSignal(new SimpleSignal(msg));
        }
        this.handler.latch.await();
    }

    @Benchmark
    @OperationsPerInvocation(EventLoop.SENDING_QUEUE_SIZE)
    public void singleThreadSendReceiveComplex() throws InterruptedException {
        this.handler.latch = new CountDownLatch(EventLoop.SENDING_QUEUE_SIZE);

        ArrayRecursiveObject obj = new ArrayRecursiveObject();
        ArrayRecursiveObject.SubArrayRecursiveObject sub1 = new ArrayRecursiveObject.SubArrayRecursiveObject();
        ArrayRecursiveObject.SubArrayRecursiveObject sub2 = new ArrayRecursiveObject.SubArrayRecursiveObject();
        sub1.setInteger(42);
        sub1.getStrings().add("comp1");
        sub1.getStrings().add("comp2");
        sub1.getStrings().add("comp3");
        sub2.setInteger(24);
        sub2.getStrings().add("comp11");
        sub2.getStrings().add("comp21");
        obj.getObjects().add(sub1);
        obj.getObjects().add(sub2);

        for(int i = 0; i < EventLoop.SENDING_QUEUE_SIZE; i++){
            this.sender.sendSignal(new ComplexSignal(obj));
        }
        this.handler.latch.await();
    }

    @Handler(
            path = "/Benchmarks/SingleThreadSignals",
            interfaceName = "Benchmarks.SingleThreadSignals"
    )
    public class SignalHandler extends GenericHandler{
        CountDownLatch latch;

        @HandlerMethod(
                member = "emptyMessage",
                type = HandlerType.SIGNAL
        )
        public void emptyMessage(Message.EmptyMessage emptyMessage){
            this.latch.countDown();
        }

        @HandlerMethod(
                member = "complexMessage",
                type = HandlerType.SIGNAL
        )
        public void complexMessage(ArrayRecursiveObject complexMessage){
            this.latch.countDown();
        }

        @HandlerMethod(
                member = "simpleMessage",
                type = HandlerType.SIGNAL
        )
        public void simpleMessage(SimpleMessage simpleMessage){
            this.latch.countDown();
        }
    }

    @DbusSignal(
            path = "/Benchmarks/SingleThreadSignals",
            interfaceName = "Benchmarks.SingleThreadSignals",
            member = "emptyMessage"
    )
    public class EmptySignal extends Signal<Message.EmptyMessage>{
        public EmptySignal() {
            super(Message.EMPTY);
        }
    }

    @DbusSignal(
            path = "/Benchmarks/SingleThreadSignals",
            interfaceName = "Benchmarks.SingleThreadSignals",
            member = "complexMessage"
    )
    public class ComplexSignal extends Signal<ArrayRecursiveObject>{
        public ComplexSignal(ArrayRecursiveObject msg) {
            super(msg);
        }
    }

    @DbusSignal(
            path = "/Benchmarks/SingleThreadSignals",
            interfaceName = "Benchmarks.SingleThreadSignals",
            member = "simpleMessage"
    )
    public class SimpleSignal extends Signal<SimpleMessage>{
        public SimpleSignal(SimpleMessage msg) {
            super(msg);
        }
    }
}
