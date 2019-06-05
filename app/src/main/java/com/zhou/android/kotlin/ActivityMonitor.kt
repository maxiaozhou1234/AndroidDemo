package com.zhou.android.kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zhou.android.common.GV
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by mxz on 2019/6/5.
 */
class ActivityMonitor {

    private var recordTime = System.currentTimeMillis()
    private var disposable: Disposable? = null
    private var context: Context? = null

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

    fun attach(context: Context) {
        Log.d("zhou", "attach $context")
        this@ActivityMonitor.context = context
        Log.d("zhou", "ActivityMonitor >> $context")
    }

    private fun createDisposable(): Disposable {
        Log.d("zhou", "createDisposable")
        return Observable.interval(2, TimeUnit.SECONDS)
                .subscribe {
                    Log.d("zhou", "time === didi......")
                    if ((System.currentTimeMillis() - recordTime) / 1000 > 5) {
                        Log.d("zhou", "timeout...")
                        this@ActivityMonitor.context!!.sendBroadcast(Intent(GV.MONITOR_TIMEOUT))
                        disposable?.apply {
                            if (!isDisposed) {
                                dispose()
                            }
                            disposable = null
                        }
                    }
                }
    }

    fun update() {
        Log.d("zhou", "update operate time.")
        recordTime = System.currentTimeMillis()
        if (disposable == null) {
            disposable = createDisposable()
        }
    }

    fun cancel() {
        Log.e("zhou","cancel")
        disposable?.also {
            if (!it.isDisposed) {
                it.dispose()
            }
            disposable = null
        }
    }

}