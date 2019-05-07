import fr.viveris.vizada.jnidbus.*;
import fr.viveris.vizada.jnidbus.message.PendingCall;

public class Main {

    static{
        System.load("/home/lucas/IdeaProjects/jnidbus/src/main/jni/libjnidbus.so");
    }

    public static void main(String[] args) throws Exception {
        Dbus sender = new Dbus(BusType.SESSION,"fr.viveris.vizada.jnidbus.sender");
        Dbus receiver = new Dbus(BusType.SESSION,"fr.viveris.vizada.jnidbus.receiver");
        TestHandler handler = new TestHandler();
        TestCallListener listener = new TestCallListener();
        receiver.addMessageHandler(handler);


        sender.sendSignal("/test/test","test.test.Interface","testMember",new TestEvent("azeaze",42));
        PendingCall<TestReturn> pending = sender.call(new TestCall(new TestEvent("azeaze",42)));
        pending.setListener(listener);

        /*long previous = System.currentTimeMillis();
        while(true){
            try{
                PendingCall<TestReturn> pending = sender.call(new TestCall());
                PendingCall<TestReturn> pending = sender.call(new TestCall());
                pending.setListener(listener);
                sender.sendSignal("/test/test","test.test.Interface","testMember",new TestEvent("azeaze",42));
            }catch (Exception e){
            }
            Thread.sleep(1);
            if(previous+1000<System.currentTimeMillis()){
                System.out.println("calls recevied in 1 sec: "+listener.get().intValue());
                listener.get().set(0);
                previous = System.currentTimeMillis();
            }
        }*/

        Thread.sleep(5000);
        sender.close();
        receiver.close();
    }
}

