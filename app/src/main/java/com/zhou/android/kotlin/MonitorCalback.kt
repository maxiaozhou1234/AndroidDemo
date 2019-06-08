package com.zhou.android.kotlin

import android.annotation.TargetApi
import android.os.Build
import android.view.*
import android.view.accessibility.AccessibilityEvent

/**
 *  @author Administrator_Zhou
 *  created on 2019/6/8
 */
class MonitorCalback(val default: Window.Callback) : Window.Callback {
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