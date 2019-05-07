#include <dbus/dbus.h>
#include <jni.h>
#include <vector>
#include <string>
#include <cstring>
#include "./context.h"

#ifndef _serialization_
#define _serialization_

/*
 * The two following functions are helpers built for recursive serialization/deserialization
 */

#define TYPE_STRING     (char) 's'
#define TYPE_INT32      (char) 'i'

/**
 * Serialize a JVM Message object and trasfer it to the Dbus message iterator.
 * The method will throw a JVM exception if something went wrong
 */
void serialize(context* ctx, jobject message, DBusMessageIter* container);

/**
 * Unserialize a DBus message iterator into a JVM Message object
 * NULL will be returned if something went wrong
 */ 
jobject unserialize(context* ctx, DBusMessageIter* container);

//helper function to transform a primitive int into a boxed JVM instance
jobject toInteger(JNIEnv* env, int value);

#endif