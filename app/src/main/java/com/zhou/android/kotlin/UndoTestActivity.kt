package com.zhou.android.kotlin

import android.app.ActivityManager
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.GV
import com.zhou.android.common.ToastUtils

/**
 * Created by mxz on 2019/6/5.
 */
class UndoTestActivity : BaseActivity() {

    lateinit var text: TextView
    lateinit var btn: Button

    override fun setContentView() {
        setContentView(R.layout.activity_undo)
    }

    override fun init() {
        text = findViewById(R.id.text)
        btn = findViewById(R.id.popup)
//        Log.d("zhou", window.callback.toString())
//        window.callback = Callback(window.callback)//替换原先的 PhoneWindow Callback，缺点做一个默认callback代理

        //动态代理
//        val callback = window.callback
//        val handler = WindowCallbackInvocation(callback)
//        val proxy: Window.Callback = Proxy.newProxyInstance(Window.Callback::class.java.classLoader, arrayOf(Window.Callback::class.java), handler) as Window.Callback
//        window.callback = proxy
//        Log.i("zhou", "proxy >> ${window.callback}")

        registerReceiver(receiver, IntentFilter().apply {
            addAction(GV.MONITOR_TIMEOUT)
            addAction(GV.MONITOR_TIME_COUNT)
        })
        ActivityMonitor.get().inject(this.javaClass, window)
    }

    override fun addListener() {
        findViewById<View>(R.id.dialog).setOnClickListener {
            val dialog = Dialog(this@UndoTestActivity).apply {
                setContentView(R.layout.layout_undo_dialog)
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                findViewById<View>(R.id.btnOk).setOnClickListener {
                    this.dismiss()
                }
            }
            dialog.show()

        }

        findViewById<View>(R.id.alertDialog).setOnClickListener {
            AlertDialog.Builder(this@UndoTestActivity)
                    .setTitle("Alert Dialog")
                    .setMessage("Show Dialog")
                    .setPositiveButton("Yes") { dialog: DialogInterface, _ ->
                        dialog.dismiss()
                    }.create().show()
        }

        findViewById<View>(R.id.next).setOnClickListener {
            startActivity(Intent(this@UndoTestActivity, SimpleListKotlinActivity::class.java))
        }

        btn.setOnClickListener {
            val popup = PopupWindow(this@UndoTestActivity).apply {
                setBackgroundDrawable(ColorDrawable(Color.parseColor("#C3C3C3")))
                contentView = LayoutInflater.from(this@UndoTestActivity).inflate(R.layout.layout_undo_pop, null)
            }
            popup.showAsDropDown(btn)
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        ActivityMonitor.get().inject(this,window)
//    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        ActivityMonitor.get().onDestroy(this.javaClass)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.also {
                when (it.action) {
                    GV.MONITOR_TIME_COUNT -> {
                        val t = "${it.getStringExtra("msg")}\n"
                        text.append(t)
                    }
                    GV.MONITOR_TIMEOUT -> {
                        Log.i("zhou", "UndoTestActivity received msg,finish")
                        ToastUtils.show(this@UndoTestActivity, "timeout,finish!!")
//                        finish()
                        //这里在默认的主页做，我们做个不一样的操作
                        //如果当前这个是栈顶，我们就关闭自己，不在栈顶，就把前面的弹出栈
                        val am: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        val list = am.getRunningTasks(1)
                        if (list != null && list.isNotEmpty()) {
                            if (this@UndoTestActivity.javaClass.name == list[0].topActivity.className) {
                                finish()
                            } else {
                                //这里会重启倒计时，如果在主页其实在白名单中是不会重计时的
                                context?.startActivity(Intent(context, UndoTestActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                })
                            }
                        }
                    }
                }
            }
        }
    }

}