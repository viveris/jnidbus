#include "./headers/context.h"

jclass find_class(context* context, const char* name){
    //get from cache
    std::string nameString = std::string(name);
    jclass classJVM = context->class_cache[nameString];
    //if class not found in cache, fetch from the JVM
    if(classJVM == NULL){
        JNIEnv* env;
        get_env(context,&env);
        classJVM = (jclass) env->NewGlobalRef(env->FindClass(name));
        context->class_cache[nameString] = classJVM;
    }
    return classJVM;
}

void get_env(context* context, JNIEnv** env){
    context->vm->AttachCurrentThread((void **) env,NULL);
}

void close_context(context* ctx){
    JNIEnv* env;
    get_env(ctx,&env);

    //unregister any cached class ref
	std::map<std::string,jclass>::iterator it = ctx->class_cache.begin();
	while (it != ctx->class_cache.end())
	{
        env->DeleteGlobalRef((jobject)it->second);
		it++;
	}
    
    close(ctx->epollFD);
    close(ctx->wakeupFD);
    free(ctx->epollStruct);
    free(ctx->bus_name);
    env->DeleteGlobalRef(ctx->eventLoop);
}