package Common.handlers;

import Common.DBusObjects.primitives.BooleanMessage;
import fr.viveris.jnidbus.dispatching.MemberType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.remote.RemoteInterface;
import fr.viveris.jnidbus.remote.RemoteMember;
import fr.viveris.jnidbus.remote.Signal;

@Handler(
        path = "/handlers/primitive/boolean",
        interfaceName = "Handlers.Primitive.BooleanHandler"
)
public class BooleanHandler extends CommonHandler<BooleanMessage> {

    @HandlerMethod(
            member = "handle",
            type = MemberType.SIGNAL
    )
    public void handle(BooleanMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @RemoteInterface("Handlers.Primitive.BooleanHandler")
    public interface BooleanHandlerRemote{

        @RemoteMember("handle")
        class BooleanSignal extends Signal<BooleanMessage> {
            public BooleanSignal(BooleanMessage params) {
                super(params);
            }
        }
    }
}
