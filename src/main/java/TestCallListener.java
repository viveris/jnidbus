import fr.viveris.vizada.jnidbus.message.PendingCall;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCallListener implements PendingCall.Listener<TestReturn> {
    private AtomicInteger i = new AtomicInteger();

    @Override
    public void notify(TestReturn value) {
        this.i.incrementAndGet();
    }

    @Override
    public void error(Throwable t) {
        System.out.println(t);
    }

    public AtomicInteger get(){
        return this.i;
    }
}
