package benchmarks;

import Common.DBusTestCase;
import fr.viveris.vizada.jnidbus.BusType;
import fr.viveris.vizada.jnidbus.Dbus;
import fr.viveris.vizada.jnidbus.bindings.bus.EventLoop;
import fr.viveris.vizada.jnidbus.dispatching.Criteria;
import fr.viveris.vizada.jnidbus.dispatching.GenericHandler;
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

    @Benchmark
    @OperationsPerInvocation(EventLoop.SENDING_QUEUE_SIZE)
    public void singleThreadSendReceive() throws InterruptedException {
        this.handler.latch = new CountDownLatch(EventLoop.SENDING_QUEUE_SIZE);
        for(int i = 0; i < EventLoop.SENDING_QUEUE_SIZE; i++){
            try{
                this.sender.sendSignal(new EmptySignal());
            }catch (Exception e){
                System.out.println("AH!");
            }
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
                type = Criteria.HandlerType.SIGNAL
        )
        public void emptyMessage(Message.EmptyMessage emptyMessage){
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
}
