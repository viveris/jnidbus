package Common.handlers;

import Common.DBusObjects.primitives.ByteMessage;
import fr.viveris.jnidbus.dispatching.MemberType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.remote.RemoteInterface;
import fr.viveris.jnidbus.remote.RemoteMember;
import fr.viveris.jnidbus.remote.Signal;

@Handler(
        path = "/handlers/primitive/byte",
        interfaceName = "Handlers.Primitive.ByteHandler"
)
public class ByteHandler extends CommonHandler<ByteMessage> {

    @HandlerMethod(
            member = "handle",
            type = MemberType.SIGNAL
    )
    public void handle(ByteMessage msg){
        this.barrier.countDown();
        this.value = msg;
    }

    @RemoteInterface("Handlers.Primitive.ByteHandler")
    public interface ByteHandlerRemote{

        @RemoteMember("handle")
        class ByteSignal extends Signal<ByteMessage> {
            public ByteSignal(ByteMessage params) {
                super(params);
            }
        }
    }
}
