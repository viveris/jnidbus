package Common;

import fr.viveris.vizada.jnidbus.BusType;
import fr.viveris.vizada.jnidbus.Dbus;
import fr.viveris.vizada.jnidbus.exception.ConnectionException;
import org.junit.After;
import org.junit.Before;

import java.security.SecureRandom;
import java.util.Random;

public abstract class DBusTestCase {
    protected Dbus sender;
    protected Dbus receiver;

    protected String senderBusName;
    protected String receiverBusName;

    /**
     * We have to setup a new connection for each bus in for more safety, in addition we must make the bus name
     * unique as DBus needs a few millis to free a bus name, meaning each test would be delayed, which is not acceptable
     */
    @Before
    public void setup() throws ConnectionException {
        SecureRandom random = new SecureRandom();
        this.senderBusName = "fr.viveris.vizada.jnidbus.test.sender."+generateRandomString();
        this.receiverBusName = "fr.viveris.vizada.jnidbus.test.sender."+generateRandomString();
        this.sender = new Dbus(BusType.SESSION,this.senderBusName);
        this.receiver = new Dbus(BusType.SESSION,this.receiverBusName);
    }

    @After
    public void dispose() {
        try{
            this.sender.close();
            this.receiver.close();
        }catch (Exception e){ }
    }

    /**
     * Don't ask me
     * @return
     */
    private static String generateRandomString(){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}
