#include <jni.h>
#include <string>
#include <android/log.h>
#include "unwind-utils.h"

#define LOG_TAG            "native_airbag"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)
#define SIGNAL_CRASH_STACK_SIZE (1024 * 128)

static struct sigaction old;

struct NativeAirBagConfig {
    int signal;
    std::string soName;
    std::string backtrace;
};

static NativeAirBagConfig airBagConfig;

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_stability_optimize_StabilityNativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

static void sig_handler(int sig, struct siginfo *info, void *ptr) {
    auto stackTrace = getStackTraceWhenCrash();
    if (sig == airBagConfig.signal &&
        stackTrace.find(airBagConfig.soName) != std::string::npos &&
        stackTrace.find(airBagConfig.backtrace) != std::string::npos) {
        LOG("异常信号已捕获");
    } else {
        LOG("异常信号交给原有信号处理器处理");
        sigaction(sig, &old, nullptr);
        raise(sig);
    }
}

void handle_exception(JNIEnv *env) {
    LOG("native air bag init failed");
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_stability_optimize_StabilityNativeLib_openNativeAirBag(
        JNIEnv *env,
        jobject /* this */,
        jint signal,
        jstring soName,
        jstring backtrace) {
    airBagConfig.signal = signal;
    airBagConfig.soName = env->GetStringUTFChars(soName, 0);
    airBagConfig.backtrace = env->GetStringUTFChars(backtrace, 0);
    do {
        stack_t ss;
        if (nullptr == (ss.ss_sp = calloc(1, SIGNAL_CRASH_STACK_SIZE))) {
            handle_exception(env);
            break;
        }
        ss.ss_size = SIGNAL_CRASH_STACK_SIZE;
        ss.ss_flags = 0;
        if (0 != sigaltstack(&ss, nullptr)) {
            handle_exception(env);
            break;
        }
        struct sigaction sigc;
        sigc.sa_sigaction = sig_handler;
        sigemptyset(&sigc.sa_mask);
        // 推荐采用SA_RESTART 虽然不是所有系统调用都支持，被中断后重新启动，但是能覆盖大部分
        sigc.sa_flags = SA_SIGINFO | SA_ONSTACK | SA_RESTART;
        int flag = sigaction(signal, &sigc, &old);
        if (flag == -1) {
            handle_exception(env);
            break;
        }
    } while (false);
}