import fr.viveris.vizada.jnidbus.dispatching.Criteria;
import fr.viveris.vizada.jnidbus.dispatching.GenericHandler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;

import java.util.concurrent.atomic.AtomicInteger;

@Handler(
        path = "/test/test",
        interfaceName = "test.test.Interface"
)
public class TestHandler extends GenericHandler {

    private AtomicInteger counter = new AtomicInteger();

    @HandlerMethod(
            member = "testMember",
            type = Criteria.HandlerType.SIGNAL
    )
    public void handle(TestEvent event){
        this.counter.incrementAndGet();
    }

    @HandlerMethod(
            member = "testMember",
            type = Criteria.HandlerType.METHOD
    )
    public TestReturn handleCall(TestEvent event){
        return new TestReturn("ah!");
    }

    public int get(){
        return counter.get();
    }

    public void reset(){
        this.counter.set(0);
    }
}
