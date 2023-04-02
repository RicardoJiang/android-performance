package com.zj.android.startup.optimize

import android.os.Build

object StartupOptimize {
    fun delayGC() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            StartupNativeLib().delayGC()
        }
    }
}