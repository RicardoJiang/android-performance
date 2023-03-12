package com.zj.android.performance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zj.android.thread.hook.ThreadHookNativeLib

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("here:"+ThreadHookNativeLib().stringFromJNI())
    }
}