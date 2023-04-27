#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include "enhanced_dlfcn.h"
#include <sys/mman.h>
#include <sched.h>

#define LOG_TAG            "startup_optimize"
#define LOG(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)
static void **mSlot = nullptr;
static void *originFun = nullptr;

// 将虚函数表中的值替换成我们hook函数的地址
bool replaceFunc(void **slot, void *func) {
    //将内存页面设置成为可写
    void *page = (void *) ((size_t) slot & (~(size_t) (PAGE_SIZE - 1)));
    if (mprotect(page, PAGE_SIZE, PROT_READ | PROT_WRITE) != 0) return false;
    //将表中的值替换成我们自己的函数
    *slot = func;
#ifdef __arm__
    //刷新内存缓存，使虚函数表修改生效
    cacheflush((long)page, (long)page + PAGE_SIZE, 0);
#endif
    //将内存页面设置成为只读
    mprotect(page, PAGE_SIZE, PROT_READ);
    return true;
}

//我们的 hook 函数
void hookRun(void *thread) {
    //休眠3秒
    LOG("gc hook Run");
    sleep(3);
    //将虚函数表中的值还原成原函数，避免每次执行run函数时都会执行hook的方法
    replaceFunc(mSlot, originFun);
    LOG("execute origin fun %p", originFun);
    //执行原来的Run方法
    ((void (*)(void *)) originFun)(thread);
}

void delayGC() {
    //以RTLD_NOW模式打开动态库libart.so，拿到句柄，RTLD_NOW即解析出每个未定义变量的地址
    void *handle = enhanced_dlopen("/system/lib64/libart.so", RTLD_NOW);
    //通过符号拿到ConcurrentGCTask类首地址
    void *taskAddress = enhanced_dlsym(handle, "_ZTVN3art2gc4Heap16ConcurrentGCTaskE");
    //通过符号拿到run方法
    void *runAddress = enhanced_dlsym(handle, "_ZN3art2gc4Heap16ConcurrentGCTask3RunEPNS_6ThreadE");
    /*由于 ConcurrentGCTask 只有五个虚函数，所以我们只需要查询前五个地址即可。
    */
    int k = 5;
    for (size_t i = 0; i < k; i++) {
        /*对象头地址中的内容存放的就是是虚函数表的地址，所以这里是指针的指针，即是虚函数表地址
           拿到虚函数表地址后，转换成数组，并遍历获取值
        */
        void *vfunc = ((void **) taskAddress)[i];

        // 如果虚函数表中的值是前面拿到的 Run 函数的地址，那么就找到了Run函数在虚函数表中的地址
        if (vfunc == runAddress) {
            //这里需要注意的是，这里 +i 操作拿到的是地址，而不是值，因为这里的值是 Run 函数的真实地址
            mSlot = (void **) taskAddress + i;
        }
    }
    // 保存原有函数
    originFun = *mSlot;
    // 将虚函数表中的值替换成我们hook函数的地址
    replaceFunc(mSlot, (void *) &hookRun);
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