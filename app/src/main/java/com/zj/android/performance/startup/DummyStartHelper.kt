package com.zj.android.performance.startup

import android.content.Context
import android.util.Log
import kotlin.random.Random

class DummyStartHelper {
    fun initCalculateTask(): Long {
        Log.i("DummyStartHelper", "initCalculateTask")
        var result = 0L
        for (i in 0..10000) {
            for (j in 0..100) {
                for (k in 0..2) {
                    val randomParameter = (0..1).random()
                    result = (i + j + k) * (i + j - k) * (i - j + k) * randomParameter.toLong()
                }
            }
        }
        Log.i("DummyStartHelper", "initCalculateTask End")
        return result
    }

    fun initSpTask(context: Context) {
        Log.i("DummyStartHelper", "initSpTask")
        for (i in 0 until 100) {
            val sp = context.getSharedPreferences("performance${i}", Context.MODE_PRIVATE)
            for (j in 0 until 200) {
                val editor = sp.edit()
                editor.putString("key${j}", "value${j}")
                editor.commit()
            }
        }
        Log.i("DummyStartHelper", "initSpTask End")
    }
}