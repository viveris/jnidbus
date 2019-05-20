package Common.handlers;

import Common.DBusObjects.primitives.LongMessage;
import Common.DBusObjects.primitives.ShortMessage;
import fr.viveris.vizada.jnidbus.dispatching.HandlerType;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;

@Handler(
        path = "/handlers/primitive/long",
        interfaceName = "Handlers.Primitive.LongHandler"
)
public class LongHandler extends CommonHandler<LongMessage> {

    @HandlerMethod(
            member = "handle",
            type = HandlerType.SIGNAL
    )
    public void handle(LongMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @DbusSignal(
            path = "/handlers/primitive/long",
            interfaceName = "Handlers.Primitive.LongHandler",
            member = "handle"
    )
    public static class LongSignal extends Signal<LongMessage>{
        public LongSignal(LongMessage params) {
            super(params);
        }
    }
}
