package com.zhou.android.kotlin

import android.content.Intent
import android.util.Log
import android.view.View
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import java.lang.reflect.Field

/**
 *  @author Administrator_Zhou
 *  created on 2019/6/9
 */
class AnalysisDecorActivity : BaseActivity() {

    override fun setContentView() {
        setContentView(R.layout.activity_decor)
    }

    override fun init() {
    }

    override fun addListener() {
        findViewById<View>(R.id.jump).setOnClickListener {
            startActivity(Intent(this@AnalysisDecorActivity, AnalysisDecorActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val view = window.decorView
        try {
            val field: Field = view::class.java.getDeclaredField("mFeatureId")
            field.isAccessible = true
            val mFeatureId = field.getInt(view)
            Log.e("zhou", "onResume >> mFeatureId = $mFeatureId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}