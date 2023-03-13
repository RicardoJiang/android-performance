#include <jni.h>
#include <string>
#include <android/log.h>
#include "bytehook.h"
#include "ThreadStackShink.h"

#define LOG_TAG            "thread_hook"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)

int pthread_create_proxy(pthread_t *_pthread_ptr, pthread_attr_t const *_attr,
                         void *(*_start_routine)(void *), void *args) {
    BYTEHOOK_STACK_SCOPE();
    thread_stack_shink::OnPThreadCreate(_pthread_ptr, _attr, _start_routine, args);

    int result = BYTEHOOK_CALL_PREV(pthread_create_proxy, _pthread_ptr, _attr, _start_routine,
                                    args);
    return result;
}

void
thread_hooked_callback(bytehook_stub_t task_stub, int status_code, const char *caller_path_name,
                       const char *sym_name, void *new_func, void *prev_func, void *arg) {
    if (BYTEHOOK_STATUS_CODE_ORIG_ADDR == status_code) {
        LOG(">>>>> save original address: %u", (uintptr_t) prev_func);
    } else {
        LOG(">>>>> hooked. stub: %u status: %d, caller_path_name: %s, sym_name: %s, new_func: %u, prev_func: %u"", arg: %u",
            (uintptr_t) task_stub, status_code, caller_path_name, sym_name, (uintptr_t) new_func,
            (uintptr_t) prev_func, (uintptr_t) arg);
    }
}

void hookThread() {
    bytehook_hook_all(nullptr, "pthread_create", (void *) pthread_create_proxy,
                      thread_hooked_callback,
                      nullptr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_thread_hook_ThreadHookNativeLib_hookThread(
        JNIEnv *env,
        jobject /* this */) {
    hookThread();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_thread_hook_ThreadHookNativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}