import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

@DBusType(
        value = "s",
        fields = "string"
)
public class TestReturn extends Message {
    private String string;

    public TestReturn(){}
    public TestReturn(String value){ this.string = value; }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
