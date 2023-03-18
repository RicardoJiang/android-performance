package com.zj.android.memory.hook

import com.bytedance.android.bytehook.ByteHook

class MemoryNativeLib {

    fun initHook() {
        ByteHook.init()
        hookMemory()
    }

    private external fun hookMemory()

    companion object {
        // Used to load the 'hook' library on application startup.
        init {
            System.loadLibrary("memory-hook")
        }
    }
}