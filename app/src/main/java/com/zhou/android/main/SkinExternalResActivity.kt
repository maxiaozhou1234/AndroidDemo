package com.zhou.android.main

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.support.annotation.ColorInt
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.zhou.android.R
import kotlinx.android.synthetic.main.activity_skin_external.*
import java.io.File
import java.lang.reflect.Method


/**
 * 换肤测试，加载外部资源
 * Created by mxz on 2021/9/8.
 */
class SkinExternalResActivity : AppCompatActivity() {

    private val resourceManager = object {

        private lateinit var localResource: Resources
        private var packageName: String = ""

        fun init(context: Context) = reset(context)

        fun load(context: Context): Boolean {

            var result = false

            if (packageName !== context.packageName) {
                Log.i("zhou", "already loaded")
                result = true
                return result
            }

            val skinPath = Environment.getExternalStorageDirectory().absolutePath + "/skin_res.apk"
            val file = File(skinPath)
            if (!file.exists()) {
                //手动复制文件到本地，里面只放了和当前布局使用的两个 drawable 和 一个 color
                // how？
                // 创建一个新工程项目，往 res 添加所需资源文件，删除其他依赖文件夹，kt 依赖也删除
                // 底部终端执行 gradlew assembleRelease 或者构建签名 apk 即可
                // 预防被安装的话，可以改文件后缀为其他
                Log.e("zhou", "file not found: $skinPath 将 assets 中文件复制到 sdcard")
                return result
            }

            try {
                val packageInfo = context.packageManager.getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES)
                packageName = packageInfo.packageName //解析并存储包名，加载资源需要用到

                //构建 AssetManager，反射指定资源路径
                val am = AssetManager::class.java.newInstance()
                val addAssetPath: Method = am.javaClass.getMethod("addAssetPath", String::class.java)
                addAssetPath.invoke(am, skinPath)

                val superResources = context.resources
                //创建新的 resources
                val resources = Resources(am, superResources.displayMetrics, superResources.configuration)

                localResource = resources
                Log.d("zhou", "load success")
                result = true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("zhou", "load fail")
            }
            return result
        }

        fun reset(context: Context) {
            localResource = context.resources
            packageName = context.packageName
        }

        @ColorInt
        fun getColor(colorName: String): Int {
            val id = localResource.getIdentifier(colorName, "color", packageName)
            return localResource.getColor(id)
        }

        fun getDrawable(drawableName: String): Drawable {

            val id = localResource.getIdentifier(drawableName, "drawable", packageName)
            return localResource.getDrawable(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_external)

        supportActionBar!!.title = javaClass.simpleName.replace("Activity", "")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        resourceManager.init(this)

        btnLoad.setOnClickListener {
            val r = resourceManager.load(this@SkinExternalResActivity)
            Toast.makeText(this, "load $r", Toast.LENGTH_SHORT).show()
        }

        btnRefresh.setOnClickListener {
            refresh()
        }

        btnReset.setOnClickListener {
            resourceManager.reset(this@SkinExternalResActivity)
            refresh()
        }
    }

    private fun refresh() {//更新 ui
        colorView.setBackgroundColor(resourceManager.getColor("colorPrimaryDark"))
        drawableView.setBackgroundDrawable(resourceManager.getDrawable("shape_skin_uni"))
        ivView.setImageDrawable(resourceManager.getDrawable("ic_3"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (android.R.id.home == item.itemId) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }
}