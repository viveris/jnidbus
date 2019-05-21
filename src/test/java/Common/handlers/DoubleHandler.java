package Common.handlers;

import Common.DBusObjects.primitives.DoubleMessage;
import fr.viveris.jnidbus.dispatching.MemberType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.remote.RemoteInterface;
import fr.viveris.jnidbus.remote.RemoteMember;
import fr.viveris.jnidbus.remote.Signal;

@Handler(
        path = "/handlers/primitive/double",
        interfaceName = "Handlers.Primitive.DoubleHandler"
)
public class DoubleHandler extends CommonHandler<DoubleMessage> {

    @HandlerMethod(
            member = "handle",
            type = MemberType.SIGNAL
    )
    public void handle(DoubleMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @RemoteInterface("Handlers.Primitive.DoubleHandler")
    public interface DoubleHandlerRemote{

        @RemoteMember("handle")
        class DoubleSignal extends Signal<DoubleMessage> {
            public DoubleSignal(DoubleMessage params) {
                super(params);
            }
        }
    }
}
