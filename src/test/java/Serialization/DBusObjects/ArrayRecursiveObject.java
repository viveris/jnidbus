package Serialization.DBusObjects;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

import java.util.ArrayList;
import java.util.List;

@DBusType(
        signature = "a(asi)",
        fields = {"objects"}
)
public class ArrayRecursiveObject extends Message {
    List<SubArrayRecursiveObject> objects = new ArrayList<>();

    public List<SubArrayRecursiveObject> getObjects() {
        return objects;
    }

    public void setObjects(List<SubArrayRecursiveObject> objects) {
        this.objects = objects;
    }

    @DBusType(
            signature = "asi",
            fields = {"strings","integer"}
    )
    public static class SubArrayRecursiveObject extends Message{
        List<String> strings = new ArrayList<>();
        int integer;

        public List<String> getStrings() {
            return strings;
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }

        public int getInteger() {
            return integer;
        }

        public void setInteger(int integer) {
            this.integer = integer;
        }
    }
}
