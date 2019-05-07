#include <jni.h>
#include <dbus/dbus.h>
#include <map>
#include <sys/epoll.h>
#include <unistd.h>

#ifndef _context_headers_
#define _context_headers_

    #ifndef EPOLL_MAX_EVENTS
    #define EPOLL_MAX_EVENTS (int) 32
    #endif

    struct context{
        JavaVM* vm;
        DBusConnection* connection;
        char* bus_name;
        int epollFD;
        int wakeupFD;
        epoll_event* epollStruct;
        jobject eventLoop;
        std::map<std::string,jclass> class_cache;
    };

    struct handler_context{
        context* ctx;
        jobject dispatcher;
    };

    struct pending_call_context{
        context* ctx;
        jobject pending_call;
    };

    jclass find_class(context* context, const char* name);
    void get_env(context* context, JNIEnv** env);
    void close_context(context* ctx);
#endif