package Common.DBusObjects.primitives;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

import java.util.List;

@DBusType(
        signature = "yyay",
        fields = {"primitive","boxed","list"}
)
public class ByteMessage extends Message {
    private byte primitive;
    private Byte boxed;
    private List<Byte> list;

    public byte getPrimitive() {
        return primitive;
    }

    public void setPrimitive(byte primitive) {
        this.primitive = primitive;
    }

    public Byte getBoxed() {
        return boxed;
    }

    public void setBoxed(Byte boxed) {
        this.boxed = boxed;
    }

    public List<Byte> getList() {
        return list;
    }

    public void setList(List<Byte> list) {
        this.list = list;
    }
}
