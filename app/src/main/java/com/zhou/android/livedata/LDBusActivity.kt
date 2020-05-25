package com.zhou.android.livedata

import android.arch.lifecycle.Observer
import android.util.Log
import android.view.View
import android.widget.Button
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.toast

/**
 * Created by mxz on 2020/5/25.
 */
class LDBusActivity : BaseActivity() {
    override fun setContentView() {
        setContentView(R.layout.activity_bus)
    }

    override fun addListener() {

    }

    override fun init() {
        LiveEventBus.getChannel("test").observe(this, Observer { value ->
            Log.d("liveBus", "onChange -- $value")
        })
    }

    fun onClick(view: View) {
        if (R.id.btn5 == view.id) {
            LiveEventBus.getChannel("test", String::class.java).observe(this, Observer { v ->
                toast(v!!)
            })
        } else {
            LiveEventBus.getChannel("test").postValue((view as Button).text)
        }
    }
}