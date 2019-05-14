package Common.DBusObjects;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

@DBusType(
        signature = "s",
        fields = "string"
)
public class StringMessage extends Message {
    private String string;

    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }
}
