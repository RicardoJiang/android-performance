package com.zj.android.thread.hook

class ThreadHookNativeLib {

    /**
     * A native method that is implemented by the 'hook' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'hook' library on application startup.
        init {
            System.loadLibrary("ThreadHook")
        }
    }
}