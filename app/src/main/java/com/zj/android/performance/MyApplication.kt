package com.zj.android.performance

import android.app.Application
import com.zj.android.thread.hook.ThreadHookNativeLib

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThreadHookNativeLib().initHook()
    }
}