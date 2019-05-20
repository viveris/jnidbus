package Common.DBusObjects;

import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.serialization.DBusType;

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
