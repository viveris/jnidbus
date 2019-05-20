package Common.handlers;

import Common.DBusObjects.primitives.IntMessage;
import fr.viveris.vizada.jnidbus.dispatching.HandlerType;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;

@Handler(
        path = "/handlers/primitive/int",
        interfaceName = "Handlers.Primitive.IntHandler"
)
public class IntHandler extends CommonHandler<IntMessage> {

    @HandlerMethod(
            member = "handle",
            type = HandlerType.SIGNAL
    )
    public void handle(IntMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @DbusSignal(
            path = "/handlers/primitive/int",
            interfaceName = "Handlers.Primitive.IntHandler",
            member = "handle"
    )
    public static class IntSignal extends Signal<IntMessage>{
        public IntSignal(IntMessage params) {
            super(params);
        }
    }
}
