package com.zj.android.performance

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zj.android.startup.optimize.StartupOptimize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        StartupOptimize.bindCore()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(
                "startup_optimize",
                """
                gcCount: ${Debug.getRuntimeStat("art.gc.gc-count")}
                gc-time: ${Debug.getRuntimeStat("art.gc.gc-time")}
                blocking-gc-count: ${Debug.getRuntimeStat("art.gc.blocking-gc-count")}
                art.gc.blocking-gc-time: ${Debug.getRuntimeStat("art.gc.blocking-gc-time")}
            """.trimIndent()
            )
        }
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