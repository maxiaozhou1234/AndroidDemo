package com.zhou.android.kotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Window
import com.zhou.android.common.GV
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.lang.reflect.Proxy
import java.util.concurrent.TimeUnit

/**
 * Created by mxz on 2019/6/5.
 */
class ActivityMonitor {

    private var recordTime = System.currentTimeMillis()
    private var disposable: Disposable? = null
    private var context: Context? = null

    private val uncheckList = arrayListOf<Any>()

    companion object {
        @JvmStatic
        fun get(): ActivityMonitor {
            return Holder.holder
        }
    }

    object Holder {
        @SuppressLint("StaticFieldLeak")
        val holder = ActivityMonitor()
    }

    fun attach(context: Context, list: ArrayList<*>) {
        Log.d("zhou", "attach $context")
        this@ActivityMonitor.context = context
        if (list.isNotEmpty()) {
            uncheckList.addAll(list)
        }
        Log.d("zhou", "ActivityMonitor >> $context")
    }

    private fun createDisposable(): Disposable {
        Log.d("zhou", "createDisposable")
        return Observable.interval(2, TimeUnit.SECONDS)
                .subscribe {
                    Log.d("zhou", "time === didi......")
                    val time = (System.currentTimeMillis() - recordTime) / 1000
                    if (time > 5) {
                        Log.d("zhou", "timeout...")
                        this@ActivityMonitor.context!!.sendBroadcast(Intent(GV.MONITOR_TIMEOUT))
                        disposable?.apply {
                            if (!isDisposed) {
                                dispose()
                            }
                            disposable = null
                        }
                    } else {
                        this@ActivityMonitor.context!!.sendBroadcast(Intent().apply {
                            action = GV.MONITOR_TIME_COUNT
                            putExtra("msg", "update >> $it current diff = $time")
                        })
                    }
                }
    }

    fun inject(clz: Class<Any>, window: Window) {
        if (uncheckList.contains(clz)) {
            cancel()
        } else {
            //代理模式
//            window.callback = MonitorCalback(window.callback)

            //动态代理
            val callback = window.callback
            val handler = WindowCallbackInvocation(callback)
            val proxy: Window.Callback = Proxy.newProxyInstance(Window.Callback::class.java.classLoader,
                    arrayOf(Window.Callback::class.java), handler) as Window.Callback
            window.callback = proxy
            update()
        }
    }

    fun update() {
        Log.d("zhou", "update operate time.")
        recordTime = System.currentTimeMillis()
        if (disposable == null) {
            disposable = createDisposable()
        }
        this@ActivityMonitor.context?.sendBroadcast(Intent().apply {
            action = GV.MONITOR_TIME_COUNT
            putExtra("msg", "on touch")
        })
    }

    private fun cancel() {
        Log.e("zhou", "cancel")
        disposable?.also {
            if (!it.isDisposed) {
                it.dispose()
            }
            disposable = null
        }
    }

    fun onDestroy(clz: Class<Any>) {
        if (uncheckList.contains(clz)) {
            update()
        } else {
            cancel()
        }
    }
}