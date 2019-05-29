package fr.viveris.jnidbus

import fr.viveris.jnidbus.exception.DBusException
import fr.viveris.jnidbus.message.PendingCall
import fr.viveris.jnidbus.serialization.Serializable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun <T : Serializable> PendingCall<T>.await() : T = suspendCancellableCoroutine{cont ->
    this.setListener(object : PendingCall.Listener<T>{
        override fun notify(value: T) {
            cont.resume(value)
        }

        override fun error(t: DBusException?) {
            cont.cancel(t)
        }
    })

    cont.invokeOnCancellation {
        this.cancel()
    }
}