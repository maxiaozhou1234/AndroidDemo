package com.zhou.android.kotlin

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.GV
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * Created by mxz on 2019/6/5.
 */
class UndoTestActivity : BaseActivity() {

    override fun setContentView() {
        setContentView(R.layout.activity_undo)
    }

    override fun init() {
        Log.d("zhou", window.callback.toString())
//        window.callback = Callback(window.callback)//替换原先的 PhoneWindow Callback，缺点做一个默认callback代理

        //动态代理
        val callback = window.callback
        val handler = WindowCallbackInvocation(callback)
        val proxy: Window.Callback = Proxy.newProxyInstance(Window.Callback::class.java.classLoader, arrayOf(Window.Callback::class.java), handler) as Window.Callback
        window.callback = proxy
        Log.i("zhou", "proxy >> ${window.callback}")

        registerReceiver(receiver, IntentFilter(GV.MONITOR_TIMEOUT))
        ActivityMonitor.get().update()
    }

    override fun addListener() {
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        ActivityMonitor.get().cancel()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (GV.MONITOR_TIMEOUT == intent?.action) {
                Log.i("zhou", "UndoTestActivity received msg,finish")
                finish()
            }
        }
    }

    class Callback(val default: Window.Callback) : Window.Callback {
        override fun onActionModeFinished(mode: ActionMode?) {
            default.onActionModeFinished(mode)
        }

        override fun onCreatePanelView(featureId: Int): View? {
            return default.onCreatePanelView(featureId)
        }

        override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
            if (MotionEvent.ACTION_UP == event?.action) {
                ActivityMonitor.get().update()
            }
            return default.dispatchTouchEvent(event)
        }

        override fun onCreatePanelMenu(featureId: Int, menu: Menu?): Boolean = default.onCreatePanelMenu(featureId, menu)

        override fun onWindowStartingActionMode(callback: ActionMode.Callback?): ActionMode {
            return default.onWindowStartingActionMode(callback)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onWindowStartingActionMode(callback: ActionMode.Callback?, type: Int): ActionMode {
            return default.onWindowStartingActionMode(callback, type)
        }

        override fun onAttachedToWindow() {
            default.onAttachedToWindow()
        }

        override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean = default.dispatchGenericMotionEvent(event)

        override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean = default.dispatchPopulateAccessibilityEvent(event)

        override fun dispatchTrackballEvent(event: MotionEvent?): Boolean = default.dispatchTrackballEvent(event)
        override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean = default.dispatchKeyShortcutEvent(event)

        override fun dispatchKeyEvent(event: KeyEvent?): Boolean = default.dispatchKeyEvent(event)

        override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean = default.onMenuOpened(featureId, menu)

        override fun onPanelClosed(featureId: Int, menu: Menu?) {
            default.onPanelClosed(featureId, menu)
        }

        override fun onMenuItemSelected(featureId: Int, item: MenuItem?): Boolean = default.onMenuItemSelected(featureId, item)

        override fun onDetachedFromWindow() {
            default.onDetachedFromWindow()
        }

        override fun onPreparePanel(featureId: Int, view: View?, menu: Menu?): Boolean = default.onPreparePanel(featureId, view, menu)

        override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams?) {
            default.onWindowAttributesChanged(attrs)
        }

        override fun onWindowFocusChanged(hasFocus: Boolean) {
            default.onWindowFocusChanged(hasFocus)
        }

        override fun onContentChanged() {
            default.onContentChanged()
        }

        override fun onSearchRequested(): Boolean = default.onSearchRequested()

        @TargetApi(Build.VERSION_CODES.M)
        override fun onSearchRequested(searchEvent: SearchEvent?): Boolean = default.onSearchRequested(searchEvent)

        override fun onActionModeStarted(mode: ActionMode?) {
            default.onActionModeStarted(mode)
        }
    }

}