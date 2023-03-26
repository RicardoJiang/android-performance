package com.zj.android.stability.optimize

import android.os.Build
import android.os.Looper
import android.util.Log

object StabilityOptimize {
    fun setUpJavaAirBag(configList: List<JavaAirBagConfig>) {
        Log.i("StabilityOptimize", "Java 安全气囊已开启")
        val preDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            handleException(preDefaultExceptionHandler, configList, thread, exception)
            if (thread == Looper.getMainLooper().thread) {
                while (true) {
                    try {
                        Looper.loop()
                    } catch (e: Throwable) {
                        handleException(
                            preDefaultExceptionHandler,
                            configList,
                            Thread.currentThread(),
                            e
                        )
                    }
                }
            }
        }
    }

    fun setUpNativeAirBag(signal: Int, soName: String, backtrace: String) {
        Log.i("StabilityOptimize", "Native 安全气囊已开启")
        StabilityNativeLib().openNativeAirBag(signal, soName, backtrace)
    }

    private fun handleException(
        preDefaultExceptionHandler: Thread.UncaughtExceptionHandler,
        configList: List<JavaAirBagConfig>,
        thread: Thread,
        exception: Throwable
    ) {
        if (configList.any { isStackTraceMatching(exception, it) }) {
            Log.w("StabilityOptimize", "Java Crash 已捕获")
            Log.w("StabilityOptimize", "FATAL EXCEPTION: ${thread.name}")
            Log.w("StabilityOptimize", exception.message ?: "")
        } else {
            Log.w("StabilityOptimize", "Java Crash 未捕获，交给原有 ExceptionHandler 处理")
            preDefaultExceptionHandler.uncaughtException(thread, exception)
        }
    }

    private fun isStackTraceMatching(exception: Throwable, config: JavaAirBagConfig): Boolean {
        val stackTraceElement = exception.stackTrace[0]
        return config.crashType == exception.javaClass.name
                && config.crashMessage == exception.message
                && config.crashClass == stackTraceElement?.className
                && config.crashMethod == stackTraceElement.methodName
                && (config.crashAndroidVersion == 0 || (config.crashAndroidVersion == Build.VERSION.SDK_INT))
    }
}