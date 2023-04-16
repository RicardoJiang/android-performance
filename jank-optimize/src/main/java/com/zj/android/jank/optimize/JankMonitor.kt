package com.zj.android.jank.optimize

import android.app.Application
import android.os.Looper

object JankMonitor {
    internal const val JANK_MULTIPILER = 3f // 掉帧数超过 3 定义为卡顿
    internal const val SLIGHT_JANK_MULTIPIER = 9f // 掉帧数在 3 到 9 之间定义为轻微卡顿
    internal const val MIDDLE_JANK_MULTIPIER = 24f // 掉帧数在 9 到 24 之间定义为中等卡顿
    internal const val CRITICAL_JANK_MULTIPIER = 42f // 掉帧数在 24 到 42 之间定义为严重卡顿
    // 掉帧数超过 42 定义为冻结帧

    private val monitorCore by lazy {
        MonitorCore()
    }

    fun init(application: Application, dumpJankStacktrace: Boolean = true) {
        application.registerActivityLifecycleCallbacks(JankActivityLifecycleCallback())
        if (dumpJankStacktrace) {
            Looper.getMainLooper().setMessageLogging(monitorCore)
        }
    }
}