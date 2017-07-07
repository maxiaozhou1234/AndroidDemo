package com.zhou.android.kotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.zhou.android.MainActivity
import com.zhou.android.R

class SimpleListKotlinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotin)
        actionBar.title = "Kotlin"
        actionBar.setDisplayHomeAsUpEnabled(true)
        var button = findViewById(R.id.button) as Button
        button.setOnClickListener { view ->
            startActivity(Intent(this@SimpleListKotlinActivity, MainActivity::class.java))
        }
        var listView = findViewById(R.id.listView) as ListView
        var data: ArrayList<String> = ArrayList()
        for (i in 0..20) {
            data.add("测试示例：" + i)
        }
        var adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, data)
        listView.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (android.R.id.home == item?.itemId) {
            finish()
            return true
        } else
            return super.onOptionsItemSelected(item)
    }

}
