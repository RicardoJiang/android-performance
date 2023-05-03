package com.zj.android.anr.monitor

internal class AnrNativeLib {

    /**
     * A native method that is implemented by the 'monitor' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun initAnrMonitor(): Unit

    companion object {
        // Used to load the 'monitor' library on application startup.
        init {
            System.loadLibrary("AnrMonitor")
        }
    }
}