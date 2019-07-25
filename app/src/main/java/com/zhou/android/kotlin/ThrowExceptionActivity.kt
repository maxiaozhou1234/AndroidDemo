package com.zhou.android.kotlin

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.Tools

/**
 * Created by mxz on 2019/7/25.
 */
class ThrowExceptionActivity : BaseActivity() {

    lateinit var receiver: ExceptionReceiver

    var fragment1 = TextFragment()
    var fragment2 = TextFragment()

    override fun setContentView() {
        setContentView(R.layout.activity_throw_exception)
    }

    override fun init() {
        Tools.printActivityStack(this)
        receiver = ExceptionReceiver()
        registerReceiver(receiver, IntentFilter("playSound"))

        supportFragmentManager.beginTransaction()
                .add(R.id.fragment1, fragment1)//在重建app后出现被重叠，即旧fragment没有被销毁
                .replace(R.id.fragment2, fragment2)//重建后，即使旧没有被销毁，也能把旧移除替换新的进去
                .commit()
    }

    override fun addListener() {
        findViewById<Button>(R.id.btnCopy).setOnClickListener {
            startActivity(Intent(ThrowExceptionActivity@ this, ThrowExceptionActivity::class.java))
        }
        findViewById<Button>(R.id.btnException).setOnClickListener {
            throw NullPointerException("Exception from ThrowExceptionActivity")
        }
        findViewById<Button>(R.id.btnError).setOnClickListener {
            throw Error("Error from ThrowExceptionActivity")
        }
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            sendBroadcast(Intent("playSound"))
        }
        findViewById<Button>(R.id.btnOther).setOnClickListener {
            startActivity(Intent(ThrowExceptionActivity@ this, AnalysisDecorActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("zhou", "ThrowExceptionActivity onDestroy")
        unregisterReceiver(receiver)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i("zhou", "onRestoreInstanceState")
        fragment1.setText("onRestoreInstanceState1")
        fragment2.setText("onRestoreInstanceState2")
    }
}