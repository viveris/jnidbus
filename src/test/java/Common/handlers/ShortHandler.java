package Common.handlers;

import Common.DBusObjects.primitives.ShortMessage;
import fr.viveris.jnidbus.dispatching.HandlerType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.message.DbusSignal;
import fr.viveris.jnidbus.message.Signal;

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
