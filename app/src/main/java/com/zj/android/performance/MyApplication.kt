package com.zj.android.performance

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.zj.android.performance.startup.DummyStartHelper
import com.zj.android.startup.optimize.StartupOptimize

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("startup_optimize", "application onCreate")
        StartupOptimize.delayGC()
        DummyStartHelper().initCalculateTask()
        DummyStartHelper().initSpTask(this)
        DummyStartHelper().initTriggerGcTask(this)
    }
}