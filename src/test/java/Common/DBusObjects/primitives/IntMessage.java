package Common.DBusObjects.primitives;

import fr.viveris.jnidbus.message.Message;
import fr.viveris.jnidbus.serialization.DBusType;

import java.util.List;

@DBusType(
        signature = "iiai",
        fields = {"primitive","boxed","list"}
)
public class IntMessage extends Message {
    private int primitive;
    private Integer boxed;
    private List<Integer> list;

    public int getPrimitive() {
        return primitive;
    }

    public void setPrimitive(int primitive) {
        this.primitive = primitive;
    }

    public Integer getBoxed() {
        return boxed;
    }

    public void setBoxed(Integer boxed) {
        this.boxed = boxed;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }
}
