package Common.handlers;

import Common.DBusObjects.primitives.BooleanMessage;
import Common.DBusObjects.primitives.IntMessage;
import fr.viveris.vizada.jnidbus.dispatching.HandlerType;
import fr.viveris.vizada.jnidbus.dispatching.annotation.Handler;
import fr.viveris.vizada.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.vizada.jnidbus.message.DbusSignal;
import fr.viveris.vizada.jnidbus.message.Signal;

@Handler(
        path = "/handlers/primitive/boolean",
        interfaceName = "Handlers.Primitive.BooleanHandler"
)
public class BooleanHandler extends CommonHandler<BooleanMessage> {

    @HandlerMethod(
            member = "handle",
            type = HandlerType.SIGNAL
    )
    public void handle(BooleanMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @DbusSignal(
            path = "/handlers/primitive/boolean",
            interfaceName = "Handlers.Primitive.BooleanHandler",
            member = "handle"
    )
    public static class BooleanSignal extends Signal<BooleanMessage>{
        public BooleanSignal(BooleanMessage params) {
            super(params);
        }
    }
}
