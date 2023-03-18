#include <jni.h>
#include <string>
#include "malloc.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_memory_hook_MemoryNativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}