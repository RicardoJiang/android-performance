package com.zj.android.performance

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zj.android.thread.hook.ThreadHookNativeLib
import kotlin.concurrent.thread

class ThreadHookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread_hook)
        initView()
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_open_thread_hook).setOnClickListener {
            ThreadHookNativeLib().initHook()
        }
        findViewById<Button>(R.id.btn_test_thread).setOnClickListener {
            testThread()
        }
    }

    private fun testThread() {
        for (i in 0 until 100) {
            thread(start = true) {
                while (true) {
                    Thread.sleep(1000)
                }
            }
        }
    }
}