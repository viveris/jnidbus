package Serialization.DBusObjects;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

import java.util.ArrayList;
import java.util.List;

@DBusType(
        value = "ai",
        fields = "array"
)
public class CollectionArray extends Message {
    private List<Integer> array = new ArrayList<>();

    public List<Integer> getArray() {
        return array;
    }

    public void setArray(List<Integer> array) {
        this.array = array;
    }
}
