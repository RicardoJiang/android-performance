package com.zj.android.performance

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zj.android.performance.jni.NativeLibTest
import com.zj.android.stability.optimize.StabilityOptimize

class StabilityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stability)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.java_airbag).setOnClickListener {
            StabilityOptimize.setUpJavaExceptionHandler()
        }
        findViewById<View>(R.id.native_airbag).setOnClickListener {
            StabilityOptimize.setUpNativeAirBag(
                11,
                "libandroid-performance.so",
                "Java_com_zj_android_performance_jni_NativeLibTest_nativeCrash1"
            )
        }
        findViewById<View>(R.id.btn_java_crash).setOnClickListener {
            throw NullPointerException("test java exception")
        }
        findViewById<View>(R.id.btn_child_thread_java_crash).setOnClickListener {
            Thread {
                throw NullPointerException("test child thread java exception")
            }.start()
        }
        findViewById<View>(R.id.btn_native_crash1).setOnClickListener {
            NativeLibTest().nativeCrash1()
        }
        findViewById<View>(R.id.btn_native_crash2).setOnClickListener {
            NativeLibTest().nativeCrash2()
        }
    }
}