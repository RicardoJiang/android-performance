#include <jni.h>
#include <string>
#include "malloc.h"
#include <android/log.h>
#include <list>

using namespace std;

#define LOG_TAG            "android_performance"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)

static std::list<void *> objList;

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_performance_jni_NativeLibTest_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_performance_jni_NativeLibTest_testMalloc(
        JNIEnv *env,
        jobject /* this */, jlong size) {
    objList.push_back(malloc(size));
    LOG("malloc %lld byte success", size);
    LOG("objList size %d", objList.size());
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_performance_jni_NativeLibTest_testFree(
        JNIEnv *env,
        jobject /* this */) {
    if (!objList.empty()) {
        auto obj = objList.back();
        objList.pop_back();
        free(obj);
        LOG("free success");
        LOG("objList size %d", objList.size());
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_performance_jni_NativeLibTest_nativeCrash1(
        JNIEnv *env,
        jobject /* this */) {
    LOG("已保护Native崩溃");
    raise(SIGSEGV);
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_performance_jni_NativeLibTest_nativeCrash2(
        JNIEnv *env,
        jobject /* this */) {
    LOG("未保护Native崩溃");
    raise(SIGSEGV);
}