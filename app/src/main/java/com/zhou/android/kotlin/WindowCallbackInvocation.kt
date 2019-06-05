package com.zhou.android.kotlin

import android.util.Log
import android.view.MotionEvent
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Created by mxz on 2019/6/5.
 */
class WindowCallbackInvocation(val callback: Any) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if ("dispatchTouchEvent" == method?.name) {
            Log.i("zhou", "WindowCallbackInvocation")
            val event: MotionEvent = args?.get(0) as MotionEvent
            if (MotionEvent.ACTION_UP == event.action) {
                ActivityMonitor.get().update()
            }
        }
        return method?.invoke(callback, *(args ?: arrayOfNulls<Any>(0)))
    }
}