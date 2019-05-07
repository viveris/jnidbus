#include "./headers/fr_viveris_vizada_jnidbus_bindings_bus_EventLoop.h"
#include "./headers/event_loop_handlers.h"

#include <iostream>
#include <string>
#include <cstring>
#include <errno.h>

#include <sys/eventfd.h>

DBusObjectPathVTable dbus_handler_struct = {
    .unregister_function = handle_dispatch_unregister,
    .message_function = handle_dispatch
};

/* This method should setup epoll and the array contianing the file descriptors.

 * Class:     fr_viveris_vizada_jnidbus_bindings_bus_EventLoop
 * Method:    setup
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_setup
  (JNIEnv * env, jobject target, jlong ctxPtr){
    //get contxet
    context* ctx = (context*) ctxPtr;

    //create epoll instance
    ctx->epollFD = epoll_create1(0);

    //create wakeup file descriptor
    ctx->wakeupFD = eventfd(0,EFD_NONBLOCK);

    //create epoll struct in which it will put the selected file descriptors
    epoll_event wakeupStruct;
    ctx->epollStruct = (epoll_event*) calloc (EPOLL_MAX_EVENTS, sizeof(epoll_event));

    //add the wakeup fd to epoll, we only care about data to read
    wakeupStruct.data.fd = ctx->wakeupFD;
    wakeupStruct.events = EPOLLIN;
    int error = epoll_ctl(ctx->epollFD,EPOLL_CTL_ADD,ctx->wakeupFD,&wakeupStruct);
    if(error == -1){
        //todo, throw
    }

    //put event loop in context
    ctx->eventLoop = env->NewGlobalRef(target);

    //add global handler, we pass it the JVM environment as a param. We don't need to worry about attaching the
    //thread to the JVM as the thread was created by it and that the handler will be called on this same thread.
    //dbus_connection_add_filter(ctx->connection,handle_message,ctx,NULL);

    //add watch handlers
    dbus_connection_set_watch_functions(ctx->connection,add_watch,remove_watch,toggle_watch,ctx,NULL);

    return JNI_TRUE;
  }

/*
 * Class:     fr_viveris_vizada_jnidbus_bindings_bus_EventLoop
 * Method:    tick
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_tick
  (JNIEnv * env, jobject target,jlong ctxPtr){
    context* ctx = (context*) ctxPtr;
    epoll_event* events = ctx->epollStruct;

    int numberSelected = epoll_wait(ctx->epollFD,events,EPOLL_MAX_EVENTS,-1);
    //iterate through what epoll selected
    for (int i = 0; i < numberSelected; i++){
        if(events[i].data.fd == ctx->wakeupFD){
          //wakeup call detected, empty the event FD and proceed
          uint64_t u;
          read(events[i].data.fd,&u,sizeof(uint64_t));
          continue;
        }else{
          //a change have been detected on a FD, dispatch to dbus
          unsigned int flags = 0;
          unsigned int epollFlages = events[i].events;
          DBusWatch* watch = (DBusWatch*) events[i].data.ptr;
          int fd = dbus_watch_get_unix_fd(watch);

          if (epollFlages & EPOLLIN)  flags |= DBUS_WATCH_READABLE;
          if (epollFlages & EPOLLOUT) flags |= DBUS_WATCH_WRITABLE;
          if (epollFlages & EPOLLHUP) flags |= DBUS_WATCH_HANGUP;
          if (epollFlages & EPOLLERR) flags |= DBUS_WATCH_ERROR;
          if(dbus_watch_get_enabled(watch)){
            while (!dbus_watch_handle(watch, flags)) {
              //TODO, change that, maybe a if and a throw?
            }

            //dispatch all the detected events
            dbus_connection_ref(ctx->connection);
            while (dbus_connection_dispatch(ctx->connection) == DBUS_DISPATCH_DATA_REMAINS);
            dbus_connection_unref(ctx->connection);
          }
        }
    }
  }

/*
 * Class:     fr_viveris_vizada_jnidbus_bindings_bus_EventLoop
 * Method:    wakeup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_wakeup
  (JNIEnv * env, jobject target, jlong ctxPtr){
    context* ctx = (context*) ctxPtr;
    //write a random value
    uint64_t u = 1;
    size_t written = write(ctx->wakeupFD,&u,sizeof(uint64_t));
  }

JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_sendReply
  (JNIEnv * env, jobject target, jlong ctxPtr, jobject messageJVM, jlong msgPointer){
    context* ctx = (context*) ctxPtr;
    DBusConnection* conn = ctx->connection;
    DBusMessage* callMsg = (DBusMessage*) msgPointer;
    DBusMessage* msg = dbus_message_new_method_return(callMsg);
    dbus_message_unref(callMsg);

    DBusError err;
    dbus_error_init(&err);

    DBusMessageIter args;
    dbus_message_iter_init_append(msg,&args);
    serialize(ctx,messageJVM,&args);
    if(env->ExceptionOccurred()){
      //TODO throw
    }else{
      dbus_uint32_t msgSerial = 0;
      if (!dbus_connection_send(conn, msg, &msgSerial)) {
        env->ThrowNew(find_class(ctx,"java/lang/IllegalStateException"),"Sending failed (probably caused by an out of memory)");
      }
    }

    dbus_message_unref(msg);
  }

JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_sendReplyError
  (JNIEnv * env, jobject target, jlong ctxPtr, jlong msgPointer, jstring errorCodeJVM, jstring errorMessageJVM){
    context* ctx = (context*) ctxPtr;
    DBusConnection* conn = ctx->connection;
    DBusMessage* callMsg = (DBusMessage*) msgPointer;

    const char* errorCodeNative = env->GetStringUTFChars(errorCodeJVM, 0);
    const char* messageNative = env->GetStringUTFChars(errorMessageJVM, 0);

    DBusMessage* msg = dbus_message_new_error(callMsg,errorCodeNative,messageNative);

    dbus_uint32_t msgSerial = 0;
    if (!dbus_connection_send(conn, msg, &msgSerial)) {
      env->ThrowNew(find_class(ctx,"java/lang/IllegalStateException"),"Sending failed (probably caused by an out of memory)");
    }

    env->ReleaseStringUTFChars(errorCodeJVM, errorCodeNative);
    env->ReleaseStringUTFChars(errorMessageJVM, messageNative);

}

JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_sendSignal
  (JNIEnv * env, jobject target, jlong ctxPtr, jstring pathJVM, jstring interfaceJVM, jstring memberJVM, jobject messageJVM){

    context* ctx = (context*) ctxPtr;
    DBusConnection* conn = ctx->connection;

    DBusError err;
    dbus_error_init(&err);

    //transform them into native types
    const char* pathNative = env->GetStringUTFChars(pathJVM, 0);
    const char* typeNative = env->GetStringUTFChars(interfaceJVM, 0);
    const char* nameNative = env->GetStringUTFChars(memberJVM, 0);

    //build message
    DBusMessage* msg = dbus_message_new_signal(pathNative,typeNative,nameNative);
    DBusMessageIter args;
    dbus_message_iter_init_append(msg,&args);
    serialize(ctx,messageJVM,&args);
    if(env->ExceptionOccurred()){
      //TODO throw
    }else{
      dbus_uint32_t msgSerial = 0;
      if (!dbus_connection_send(conn, msg, &msgSerial)) {
        env->ThrowNew(find_class(ctx,"java/lang/IllegalStateException"),"Sending failed (probably caused by an out of memory)");
      }
    }

    env->ReleaseStringUTFChars(pathJVM, pathNative);
    env->ReleaseStringUTFChars(interfaceJVM, typeNative);
    env->ReleaseStringUTFChars(memberJVM, nameNative);
    dbus_message_unref(msg);
}

/*
 * Class:     fr_viveris_vizada_jnidbus_bindings_bus_Connection
 * Method:    sendEvent
 * Signature: (Lfr/viveris/vizada/jnidbus/bindings/message/Event;)Z
 */
JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_sendCall
  (JNIEnv * env, jobject target, jlong ctxPtr, jstring pathJVM, jstring interfaceJVM, jstring memberJVM, jobject messageJVM, jstring destJVM, jobject pendingCall){
    context* ctx = (context*) ctxPtr;
    DBusError err;
    dbus_error_init(&err);

    //get the dbus connection from pointer
    DBusConnection* conn = ctx->connection;

    //transform them into native types
    const char* pathNative = env->GetStringUTFChars(pathJVM, 0);
    const char* typeNative = env->GetStringUTFChars(interfaceJVM, 0);
    const char* nameNative = env->GetStringUTFChars(memberJVM, 0);
    const char* destNative = env->GetStringUTFChars(destJVM, 0);

    //build message
    DBusMessage* msg = dbus_message_new_method_call(destNative,pathNative,typeNative,nameNative);
    DBusMessageIter args;
    dbus_message_iter_init_append(msg,&args);
    serialize(ctx,messageJVM,&args);
    if(env->ExceptionOccurred()){
      //TODO throw
    }else{
      DBusPendingCall* res;
      if (!dbus_connection_send_with_reply(conn,msg,&res,-1)) {
        env->ThrowNew(find_class(ctx,"java/lang/IllegalStateException"),"Sending failed (probably caused by an out of memory)");
      }else{
        pending_call_context* callContext = (pending_call_context*) malloc(sizeof(pending_call_context));
        callContext->pending_call = env->NewGlobalRef(pendingCall);
        callContext->ctx = ctx;
        dbus_pending_call_set_notify(res,handle_call_response,callContext,NULL);
      }
    }

    //free resources
    env->ReleaseStringUTFChars(pathJVM, pathNative);
    env->ReleaseStringUTFChars(interfaceJVM, typeNative);
    env->ReleaseStringUTFChars(memberJVM, nameNative);
    env->ReleaseStringUTFChars(destJVM, destNative);

    dbus_message_unref(msg);
}

/*
 * Class:     fr_viveris_vizada_jnidbus_bindings_bus_EventLoop
 * Method:    addPathHandler
 * Signature: (JLjava/lang/String;Lfr/viveris/vizada/jnidbus/dispatching/Dispatcher;)V
 */
JNIEXPORT void JNICALL Java_fr_viveris_vizada_jnidbus_bindings_bus_EventLoop_addPathHandler
  (JNIEnv * env, jobject target, jlong ctxPtr, jstring pathJVM, jobject dispatcher){
  context* ctx = (context*) ctxPtr;

  const char* pathNative = env->GetStringUTFChars(pathJVM, 0);

  handler_context* handler_data = (handler_context*) malloc(sizeof(handler_context));
  handler_data->ctx = ctx;
  handler_data->dispatcher = env->NewGlobalRef(dispatcher);
  dbus_connection_register_object_path(ctx->connection,pathNative,&dbus_handler_struct,handler_data);
  std::string match = "type='signal',path='";
  match += pathNative;
  match += "'";
  printf("match added : %s\n",match.c_str());
  fflush(stdout);
  dbus_bus_add_match(ctx->connection,match.c_str(),NULL);

  env->ReleaseStringUTFChars(pathJVM,pathNative);

}