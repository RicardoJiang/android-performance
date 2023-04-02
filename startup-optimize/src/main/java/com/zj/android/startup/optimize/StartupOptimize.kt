package com.zj.android.startup.optimize

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader

object StartupOptimize {
    fun delayGC() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            StartupNativeLib().delayGC()
        }
    }

    fun bindCore() {
        val threadId = getRenderThreadTid()
        val cpuCores = getNumberOfCPUCores()
        val cpuFreqList = (0 until cpuCores).map {
            Pair(it, getCpuFreq(it))
        }.sortedByDescending {
            it.second
        }
        StartupNativeLib().bindCore(threadId, cpuFreqList[1].first)
    }

    private fun getCpuFreq(core: Int): Int {
        val filename = "/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq"
        val cpuInfoMaxFreqFile = File(filename)
        if (cpuInfoMaxFreqFile.exists()) {
            val buffer = ByteArray(128)
            val stream = FileInputStream(cpuInfoMaxFreqFile)
            try {
                stream.read(buffer)
                var endIndex = 0
                //Trim the first number out of the byte buffer.
                while (buffer[endIndex] >= '0'.code.toByte() && buffer[endIndex] <= '9'.code.toByte() && endIndex < buffer.size) endIndex++
                val str = String(buffer, 0, endIndex)
                return str.toInt()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            } finally {
                stream.close()
            }
        }
        return 0
    }

    private fun getNumberOfCPUCores(): Int {
        return File("/sys/devices/system/cpu/").listFiles { file: File ->
            val path = file.name
            if (path.startsWith("cpu")) {
                for (i in 3 until path.length) {
                    if (path[i] < '0' || path[i] > '9') {
                        return@listFiles false
                    }
                }
                return@listFiles true
            }
            false
        }?.size ?: 0
    }

    private fun getRenderThreadTid(): Int {
        val taskParent = File("/proc/" + android.os.Process.myPid() + "/task/")
        if (taskParent.isDirectory) {
            val taskFiles = taskParent.listFiles()
            for (taskFile in taskFiles) {
                //读线程名
                var br: BufferedReader? = null
                var cpuRate = ""
                try {
                    br = BufferedReader(FileReader(taskFile.path + "/stat"), 100)
                    cpuRate = br.readLine()
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                } finally {
                    br?.close()
                }
                if (cpuRate.isNotEmpty()) {
                    val param = cpuRate.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (param.size < 2) {
                        continue
                    }
                    val threadName = param[1]
                    //找到name为RenderThread的线程，则返回第0个数据就是 tid
                    if (threadName == "(RenderThread)") {
                        return param[0].toInt()
                    }
                }
            }
        }
        return -1
    }
}