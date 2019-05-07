#include "./headers/event_loop_handlers.h"

DBusHandlerResult handle_dispatch(DBusConnection* connection, DBusMessage* msg, void* ctxPtr){
  //ref the message to notify dbus we are processing it
  dbus_message_ref(msg);

  //get the JVM environment
  handler_context* handlerCtx = (handler_context*) ctxPtr;
  context* ctx = (context*) handlerCtx->ctx;
  JNIEnv* env;
  get_env(ctx,&env);
  
  DBusMessageIter rootIter;
  dbus_message_iter_init(msg, &rootIter);
  jobject jvmObject = unserialize(ctx,&rootIter);
  
  //TODO: check null return

  //message metadata
  const char * interface = dbus_message_get_interface(msg);
  const char * member = dbus_message_get_member(msg);

  //if the message is a call, prepare the response
  uintptr_t msgPointer = 0;
  if(dbus_message_get_type(msg) == DBUS_MESSAGE_TYPE_METHOD_CALL){
    msgPointer = (uintptr_t) msg;
  }

  //Call the JVM handler method
  env->CallVoidMethod(
    handlerCtx->dispatcher,
    env->GetMethodID(
      find_class(ctx,"fr/viveris/vizada/jnidbus/dispatching/Dispatcher"), 
      "dispatch", 
      "(Lfr/viveris/vizada/jnidbus/serialization/DBusObject;Ljava/lang/String;Ljava/lang/String;J)V"
    ),
    jvmObject,
    env->NewStringUTF(interface),
    env->NewStringUTF(member),
    msgPointer
  );

  if(msgPointer == 0){
    dbus_message_unref(msg);
  }

  return DBUS_HANDLER_RESULT_HANDLED;
}

void handle_dispatch_unregister(DBusConnection* connection, void* ctxPtr){
  handler_context* handlerCtx = (handler_context*) ctxPtr;
  context* ctx = (context*) handlerCtx->ctx;

  JNIEnv* env;
  get_env(ctx,&env);

  env->DeleteGlobalRef(handlerCtx->dispatcher);
  free(handlerCtx);
}

void handle_call_response(DBusPendingCall* pending, void* ctxPtr){
  pending_call_context* pCtx = (pending_call_context*) ctxPtr;
  context* ctx = pCtx->ctx;
  JNIEnv* env;
  get_env(ctx,&env);

  DBusMessage* msg = dbus_pending_call_steal_reply(pending);

  if(msg == NULL){
    //TODO throw
  }

  //TODO: check if reply is an error

  DBusMessageIter rootIter;
  dbus_message_iter_init(msg, &rootIter);
  jobject jvmObject = unserialize(ctx,&rootIter);

  env->CallVoidMethod(
    pCtx->pending_call,
    env->GetMethodID(
      find_class(ctx,"fr/viveris/vizada/jnidbus/message/PendingCall"), 
      "notify", 
      "(Lfr/viveris/vizada/jnidbus/serialization/DBusObject;)V"
    ),
    jvmObject
  );

  //free resources
  dbus_message_unref(msg);
  dbus_pending_call_unref(pending);
  env->DeleteGlobalRef(pCtx->pending_call);
  free(pCtx);
}

dbus_bool_t add_watch(DBusWatch *w, void *data){
    //get the JVM environment
    context* ctx = (context*) data;
    JNIEnv* env;
    get_env(ctx,&env);

    //get epoll fd from JVM object

    // we always want to know if the socket was closed
    short cond = EPOLLHUP | EPOLLERR;
    int fd = dbus_watch_get_unix_fd(w);
    unsigned int flags = dbus_watch_get_flags(w);

    
    if(flags & DBUS_WATCH_READABLE){
        cond |= EPOLLIN;
    }
    if(flags & DBUS_WATCH_WRITABLE){
        cond |= EPOLLOUT;
    }

    //register to epoll and store a pointer to the watch in the epoll struct. Beware, event.data is a union struct
    //so only one field can be used at a time, meaning we are storing only the watch pointer (which contains the
    //file descriptor, so we are fine)
    epoll_event event;
    event.events = cond;
    event.data.ptr = w;

    int error = epoll_ctl(ctx->epollFD,EPOLL_CTL_ADD,fd,&event);
    if(error == -1){
        //if the error is that we already have a FD registered, try to update instead
        if(errno == EEXIST){
            error = epoll_ctl(ctx->epollFD,EPOLL_CTL_MOD,fd,&event);
            return 1;
        }
    }

    return 1;
}

void remove_watch(DBusWatch *w, void *data){
  context* ctx = (context*) data;
  JNIEnv* env;
  get_env(ctx,&env);

  epoll_ctl(ctx->epollFD,EPOLL_CTL_DEL,dbus_watch_get_unix_fd(w),NULL);
}

void toggle_watch(DBusWatch *w, void *data){
  //TODO set the epoll flag at 0 when they are not and set them to the correct value when they are zeroed
}