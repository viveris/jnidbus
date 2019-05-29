package fr.viveris.jnidbus.dispatching

import fr.viveris.jnidbus.exception.DBusException
import fr.viveris.jnidbus.message.PendingCall
import fr.viveris.jnidbus.message.Promise
import fr.viveris.jnidbus.serialization.Serializable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

class KotlinMethodInvocator : HandlerMethod.MethodInvocator{
    /**
     * Equivalent of the static{} initializer in Java
     */
    companion object{
        fun registerKotlinInvocator() {
            HandlerMethod.kotlinInvocator = KotlinMethodInvocator()
        }
    }

    override fun <T : Serializable>call(handler : Any?, method: Method, param: Serializable?): Any {
        val promise = Promise<T>()
        val kotlinMethod = method.kotlinFunction!!
        val job = GlobalScope.launch {
            kotlinMethod.callSuspend(handler,param)
                    .let { promise.resolve(it as T) }
        }
        job.invokeOnCompletion {
            if(it != null){
                when(it){
                    is CancellationException -> {
                        promise.reject(DBusException(PendingCall.PENDING_CALL_CANCELLED_ERROR_CODE,"The handler execution was cancelled"))
                    }
                    else -> {
                        promise.reject(DBusException(it.javaClass.name,it.message))
                    }
                }
            }
        }
        return promise
    }
}