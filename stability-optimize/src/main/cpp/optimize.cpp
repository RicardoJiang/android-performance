#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_stability_optimize_StabilityNativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}