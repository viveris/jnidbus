package Common.DBusObjects.primitives;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

import java.util.List;

@DBusType(
        signature = "bbab",
        fields = {"primitive","boxed","list"}
)
public class BooleanMessage extends Message {
    private boolean primitive;
    private Boolean boxed;
    private List<Boolean> list;

    public boolean getPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public Boolean getBoxed() {
        return boxed;
    }

    public void setBoxed(Boolean boxed) {
        this.boxed = boxed;
    }

    public List<Boolean> getList() {
        return list;
    }

    public void setList(List<Boolean> list) {
        this.list = list;
    }
}
