#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include "bytehook.h"
#include <unwind.h>

#define LOG_TAG            "memory_hook"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)

struct backtrace_stack {
    void **current;
    void **end;
};

static _Unwind_Reason_Code unwind_callback(struct _Unwind_Context *context, void *data) {
    auto *state = (struct backtrace_stack *) (data);
    uintptr_t pc = _Unwind_GetIP(context);  // 获取 pc 值，即绝对地址
    if (pc) {
        if (state->current == state->end) {
            return _URC_END_OF_STACK;
        } else {
            *state->current++ = (void *) (pc);
        }
    }
    return _URC_NO_REASON;
}

static size_t fill_backtraces_buffer(void **buffer, int max) {
    struct backtrace_stack stack = {buffer, buffer + max};
    _Unwind_Backtrace(unwind_callback, &stack);
    return stack.current - buffer;
}

void dumpBacktrace(void **buffer, size_t count) {
    for (int i = 0; i < count; ++i) {
        void *addr = buffer[i];
        Dl_info info = {};
        if (dladdr(addr, &info)) {
            const uintptr_t address_relative = (uintptr_t) addr - (uintptr_t) info.dli_fbase;
            LOG("# %d : %p : %s(%p)(%s)(%p)", i, addr, info.dli_fname, address_relative,
                info.dli_sname, info.dli_saddr);
        }
    }
}


void *malloc_proxy(size_t len) {
    BYTEHOOK_STACK_SCOPE();
    if (len > 80 * 1024 * 1024) {
        // 获取堆栈
        int maxStackSize = 30;
        void *buffer[maxStackSize];
        int count = fill_backtraces_buffer(buffer, maxStackSize);
        dumpBacktrace(buffer, count);
    }
    return BYTEHOOK_CALL_PREV(malloc_proxy, len);
}

void hookMemory() {
    LOG("hookMemory");
    bytehook_hook_all(nullptr, "malloc", (void *) malloc_proxy,
                      nullptr,
                      nullptr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_memory_hook_MemoryNativeLib_hookMemory(
        JNIEnv *env,
        jobject /* this */) {
    hookMemory();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_memory_hook_MemoryNativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}