package Serialization.DBusObjects;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

@DBusType(
        value = "i(s)s",
        fields = {"integer","object","string"}
)
public class RecursiveObject extends Message {
    private int integer;
    private SubObject object = new SubObject();
    private String string;


    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public SubObject getObject() {
        return object;
    }

    public void setObject(SubObject object) {
        this.object = object;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @DBusType(
            value = "s",
            fields = {"string"}
    )
    public static class SubObject extends Message{
        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}
