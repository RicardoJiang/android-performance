package com.zj.android.startup.optimize

internal class StartupNativeLib {

    /**
     * A native method that is implemented by the 'optimize' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun delayGC(): Unit

    companion object {
        // Used to load the 'optimize' library on application startup.
        init {
            System.loadLibrary("startup-optimize")
        }
    }
}