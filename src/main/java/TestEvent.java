import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

@DBusType(
        value = "si",
        fields = {"value1","value2"}
)
public class TestEvent extends Message {

    private String value1;
    private int value2;

    public TestEvent(){}
    public TestEvent(String value1, int value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getValue1() {
        return value1;
    }

    public int getValue2() {
        return value2;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public void setValue2(Integer value2) {
        this.value2 = value2;
    }
}