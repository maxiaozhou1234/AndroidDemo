package com.zhou.android.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.MenuItem
import com.zhou.android.R
import com.zhou.android.skin.SkinFactory
import com.zhou.android.skin.SkinManager
import kotlinx.android.synthetic.main.activity_skin_page.*

/**
 * Created by mxz on 2021/3/18.
 */
class SkinPageActivity : AppCompatActivity() {

    private val skinFactory = SkinFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory(layoutInflater, skinFactory)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_page)

        supportActionBar?.title = javaClass.simpleName.replace("Activity", "")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSwitch.setOnClickListener {
            SkinManager.switchMode(!SkinManager.isDayMode())

            val tt = if (SkinManager.isDayMode()) "白天" else "夜晚"
            tvMode.text = Editable.Factory.getInstance().newEditable(tt)
        }
        btnList.setOnClickListener {
            startActivity(Intent(this, SkinListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val t = if (SkinManager.isDayMode()) "白天" else "夜晚"
        tvMode.text = Editable.Factory.getInstance().newEditable(t)
    }

    override fun onDestroy() {
        skinFactory.destroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}