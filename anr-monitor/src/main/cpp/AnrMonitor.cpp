#include <jni.h>
#include <string>
#include <android/log.h>
#include <mutex>
#include <unistd.h>
#include <dirent.h>
#include "Support.h"
#include <fcntl.h>
#include <syscall.h>
#include <cinttypes>
#define LOG_TAG            "anr_monitor"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_zj_android_anr_monitor_AnrNativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

static sigset_t old_sigSet;
static const int TARGET_SIG = SIGQUIT;
struct sigaction sOldHandlers;
#define SIGNAL_CATCHER_THREAD_NAME "Signal Catcher"
#define SIGNAL_CATCHER_THREAD_SIGBLK 0x1000
JavaVM *javaVm = NULL;

static struct StacktraceJNI {
    jclass AnrDetective;

    jmethodID AnrDetector_onANRDumped;
} gJ;

static int getSignalCatcherThreadId() {
    char taskDirPath[128];
    DIR *taskDir;
    long long sigblk;
    int signalCatcherTid = -1;
    int firstSignalCatcherTid = -1;

    snprintf(taskDirPath, sizeof(taskDirPath), "/proc/%d/task", getpid());
    if ((taskDir = opendir(taskDirPath)) == nullptr) {
        return -1;
    }
    struct dirent *dent;
    pid_t tid;
    while ((dent = readdir(taskDir)) != nullptr) {
        tid = atoi(dent->d_name);
        if (tid <= 0) {
            continue;
        }

        char threadName[1024];
        char commFilePath[1024];
        snprintf(commFilePath, sizeof(commFilePath), "/proc/%d/task/%d/comm", getpid(), tid);

        Support::readFileAsString(commFilePath, threadName, sizeof(threadName));

        if (strncmp(SIGNAL_CATCHER_THREAD_NAME, threadName , sizeof(SIGNAL_CATCHER_THREAD_NAME)-1) != 0) {
            continue;
        }

        if (firstSignalCatcherTid == -1) {
            firstSignalCatcherTid = tid;
        }

        sigblk = 0;
        char taskPath[128];
        snprintf(taskPath, sizeof(taskPath), "/proc/%d/status", tid);

        ScopedFileDescriptor fd(open(taskPath, O_RDONLY, 0));
        LineReader lr(fd.get());
        const char *line;
        size_t len;
        while (lr.getNextLine(&line, &len)) {
            if (1 == sscanf(line, "SigBlk: %" SCNx64, &sigblk)) {
                break;
            }
            lr.popLine(len);
        }
        if (SIGNAL_CATCHER_THREAD_SIGBLK != sigblk) {
            continue;
        }
        signalCatcherTid = tid;
        break;
    }
    closedir(taskDir);

    if (signalCatcherTid == -1) {
        signalCatcherTid = firstSignalCatcherTid;
    }
    return signalCatcherTid;
}

static void sendSigToSignalCatcher() {
    int tid = getSignalCatcherThreadId();
    syscall(SYS_tgkill, getpid(), tid, SIGQUIT);
}

static void *anrCallback(void* arg) {
//    anrDumpCallback();
//    if (strlen(mAnrTraceFile) > 0) {
//        hookAnrTraceWrite(false);
//    }
    JNIEnv *env = NULL;
    if(JNI_OK != (*javaVm).AttachCurrentThread(&env,NULL)){
        return NULL;
    }
    env->CallStaticVoidMethod(gJ.AnrDetective, gJ.AnrDetector_onANRDumped);
    sendSigToSignalCatcher();
    return nullptr;
}

static void *siUserCallback(void* arg) {
    sendSigToSignalCatcher();
    return nullptr;
}

void signalHandler(int sig, siginfo_t* info, void* uc) {
    int fromPid1 = info->_si_pad[3];
    int fromPid2 = info->_si_pad[4];
    int myPid = getpid();
    bool fromMySelf = fromPid1 == myPid || fromPid2 == myPid;
    if (sig == SIGQUIT) {
        pthread_t thd;
        if (!fromMySelf) {
            pthread_create(&thd, nullptr, anrCallback, nullptr);
        } else {
            pthread_create(&thd, nullptr, siUserCallback, nullptr);
        }
        pthread_detach(thd);
    }
}

bool installHandlersLocked() {
    if (sigaction(TARGET_SIG, nullptr, &sOldHandlers) == -1) {
        return false;
    }

    struct sigaction sa{};
    sa.sa_sigaction = signalHandler;
    sa.sa_flags = SA_ONSTACK | SA_SIGINFO | SA_RESTART;

    if (sigaction(TARGET_SIG, &sa, nullptr) == -1) {
        return false;
    }
    return true;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVm = vm;
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK)
        return -1;

    jclass anrDetectiveCls = env->FindClass("com/zj/android/anr/monitor/AnrTracer");
    if (!anrDetectiveCls)
        return -1;
    gJ.AnrDetective = static_cast<jclass>(env->NewGlobalRef(anrDetectiveCls));
    gJ.AnrDetector_onANRDumped =
            env->GetStaticMethodID(anrDetectiveCls, "onANRDumped", "()V");
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL
Java_com_zj_android_anr_monitor_AnrNativeLib_initAnrMonitor(
        JNIEnv* env,
        jobject /* this */) {
    sigset_t sigSet;
    sigemptyset(&sigSet);
    sigaddset(&sigSet, SIGQUIT);
    pthread_sigmask(SIG_UNBLOCK, &sigSet , &old_sigSet);
    auto installSuccess = installHandlersLocked();
    if (installSuccess){
        LOG("init succeess");
    }
}