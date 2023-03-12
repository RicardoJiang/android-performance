#include <jni.h>
#include <string>
#include <android/log.h>
#include "bytehook.h"

#define LOG_TAG            "thread_hook"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)
#define LOGE(TAG, fmt, ...) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, ##__VA_ARGS__)
#define LOGD(TAG, fmt, ...) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, ##__VA_ARGS__)
#ifndef LIKELY
#define LIKELY(cond) (__builtin_expect(!!(cond), 1))
#endif

#ifndef UNLIKELY
#define UNLIKELY(cond) (__builtin_expect(!!(cond), 0))
#endif

static int AdjustStackSize(pthread_attr_t *attr) {
    size_t origStackSize = 0;
    {
        int ret = pthread_attr_getstacksize(attr, &origStackSize);
        if (UNLIKELY(ret != 0)) {
            LOGE(LOG_TAG, "Fail to call pthread_attr_getstacksize, ret: %d", ret);
            return ret;
        }
    }
    const size_t defaultNativeStackSize = 1064960;
    if (origStackSize != defaultNativeStackSize) {
        LOGE(LOG_TAG,
             "Stack size is not equal to default native stack size (%u != %u), give up adjusting.",
             origStackSize, defaultNativeStackSize);
        return ERANGE;
    }
    if (origStackSize < 2 * PTHREAD_STACK_MIN) {
        LOGE(LOG_TAG,
             "Stack size is too small to reduce (%u / 2 < %u (PTHREAD_STACK_MIN)), give up adjusting.",
             origStackSize, PTHREAD_STACK_MIN);
        return ERANGE;
    }
    int ret = pthread_attr_setstacksize(attr, origStackSize >> 1U);
    if (LIKELY(ret == 0)) {
        LOGD(LOG_TAG, "Succesfully shrink thread stack size from %u to %u.", origStackSize,
             origStackSize >> 1U);
    } else {
        LOGE(LOG_TAG, "Fail to call pthread_attr_setstacksize, ret: %d", ret);
    }
    return ret;
}

void
OnPThreadCreate(pthread_t *pthread_ptr, pthread_attr_t const *attr, void *(*start_routine)(void *),
                void *args) {
    if (attr == nullptr) {
        LOG("attr is null, skip adjusting.");
        return;
    }

    int ret = AdjustStackSize(const_cast<pthread_attr_t *>(attr));
    if (UNLIKELY(ret != 0)) {
        LOGE(LOG_TAG, "Fail to adjust stack size, ret: %d", ret);
    }
}

int pthread_create_proxy(pthread_t *_pthread_ptr, pthread_attr_t const *_attr,
                         void *(*_start_routine)(void *), void *args) {
    BYTEHOOK_STACK_SCOPE();
    OnPThreadCreate(_pthread_ptr, _attr, _start_routine, args);

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