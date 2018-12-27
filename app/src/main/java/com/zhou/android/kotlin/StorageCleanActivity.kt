package com.zhou.android.kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.zhou.android.R
import com.zhou.android.common.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StorageCleanActivity : BaseActivity() {

    private val TAG = "clean"

    private var dp8 = 0f
    private var count = 0
    private val _colors = arrayOf("#ffffff00", "#ff6aa84f", "#ff3d85c6", "#ffff9900", "#fff92ce9")

    lateinit var adapter: CommonAdapter<AppDetail>
    lateinit var layout: LinearLayout
    lateinit var listView: ListView
    var total = 1L
    val data = mutableListOf<AppDetail>()
    private val map = HashMap<String, View>()

    override fun setContentView() {
        setContentView(R.layout.activity_storage_clean)
    }

    override fun init() {
        val textStorage: TextView = findViewById(R.id.textStorage)
        val progress: ProgressBar = findViewById(R.id.progress)
        layout = findViewById(R.id.layout)
        listView = findViewById(R.id.listView)

        adapter = AppAdapter(this, data, R.layout.layout_app)
        listView.adapter = adapter

        dp8 = Tools.dip2pxf(this, 8)

        val map = StorageUtil.queryMyPhone(this)
        total = map[StorageUtil.key_total] ?: 1
        val used: Long = map[StorageUtil.key_used] ?: 0
        val available: Long = map[StorageUtil.key_available] ?: 0
        val system: Long = map[StorageUtil.key_system] ?: -1

        textStorage.text = "已用 ${StorageUtil.getUnit(used)} / 总 ${StorageUtil.getUnit(total)}"
        progress.progress = (used * 100 / total).toInt()

        createItem("可用 ${StorageUtil.getUnit(available)}", (available * 100 / total).toInt())
        val other = used - system
        createItem("其他 ${StorageUtil.getUnit(other)}", (other * 100 / total).toInt())
        if (system > 0) {
            createItem("系统 ${StorageUtil.getUnit(system)}", (system * 100 / total).toInt())
        }

        StorageUtil.queryMyApps(this, object : StorageUtil.Callback {
            override fun onSuccess(data: List<AppDetail>) {
                this@StorageCleanActivity.data.apply {
                    clear()
                    addAll(data)
                }
                var code = 0L
                var cache = 0L
                for (app in data) {
                    code += app.code
                    cache += app.cache
                }
//                addLine()
                createItem("应用 ${StorageUtil.getUnit(code)}", (code * 100 / total).toInt())
                createItem("缓存 ${StorageUtil.getUnit(cache)}", (cache * 100 / total).toInt())
                this@StorageCleanActivity.adapter.notifyDataSetChanged()
            }

            override fun onError(data: List<AppDetail>, error: Int, reason: String) {
                this@StorageCleanActivity.data.apply {
                    clear()
                    addAll(data)
                }
                this@StorageCleanActivity.adapter.notifyDataSetChanged()
                if (StorageUtil.WITHOUT_PERMISSION == error) {
                    StorageUtil.requestPermission(this@StorageCleanActivity)
                } else {
                    ToastUtils.show(this@StorageCleanActivity, reason)
                }
            }
        })

        Thread {
            val file = File(cacheDir.path + "/cache.txt")
            if (!file.exists()) {
                file.createNewFile()
                Log.d(TAG, "create file")
            }
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file, true)
                fos.write("增加测试数据abcd\n".toByteArray())
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                Tools.closeIO(fos)
            }

            val reader = file.bufferedReader()
            var line: String? = reader.readLine()
            while (line != null) {
                Log.i(TAG, line)
                line = reader.readLine()
            }
            Log.d(TAG, "end")
            Tools.closeIO(reader)
        }.start()
    }

    override fun addListener() {

        //1 与 java 类似写法
//        findViewById<View>(R.id.btnClean).setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View?) {
//
//            }
//        })

        //2 lambda 表达式，使用“{ }”代替
//        findViewById<View>(R.id.btnClean).setOnClickListener({ v ->
//            Log.d(TAG, "onClick")
//        })

        //3 lambda 如果参数做为函数且为最后一个，可以将花括号移出到括号后面
//        findViewById<View>(R.id.btnClean).setOnClickListener() { v ->
//            Log.d(TAG, "onClick")
//        }

        //4 lambda 移动到外面，括号没有参数，括号可以省略
//        findViewById<View>(R.id.btnClean).setOnClickListener { v ->
//            Log.d(TAG, "onClick")
//        }

        //5 lambda 最终版
//        findViewById<View>(R.id.btnClean).setOnClickListener {
//                        StorageUtil.cleanAppCache(this@StorageCleanActivity, arrayOf(this@StorageCleanActivity.packageName), object : StorageUtil.CleanCallback {
//                override fun callback(packageName: String, bool: Boolean) {
//                    if (bool) {
//                        this@StorageCleanActivity.data.filter {
//                            it.packageName == packageName
//                        }.apply {
//                            if (size == 1) {
//                                val app = this[0]
//                                app.cache = 0
//                                this@StorageCleanActivity.adapter.notifyDataSetChanged()
//                            }
//                        }
//                    } else {
//                        ToastUtils.show(this@StorageCleanActivity, "清除缓存失败")
//                    }
//                }
//            })
//            ToastUtils.show(this@StorageCleanActivity, "onClick")
//        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val app = data[position]
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", app.packageName, null)
            })
        }
    }

    private fun createItem(text: String, percent: Int) {

        var view = map[text]

        if (view != null) {
            view.findViewById<TextView>(R.id.text).text = text
            view.findViewById<ProgressBar>(R.id.progress).progress = if (percent == 0) percent + 1 else percent
            return
        }

        view = LayoutInflater.from(this).inflate(R.layout.layout_storage_item, null)
        val drawable = GradientDrawable()

        drawable.cornerRadius = dp8
        drawable.setColor(Color.parseColor(_colors[count++ % _colors.size]))

        view.findViewById<View>(R.id.dot).setBackgroundDrawable(drawable)
        view.findViewById<TextView>(R.id.text).text = text
        view.findViewById<ProgressBar>(R.id.progress).progress = if (percent == 0) percent + 1 else percent
        layout.addView(view)
        map.put(text, view)
    }

    private fun addLine() {
        val view = View(this)
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dip2px(this, 3))
        view.setBackgroundColor(Color.parseColor("#FF303F9F"))
        layout.addView(view)
    }

    class AppAdapter(context: Context, val data: List<AppDetail>, layout: Int) : CommonAdapter<AppDetail>(context, data, layout) {

        override fun fillData(holder: ViewHolder?, position: Int) {
            val app = data[position]
            holder?.apply {
                getView<ImageView>(R.id.icon).setImageDrawable(app.icon)
                getView<TextView>(R.id.packageName).text = app.packageName
                getView<TextView>(R.id.size).text = app.getAppString()
            }
        }

    }
}