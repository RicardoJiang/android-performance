package com.zj.android.jank.optimize

import android.view.Window

internal open class WindowCallbackWrapper(private val originCallback: Window.Callback) :
    Window.Callback by originCallback