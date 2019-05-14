#include <dbus/dbus.h>
#include <jni.h>
#include <vector>
#include <string>
#include <cstring>
#include "./context.h"

#ifndef _serialization_
#define _serialization_

/**
 * Serialize a JVM Message object and transfer it to the Dbus message iterator.
 * The method will throw a JVM exception if something went wrong
 */
void serialize(context* ctx, jobject message, DBusMessageIter* container);

/**
 * Unserialize a DBus message iterator into a JVM Message object
 * NULL will be returned if something went wrong
 */ 
jobject unserialize(context* ctx, DBusMessageIter* container, DBusSignatureIter* signature);



/**
 * Transfer the JVM array into the container
 */
void serialize_array(context* ctx, int dbus_type, jobjectArray array, DBusMessageIter* container, DBusSignatureIter* signatureIter);

/**
 * Transfer the container array into a JVM array and append the array signature to the given string
 */
jobjectArray unserialize_array(context* ctx, int dbus_type, DBusMessageIter* container, std::string* signature, DBusSignatureIter* signatureIter);



/**
 * Transfer the JVM object into the container
 */
void serialize_element(context* ctx, int dbus_type, jobject object, DBusMessageIter* container);

/**
 * Transfer the serialized element into a JVM object
 */
jobject unserialize_element(context* ctx, DBusMessageIter* container);

#endif