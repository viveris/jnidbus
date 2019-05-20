package Common.DBusObjects;

import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.serialization.DBusType;

@DBusType(
        signature = "s",
        fields = "string"
)
public class SingleStringMessage extends Message {
    private String string;

    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }
}
