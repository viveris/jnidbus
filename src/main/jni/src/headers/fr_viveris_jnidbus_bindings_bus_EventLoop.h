/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class fr_viveris_jnidbus_bindings_bus_EventLoop */

#ifndef _Included_fr_viveris_jnidbus_bindings_bus_EventLoop
#define _Included_fr_viveris_jnidbus_bindings_bus_EventLoop
#ifdef __cplusplus
extern "C" {
#endif
#undef fr_viveris_jnidbus_bindings_bus_EventLoop_SENDING_QUEUE_SIZE
#define fr_viveris_jnidbus_bindings_bus_EventLoop_SENDING_QUEUE_SIZE 128L
/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    setup
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_setup
  (JNIEnv *, jobject, jlong);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    tick
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_tick
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    wakeup
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_wakeup
  (JNIEnv *, jobject, jlong);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    sendReply
 * Signature: (JLfr/viveris/jnidbus/serialization/DBusObject;J)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_sendReply
  (JNIEnv *, jobject, jlong, jobject, jlong);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    sendReplyError
 * Signature: (JJLjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_sendReplyError
  (JNIEnv *, jobject, jlong, jlong, jstring, jstring);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    sendSignal
 * Signature: (JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lfr/viveris/jnidbus/serialization/DBusObject;)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_sendSignal
  (JNIEnv *, jobject, jlong, jstring, jstring, jstring, jobject);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    sendCall
 * Signature: (JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lfr/viveris/jnidbus/serialization/DBusObject;Ljava/lang/String;Lfr/viveris/jnidbus/message/PendingCall;)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_sendCall
  (JNIEnv *, jobject, jlong, jstring, jstring, jstring, jobject, jstring, jobject);

/*
 * Class:     fr_viveris_jnidbus_bindings_bus_EventLoop
 * Method:    addPathHandler
 * Signature: (JLjava/lang/String;Lfr/viveris/jnidbus/dispatching/Dispatcher;)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_EventLoop_addPathHandler
  (JNIEnv *, jobject, jlong, jstring, jobject);

#ifdef __cplusplus
}
#endif
#endif
