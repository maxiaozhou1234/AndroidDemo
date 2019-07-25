package com.zhou.android.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Process
import android.util.Log

/**
 * Created by mxz on 2019/7/25.
 */
class ExceptionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action.equals("playSound")) {
            Log.d("zhou", "playSound ${Process.myPid()} ${ExceptionReceiver@ this.toString()}")
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, uri).play()
        }
    }
}