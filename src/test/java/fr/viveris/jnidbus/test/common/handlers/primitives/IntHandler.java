/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.test.common.handlers.primitives;

import fr.viveris.jnidbus.dispatching.MemberType;
import fr.viveris.jnidbus.dispatching.annotation.Handler;
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod;
import fr.viveris.jnidbus.remote.RemoteInterface;
import fr.viveris.jnidbus.remote.RemoteMember;
import fr.viveris.jnidbus.remote.Signal;
import fr.viveris.jnidbus.test.common.DBusObjects.primitives.IntMessage;
import fr.viveris.jnidbus.test.common.handlers.CommonHandler;

@Handler(
        path = "/handlers/primitive/int",
        interfaceName = "Handlers.Primitive.IntHandler"
)
public class IntHandler extends CommonHandler<IntMessage> {

    @HandlerMethod(
            member = "handle",
            type = MemberType.SIGNAL
    )
    public void handle(IntMessage msg){
        this.doHandle(msg);
    }

    @Override
    public Signal<IntMessage> buildSignal(IntMessage value) {
        return new IntHandlerRemote.IntSignal(value);
    }

    @RemoteInterface("Handlers.Primitive.IntHandler")
    public interface IntHandlerRemote{

        @RemoteMember("handle")
        class IntSignal extends Signal<IntMessage> {
            public IntSignal(IntMessage params) {
                super(params);
            }
        }
    }
}
