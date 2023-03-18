package com.zj.android.performance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_thread_hook).setOnClickListener {
            startActivity(Intent(this, ThreadHookActivity::class.java))
        }
        findViewById<Button>(R.id.btn_memory_hook).setOnClickListener {
            startActivity(Intent(this, MemoryHookActivity::class.java))
        }
    }
}