package com.zhou.android.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.zhou.android.MainActivity
import com.zhou.android.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by zhou on 2021/7/20.
 */
class KidWidgetProvider : AppWidgetProvider() {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->

            val pendingIntent = Intent(context, MainActivity::class.java).let {
                PendingIntent.getActivity(context, 0, it, 0)
            }
            val view = RemoteViews(context.packageName, R.layout.layout_kid_widget_provider).apply {
                setOnClickPendingIntent(R.id.layout, pendingIntent)
                setTextViewText(R.id.tvContent, dateFormat.format(Date()))
            }

            appWidgetManager.updateAppWidget(id, view)
        }
    }
}