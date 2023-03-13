#include "jni.h"
#include "ThreadStackShink.h"
#include <string>
#include <android/log.h>

#define LOG_TAG            "thread_hook"
#define LOGE(TAG, fmt, ...) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, ##__VA_ARGS__)
#define LOGD(TAG, fmt, ...) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, ##__VA_ARGS__)
#ifndef LIKELY
#define LIKELY(cond) (__builtin_expect(!!(cond), 1))
#endif

#ifndef UNLIKELY
#define UNLIKELY(cond) (__builtin_expect(!!(cond), 0))
#endif
static std::atomic_size_t sDefaultNativeStackSize(0);

namespace thread_stack_shink {
    static size_t GetDefaultNativeStackSize() {
        size_t stackSize = sDefaultNativeStackSize.load();
        if (UNLIKELY(stackSize == 0)) {
            pthread_attr_t defAttr = {};
            pthread_attr_init(&defAttr);
            pthread_attr_getstacksize(&defAttr, &stackSize);
            if (stackSize == 0) {
                constexpr size_t SIGNAL_STACK_SIZE_WITHOUT_GUARD = 16 * 1024;
                stackSize = 1 * 1024 * 1024 - SIGNAL_STACK_SIZE_WITHOUT_GUARD;
            }
            sDefaultNativeStackSize.store(stackSize);
        }
        return stackSize;
    }

    static int AdjustStackSize(pthread_attr_t *attr) {
        size_t origStackSize = 0;
        {
            int ret = pthread_attr_getstacksize(attr, &origStackSize);
            if (UNLIKELY(ret != 0)) {
                LOGE(LOG_TAG, "Fail to call pthread_attr_getstacksize, ret: %d", ret);
                return ret;
            }
        }
        const size_t defaultNativeStackSize = GetDefaultNativeStackSize();
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
    OnPThreadCreate(pthread_t *pthread_ptr, pthread_attr_t const *attr,
                    void *(*start_routine)(void *),
                    void *args) {
        if (attr == nullptr) {
            LOGD(LOG_TAG, "attr is null, skip adjusting.");
            return;
        }

        int ret = AdjustStackSize(const_cast<pthread_attr_t *>(attr));
        if (UNLIKELY(ret != 0)) {
            LOGE(LOG_TAG, "Fail to adjust stack size, ret: %d", ret);
        }
    }
}