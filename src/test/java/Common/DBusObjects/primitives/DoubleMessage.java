package Common.DBusObjects.primitives;

import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.serialization.DBusType;

import java.util.List;

@DBusType(
        signature = "ddad",
        fields = {"primitive","boxed","list"}
)
public class DoubleMessage extends Message {
    private double primitive;
    private Double boxed;
    private List<Double> list;

    public double getPrimitive() {
        return primitive;
    }

    public void setPrimitive(double primitive) {
        this.primitive = primitive;
    }

    public Double getBoxed() {
        return boxed;
    }

    public void setBoxed(Double boxed) {
        this.boxed = boxed;
    }

    public List<Double> getList() {
        return list;
    }

    public void setList(List<Double> list) {
        this.list = list;
    }
}
