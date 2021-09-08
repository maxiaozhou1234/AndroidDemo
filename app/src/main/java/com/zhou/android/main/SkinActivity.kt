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
import kotlinx.android.synthetic.main.activity_skin.*

/**
 * 换肤 白天/黑暗 模式
 * <a href="https://www.jianshu.com/p/af7c0585dd5b">Android主题换肤 无缝切换</>
 * <a href="https://github.com/burgessjp/ThemeSkinning">仓库 ThemeSkinning </a>
 * Created by mxz on 2021/3/17.
 */
class SkinActivity : AppCompatActivity() {

    private val factory = SkinFactory()

    override fun onCreate(savedInstanceState: Bundle?) {

        LayoutInflaterCompat.setFactory(layoutInflater, factory)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin)

        supportActionBar?.title = javaClass.simpleName.replace("Activity", "")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSwitch.setOnClickListener {

            SkinManager.switchMode(!SkinManager.isDayMode())

            val text = "tv 颜色显示更换 [${if (SkinManager.isDayMode()) "白天" else "夜晚"}]"
            tvMode.text = Editable.Factory.getInstance().newEditable(text)
        }

        btnOpen.setOnClickListener {
            startActivity(Intent(this, SkinPageActivity::class.java))
        }

        btnExternal.setOnClickListener {
            startActivity(Intent(this, SkinExternalResActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val text = "tv 颜色显示更换 [${if (SkinManager.isDayMode()) "白天" else "夜晚"}]"
        tvMode.text = Editable.Factory.getInstance().newEditable(text)
    }

    override fun onDestroy() {
        factory.destroy()
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