#include "./headers/serialization.h"

using namespace std;

void serialize(context* ctx, jobject message, DBusMessageIter* container){
    JNIEnv* env;
    get_env(ctx,&env);

    //get the serialized object and populate the message with it
    jmethodID id = env->GetMethodID(find_class(ctx,"fr/viveris/vizada/jnidbus/message/Message"), "serialize", "()Lfr/viveris/vizada/jnidbus/serialization/DBusObject;");
    jobject serialized = env->CallObjectMethod(message, id);
    jthrowable excOccured = env->ExceptionOccurred();

    //if the serialization went well
    if(!excOccured && serialized != NULL){
        jclass dbusObjectClass = find_class(ctx,"fr/viveris/vizada/jnidbus/serialization/DBusObject");
        
        //get signature and values, then transfer them to DBus
        jstring dbusTypesJVM = (jstring) env->CallObjectMethod(serialized, env->GetMethodID(dbusObjectClass, "getSignature", "()Ljava/lang/String;"));
        jobjectArray dbusValues = (jobjectArray) env->CallObjectMethod(serialized, env->GetMethodID(dbusObjectClass, "getValues", "()[Ljava/lang/Object;"));
        const char *dbusTypesNative = env->GetStringUTFChars(dbusTypesJVM, 0);
        int size = (int) env->GetStringLength(dbusTypesJVM);

        //cut the signature in single char and transform the JVM types in primitive types
        jobject valueJVM;
        for(int i = 0;i<size; i++ ){
            valueJVM = env->GetObjectArrayElement(dbusValues,i);

            //if the value is a string, transform to primitive char*, put it in the message and free it
            //there is no risk of use-after-free as DBus copy the string in an internal buffer.
            if(dbusTypesNative[i] == 's'){
                const char* valueNative = env->GetStringUTFChars((jstring)valueJVM, 0);
                dbus_message_iter_append_basic(container, DBUS_TYPE_STRING, &valueNative);
                env->ReleaseStringUTFChars((jstring) valueJVM, valueNative);
            }else if(dbusTypesNative[i] == 'i'){
                jclass intClass = env->FindClass("java/lang/Integer");
                jint valueNative = env->CallIntMethod(valueJVM,env->GetMethodID(intClass, "intValue", "()I"));
                dbus_message_iter_append_basic(container, DBUS_TYPE_INT32, &valueNative);
            }else{
                //TODO: throw something
            }
        }
        env->ReleaseStringUTFChars(dbusTypesJVM, dbusTypesNative);
    }
}

jobject unserialize(context* ctx, DBusMessageIter* container){
    JNIEnv* env;
    get_env(ctx,&env);

    vector<jobject> values;
    string signature = "";
    jclass dbusObjectClass = find_class(ctx,"fr/viveris/vizada/jnidbus/serialization/DBusObject");

    //check if there is data in the container
    
    if(dbus_message_iter_get_arg_type(container) != DBUS_TYPE_INVALID){
        do{
            if(dbus_message_iter_get_arg_type(container) == DBUS_TYPE_STRING){
                signature.append(1,TYPE_STRING);
                char* value = NULL;
                dbus_message_iter_get_basic(container, &value);
                values.push_back(env->NewStringUTF((const char*)value));
            }else if(dbus_message_iter_get_arg_type(container) == DBUS_TYPE_INT32){
                signature.append(1,TYPE_INT32);
                int value;
                dbus_message_iter_get_basic(container, &value);
                values.push_back(toInteger(env,value));
            }
        }while(dbus_message_iter_next(container));
    }

    jmethodID constructor = env->GetMethodID(dbusObjectClass, "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V");
    jobjectArray objectArray = env->NewObjectArray(values.size(),env->FindClass("java/lang/Object"),NULL);
    
    //fill array
    for(int i = 0; i<values.size();i++){
        env->SetObjectArrayElement(objectArray,i,values.at(i));
    }

    jobject unserialized = env->NewObject(dbusObjectClass,constructor,env->NewStringUTF(signature.c_str()),objectArray);

    return unserialized;
}

jobject toInteger(JNIEnv* env, int value){
    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID constructor = env->GetMethodID(integerClass, "<init>", "(I)V");
    return env->NewObject(integerClass,constructor,value);
}