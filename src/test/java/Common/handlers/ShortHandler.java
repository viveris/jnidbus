package Common.handlers;

import Common.DBusObjects.primitives.ByteMessage;
import Common.DBusObjects.primitives.ShortMessage;
import fr.viveris.vizada.jnidbus.dispatching.HandlerType;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;

@Handler(
        path = "/handlers/primitive/short",
        interfaceName = "Handlers.Primitive.ShortHandler"
)
public class ShortHandler extends CommonHandler<ShortMessage> {

    @HandlerMethod(
            member = "handle",
            type = HandlerType.SIGNAL
    )
    public void handle(ShortMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @DbusSignal(
            path = "/handlers/primitive/short",
            interfaceName = "Handlers.Primitive.ShortHandler",
            member = "handle"
    )
    public static class ShortSignal extends Signal<ShortMessage>{
        public ShortSignal(ShortMessage params) {
            super(params);
        }
    }
}
