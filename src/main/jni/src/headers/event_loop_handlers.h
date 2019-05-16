#include <dbus/dbus.h>
#include <jni.h>
#include "./context.h"
#include "./serialization.h"

#ifndef _event_loop_handlers_
#define _event_loop_handlers_

    /**
     * Watch functions used by DBus to controll epoll
     */ 
    dbus_bool_t add_watch(DBusWatch *w, void *data);
    void remove_watch(DBusWatch *w, void *data);
    void toggle_watch(DBusWatch *w, void *data);

    /**
     * handler function called when a message arrives directly on the registered object path, the message 
     * canb be anything (signal or call) and will be given to the JVM dispatcher for proper dispatching
     */
    DBusHandlerResult handle_dispatch(DBusConnection* connection, DBusMessage* msg, void* ctxPtr);

    /**
     * handler called when unregistering an object path
     */
    void handle_dispatch_unregister(DBusConnection* connection, void* ctxPtr); 

    /**
     * handler function called when the response to a call is received, the received response will not be dispatched to
     * handle_dispatch.
     */
    void handle_call_response(DBusPendingCall* pending, void* ctxPtr);

#endif