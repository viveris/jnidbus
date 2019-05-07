#include <dbus/dbus.h>
#include <jni.h>
#include "./context.h"
#include "./serialization.h"

#ifndef _event_loop_handlers_
#define _event_loop_handlers_

    /**
     * Watched functions used by DBus to set epoll
     */ 
    dbus_bool_t add_watch(DBusWatch *w, void *data);
    void remove_watch(DBusWatch *w, void *data);
    void toggle_watch(DBusWatch *w, void *data);

    /**
     * handler functions
     */
    DBusHandlerResult handle_dispatch(DBusConnection* connection, DBusMessage* msg, void* ctxPtr); 
    void handle_dispatch_unregister(DBusConnection* connection, void* ctxPtr); 

    void handle_call_response(DBusPendingCall* pending, void* ctxPtr);

#endif