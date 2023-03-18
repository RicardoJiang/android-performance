package com.zj.android.performance.jni

class NativeLibTest {
    external fun stringFromJNI(): String
    external fun testMalloc(size: Long)

    external fun testFree()

    companion object {
        // Used to load the 'hook' library on application startup.
        init {
            System.loadLibrary("android-performance")
        }
    }
}