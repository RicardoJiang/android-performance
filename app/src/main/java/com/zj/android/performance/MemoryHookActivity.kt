package com.zj.android.performance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.zj.android.memory.hook.MemoryNativeLib
import com.zj.android.performance.jni.NativeLibTest

class MemoryHookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_hook)
        initView()
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_open_memory_hook).setOnClickListener {
            MemoryNativeLib().initHook()
        }
        findViewById<Button>(R.id.btn_malloc).setOnClickListener {
            NativeLibTest().testMalloc(88 * 1024 * 1024)
        }
        findViewById<Button>(R.id.btn_free).setOnClickListener {
            NativeLibTest().testFree()
        }
        findViewById<Button>(R.id.btn_dump).setOnClickListener {
            MemoryNativeLib().dump()
        }
    }
}