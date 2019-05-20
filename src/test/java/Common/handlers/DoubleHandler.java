package Common.handlers;

import Common.DBusObjects.primitives.DoubleMessage;
import Common.DBusObjects.primitives.LongMessage;
import fr.viveris.vizada.jnidbus.dispatching.HandlerType;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;

@Handler(
        path = "/handlers/primitive/double",
        interfaceName = "Handlers.Primitive.DoubleHandler"
)
public class DoubleHandler extends CommonHandler<DoubleMessage> {

    @HandlerMethod(
            member = "handle",
            type = HandlerType.SIGNAL
    )
    public void handle(DoubleMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @DbusSignal(
            path = "/handlers/primitive/double",
            interfaceName = "Handlers.Primitive.DoubleHandler",
            member = "handle"
    )
    public static class DoubleSignal extends Signal<DoubleMessage>{
        public DoubleSignal(DoubleMessage params) {
            super(params);
        }
    }
}
