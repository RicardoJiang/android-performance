package com.zj.android.jank.optimize

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.metrics.performance.PerformanceMetricsState

internal class JankActivityLifecycleCallback : ActivityLifecycleCallbacks {
    private var curActivity: Activity? = null
    private val jankAggregatorMap by lazy {
        hashMapOf<String, JankStatsAggregator>()
    }

    private val singleFrameNanosDuration by lazy {
        1000 / getRefreshRate() * 1_000_000
    }

    private val jankReportListener by lazy {
        JankStatsAggregator.OnJankReportListener { reason, totalFrames, jankFrameData ->
            var slightJankCount = 0
            var middleJankCount = 0
            var criticalJankCount = 0
            var frozenJankCount = 0

            jankFrameData.forEach { frameData ->
                Log.v("JankMonitor", frameData.toString())
                val dropFrameCount = frameData.frameDurationUiNanos / singleFrameNanosDuration
                if (dropFrameCount <= JankMonitor.SLIGHT_JANK_MULTIPIER) {
                    slightJankCount++
                } else if (dropFrameCount <= JankMonitor.MIDDLE_JANK_MULTIPIER) {
                    middleJankCount++
                } else if (dropFrameCount <= JankMonitor.CRITICAL_JANK_MULTIPIER) {
                    criticalJankCount++
                } else {
                    frozenJankCount++
                }
            }
            Log.v(
                "JankMonitor",
                "*** Jank Report ($reason), " +
                        "totalFrames = $totalFrames, " +
                        "jankFrames = ${jankFrameData.size}, " +
                        "slightJankCount = $slightJankCount, " +
                        "middleJankCount = $middleJankCount, " +
                        "criticalJankCount = $criticalJankCount, " +
                        "frozenJankCount = $frozenJankCount"
            )
        }
    }

    private fun getRefreshRate(): Float {
        val window = curActivity?.window ?: return 60f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return window.context.display?.refreshRate ?: 60f
        }
        return window.windowManager.defaultDisplay.refreshRate
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        curActivity = activity
        val originCallback = activity.window.callback
        activity.window.callback = object : WindowCallbackWrapper(originCallback) {
            override fun onContentChanged() {
                val activityName = activity.javaClass.simpleName
                if (!jankAggregatorMap.containsKey(activityName)) {
                    val jankAggregator = JankStatsAggregator(activity.window, jankReportListener)
                    PerformanceMetricsState.getHolderForHierarchy(activity.window.decorView).state?.putState(
                        "Activity",
                        activityName
                    )
                    jankAggregatorMap[activityName] = jankAggregator
                }
            }
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
        jankAggregatorMap[activity.javaClass.simpleName]?.let {
            it.jankStats.isTrackingEnabled = true
        }
    }

    override fun onActivityPaused(activity: Activity) {
        jankAggregatorMap[activity.javaClass.simpleName]?.let {
            it.issueJankReport("Activity paused")
            it.jankStats.isTrackingEnabled = false
        }
    }

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        jankAggregatorMap.remove(activity.javaClass.simpleName)
    }
}