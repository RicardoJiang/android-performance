package com.zj.android.performance

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        Log.i(
            "DummyStartHelper",
            Debug.getRuntimeStat("art.gc.gc-count") + "," +
                    Debug.getRuntimeStat("art.gc.gc-time") + "," + Debug.getRuntimeStat(
                "art.gc.blocking-gc-count"
            ) + "," + Debug.getRuntimeStat("art.gc.blocking-gc-time")
        )
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_thread_hook).setOnClickListener {
            startActivity(Intent(this, ThreadHookActivity::class.java))
        }
        findViewById<Button>(R.id.btn_memory_hook).setOnClickListener {
            startActivity(Intent(this, MemoryHookActivity::class.java))
        }
        findViewById<Button>(R.id.btn_stability_optimization).setOnClickListener {
            startActivity(Intent(this, StabilityActivity::class.java))
        }
    }
}