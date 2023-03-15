package com.zj.android.performance

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_thread_test).setOnClickListener {
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