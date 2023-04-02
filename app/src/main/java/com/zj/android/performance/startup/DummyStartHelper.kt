package com.zj.android.performance.startup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.webkit.WebView

class DummyStartHelper {
    fun initCalculateTask(): Long {
        var result = 0L
        for (i in 0..1000) {
            for (j in 0..100) {
                for (k in 0..2) {
                    val randomParameter = (0..1).random()
                    result = (i + j + k) * (i + j - k) * (i - j + k) * randomParameter.toLong()
                }
            }
        }
        return result
    }

    fun initSpTask(context: Context) {
        for (i in 0 until 100) {
            val sp = context.getSharedPreferences("performance${i}", Context.MODE_PRIVATE)
            for (j in 0 until 20) {
                val editor = sp.edit()
                editor.putString("key${j}", "value${j}")
                editor.commit()
            }
        }
    }

    fun initTriggerGcTask(context: Context) {
        val webViewList = mutableListOf<WebView>()
        for (i in 0 until 10) {
            val webView = WebView(context)
            webViewList.add(webView)
        }
        webViewList.clear()
        val bitmapList = mutableListOf<Bitmap>()
        for (i in 0 until 100) {
            val bitmap = Bitmap.createBitmap(1000, 100, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.RED)
            bitmapList.add(bitmap)
        }
        bitmapList.clear()
    }
}