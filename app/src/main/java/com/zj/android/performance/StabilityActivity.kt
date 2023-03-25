package com.zj.android.performance

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zj.android.stability.optimize.StabilityOptimize

class StabilityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stability)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.btn_java_crash).setOnClickListener {
            throw NullPointerException("test java exception")
        }
        findViewById<View>(R.id.btn_child_thread_java_crash).setOnClickListener {
            Thread {
                throw NullPointerException("test child thread java exception")
            }.start()
        }
        findViewById<View>(R.id.java_air_cushion).setOnClickListener {
            StabilityOptimize.setUpJavaExceptionHandler()
        }
    }
}