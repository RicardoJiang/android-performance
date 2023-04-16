package com.zj.android.performance.jank

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.metrics.performance.PerformanceMetricsState
import androidx.recyclerview.widget.RecyclerView
import com.zj.android.performance.R

class JankActivity : AppCompatActivity() {
    private var metricsStateHolder: PerformanceMetricsState.Holder? = null

    private val messageList = Array(100) { "Message #$it" }

    // [START state_recyclerview]
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            // check if JankStats is initialized and skip adding state if not
            val metricsState = metricsStateHolder?.state ?: return

            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    metricsState.putState("RecyclerView", "Dragging")
                }
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    metricsState.putState("RecyclerView", "Settling")
                }
                else -> {
                    metricsState.removeState("RecyclerView")
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jank)
        metricsStateHolder = PerformanceMetricsState.getHolderForHierarchy(window.decorView)
        initView()
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.messageList)
        recyclerView.adapter = MessageListAdapter(messageList)
        recyclerView.addOnScrollListener(scrollListener)
    }
}