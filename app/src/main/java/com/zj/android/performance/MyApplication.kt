package com.zj.android.performance

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.zj.android.performance.startup.DummyStartHelper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DummyStartHelper().initCalculateTask()
        DummyStartHelper().initSpTask(this)
        DummyStartHelper().initTriggerGcTask(this)
    }
}