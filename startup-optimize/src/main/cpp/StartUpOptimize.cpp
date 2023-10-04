#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <sched.h>
#include <unistd.h>
#include "shadowhook.h"

#define LOG_TAG            "startup_optimize"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)

void *originGC = nullptr;
void *gcStub = nullptr;

typedef void (*TaskRunType)(void *, void *);

void newGCDelay(void *task, void *thread) {
    if (gcStub != nullptr) {
        LOG("unhook gc task");
        shadowhook_unhook(gcStub);
    }
    LOG("gc task sleep");
    sleep(5);
    LOG("gc task wake up");
    ((TaskRunType) originGC)(task, thread);
    LOG("gc task done");
}

void delayGC() {
    gcStub = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art2gc4Heap16ConcurrentGCTask3RunEPNS_6ThreadE",
                                      (void *) newGCDelay,
                                      (void **) &originGC);
    if (gcStub != nullptr) {
        LOG("hook ConcurrentGCTask success");
    } else {
        LOG("hook ConcurrentGCTask failed");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_startup_optimize_StartupNativeLib_delayGC(
        JNIEnv *env,
        jobject /* this */) {
    LOG("Open Startup Optimize");
    delayGC();
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_startup_optimize_StartupNativeLib_bindCore(
        JNIEnv *env,
        jobject /* this */, jint thread_id, jint core) {
    cpu_set_t mask;     //CPU核的集合
    CPU_ZERO(&mask);     //将mask置空
    CPU_SET(core, &mask);    //将需要绑定的cpu核设置给mask，核为序列0,1,2,3……
    if (sched_setaffinity(thread_id, sizeof(mask), &mask) == -1) {     //将线程绑核
        LOG("bind thread %d to core %d fail", thread_id, core);
    } else {
        LOG("bind thread %d to core %d success", thread_id, core);
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_startup_optimize_StartupNativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}