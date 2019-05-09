package Common;

import fr.viveris.vizada.jnidbus.exception.DBusException;
import fr.viveris.vizada.jnidbus.message.PendingCall;

import java.util.concurrent.CountDownLatch;

public class Listener<T> implements PendingCall.Listener<T>{
    private CountDownLatch barrier = new CountDownLatch(1);
    private DBusException t = null;
    private T value = null;
    @Override
    public void notify(T value) {
        this.value = value;
        barrier.countDown();
    }

    @Override
    public void error(DBusException t) {
        this.t = t;
        barrier.countDown();
    }

    public CountDownLatch getBarrier() {
        return barrier;
    }

    public DBusException getT() {
        return t;
    }

    public T getValue() {
        return value;
    }
}
