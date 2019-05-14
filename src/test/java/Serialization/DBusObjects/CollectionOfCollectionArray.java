package Serialization.DBusObjects;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

import java.util.ArrayList;
import java.util.List;

@DBusType(
        signature = "aai",
        fields = "array"
)
public class CollectionOfCollectionArray extends Message {
    private List<List<Integer>> array = new ArrayList<>();

    public List<List<Integer>> getArray() {
        return array;
    }

    public void setArray(List<List<Integer>> array) {
        this.array = array;
    }
}
