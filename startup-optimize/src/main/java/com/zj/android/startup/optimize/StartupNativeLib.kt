package com.zj.android.startup.optimize

import com.bytedance.shadowhook.ShadowHook

internal class StartupNativeLib {

    /**
     * A native method that is implemented by the 'optimize' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun delayGC(): Unit

    external fun bindCore(threadId: Int, core: Int): Unit

    companion object {
        // Used to load the 'optimize' library on application startup.
        init {
            ShadowHook.init(
                ShadowHook.ConfigBuilder().setMode(ShadowHook.Mode.UNIQUE).setDebuggable(true).build()
            )
            System.loadLibrary("startup-optimize")
        }
    }
}