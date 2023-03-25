package com.zj.android.stability.optimize

internal class StabilityNativeLib {

    /**
     * A native method that is implemented by the 'optimize' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'optimize' library on application startup.
        init {
            System.loadLibrary("optimize")
        }
    }
}