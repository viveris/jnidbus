import Common.DBusObjects.SingleStringMessage
import Common.DBusTestCase
import fr.viveris.jnidbus.await
import fr.viveris.jnidbus.dispatching.GenericHandler
import fr.viveris.jnidbus.dispatching.KotlinGenericHandler
import fr.viveris.jnidbus.dispatching.KotlinMethodInvocator
import fr.viveris.jnidbus.dispatching.MemberType
import fr.viveris.jnidbus.dispatching.annotation.Handler
import fr.viveris.jnidbus.dispatching.annotation.HandlerMethod
import fr.viveris.jnidbus.message.Message
import fr.viveris.jnidbus.message.PendingCall
import fr.viveris.jnidbus.message.Promise
import fr.viveris.jnidbus.remote.RemoteInterface
import fr.viveris.jnidbus.remote.RemoteMember
import kotlinx.coroutines.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspendingHandlerTest : DBusTestCase(){

    @BeforeTest
    fun registerInvocator(){
        KotlinMethodInvocator.registerKotlinInvocator()
    }

    @Test
    fun `Handler can suspend and return value`() = runBlocking{
        val handler = CallHandler()
        this@SuspendingHandlerTest.receiver.addHandler(handler)
        val remoteObj = this@SuspendingHandlerTest.sender.createRemoteObject(
                this@SuspendingHandlerTest.receiverBusName,
                "/Call/AsyncCallTest",
                AsyncCallTestRemote::class.java)

        val pending = remoteObj.blockingCall()
        val msg = withTimeout(2500){
            pending.await()
        }

        assertEquals("test",msg.string)
    }

    @Handler(path = "/Call/AsyncCallTest", interfaceName = "Call.AsyncCallTest")
    class CallHandler : KotlinGenericHandler() {

        @HandlerMethod(member = "blockingCallWithSuspend", type = MemberType.METHOD)
        suspend fun blockingCallWithSuspend(emptyMessage: Message.EmptyMessage): SingleStringMessage {
            delay(2000)
            return SingleStringMessage().apply { string = "test" }
        }

        @HandlerMethod(member = "blockingCallWithPromise", type = MemberType.METHOD)
        fun blockingCallWithPromise(emptyMessage: Message.EmptyMessage): Promise<SingleStringMessage> {
            val promise = Promise<SingleStringMessage>()
            GlobalScope.launch {
                delay(2000)
                promise.resolve(SingleStringMessage().apply { string = "test" })
            }
            return promise
        }
    }

    @RemoteInterface("Call.AsyncCallTest")
    interface AsyncCallTestRemote {

        @RemoteMember("blockingCallWithSuspend")
        fun blockingCall(): PendingCall<SingleStringMessage>
    }
}