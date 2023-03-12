package com.zj.android.thread.hook

import com.bytedance.android.bytehook.ByteHook

class ThreadHookNativeLib {

    fun initHook() {
        ByteHook.init()
        hookThread()
    }

    external fun hookThread()

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