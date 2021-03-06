/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
#include "./headers/fr_viveris_jnidbus_bindings_bus_Connection.h"
#include "./headers/context.h"
#include <iostream>
#include <string>
#include <cstring>

#include <dbus/dbus.h>

using namespace std;

/*
 * Create a new DBus connection. The created connection is private, meaning that one process can create multiple
 * DBus connection instead of sharing only one, if the developer wants only one open connection, it should ensure it
 * himself. The method will throw a ConnectionException and return null if something goes wrong.
 *
 * Class:     fr_viveris_jnidbus_bindings_bus_Connection
 * Method:    createConnection
 * Signature: (Lfr/viveris/jnidbus/BusType;)Lfr/viveris/jnidbus/bindings/bus/Connection;
 */
JNIEXPORT jobject JNICALL Java_fr_viveris_jnidbus_bindings_bus_Connection_createConnection
  (JNIEnv * env, jclass target, jobject busType, jstring busName, jstring bus_address){
      //init context
      context* ctx = new context;
      env->GetJavaVM(&ctx->vm);

      //get the bus type needed
      jmethodID getBusTypeMethod = env->GetMethodID(find_class(ctx,"fr/viveris/jnidbus/BusType"), "name", "()Ljava/lang/String;");
      jstring rawValue = (jstring) env->CallObjectMethod(busType, getBusTypeMethod);
      const char* value = env->GetStringUTFChars(rawValue, 0);
      
      DBusBusType type;
   
      if(strcmp(value,"SYSTEM") == 0){
         type = DBUS_BUS_SYSTEM;
      }else if(strcmp(value,"SESSION") == 0){
         type = DBUS_BUS_SESSION;
      }else if(strcmp(value,"STARTER") == 0){
         type = DBUS_BUS_STARTER;
      }else{
         env->ThrowNew(find_class(ctx,"fr/viveris/jnidbus/exception/ConnectionException"),"Unknown bus type");
         return NULL;
      }

      //connect to bus
      DBusError err;
      dbus_error_init(&err);

      DBusConnection* conn;

      if(bus_address == NULL){
         conn = dbus_bus_get_private(type, &err);
      }else{
         const char* bus_address_native = env->GetStringUTFChars(bus_address, 0);
         conn = dbus_connection_open_private(bus_address_native, &err);
         dbus_bus_register(conn,&err);
         env->ReleaseStringUTFChars(bus_address, bus_address_native);
      }
      
      if (dbus_error_is_set(&err)) { 
         env->ThrowNew(find_class(ctx,"fr/viveris/jnidbus/exception/ConnectionException"),err.message);
         dbus_error_free(&err); 
         return NULL;
      }


      // register our name on the bus, and check for errors
      const char* busNameConverted = env->GetStringUTFChars(busName, 0);
      char* busNameCopied = (char*) malloc(sizeof(char)*(strlen(busNameConverted)+1));
      strcpy(busNameCopied,busNameConverted);

      int ret = dbus_bus_request_name(conn, busNameConverted, DBUS_NAME_FLAG_REPLACE_EXISTING , &err);
      if (dbus_error_is_set(&err)) { 
         env->ThrowNew(find_class(ctx,"fr/viveris/jnidbus/exception/ConnectionException"),err.message);
         dbus_error_free(&err);
         return NULL;
      }

      if (DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER != ret) { 
         env->ThrowNew(find_class(ctx,"fr/viveris/jnidbus/exception/ConnectionException"),"Could not apply bus name");
         return NULL;
      }

      //add connection to the context
      ctx->connection = conn;
      ctx->bus_name = busNameCopied;

      //free resources
      env->ReleaseStringUTFChars(rawValue, value);
      env->ReleaseStringUTFChars(busName, busNameConverted);

      //construct JVM object and pass the connection pointer to it
      jclass cls = find_class(ctx, "fr/viveris/jnidbus/bindings/bus/Connection");
      jmethodID constructor = env->GetMethodID(cls, "<init>", "(JLjava/lang/String;)V");
      jobject obj = env->NewObject(cls, constructor,(uintptr_t) ctx,busName);
      return obj;
  }

/*
 * Close the DBus connection and free any resource used
 *
 * Class:     fr_viveris_jnidbus_bindings_bus_Connection
 * Method:    closeNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fr_viveris_jnidbus_bindings_bus_Connection_closeNative
  (JNIEnv * env, jobject target, jlong contextPtr){
     context* ctx = (context*) contextPtr;
      //get the dbus connection from pointer
      DBusConnection* conn = ctx->connection;

      DBusError err;
      dbus_error_init(&err);

      dbus_bus_release_name(conn,ctx->bus_name,&err);

      if (dbus_error_is_set(&err)) { 
         env->ThrowNew(find_class(ctx,"fr/viveris/jnidbus/exception/ConnectionException"),err.message);
         dbus_error_free(&err); 
         return;
      }

      dbus_connection_flush(conn);

      //free resources
      dbus_connection_close(conn);
      dbus_connection_unref(conn);

      close_context(ctx);
      delete ctx;
  }
