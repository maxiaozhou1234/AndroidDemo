package com.zhou.android.main

import android.os.Bundle
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zhou.android.R
import com.zhou.android.skin.SkinFactory
import com.zhou.android.skin.SkinManager
import kotlinx.android.synthetic.main.activity_skin_list.*

/**
 * Created by mxz on 2021/3/19.
 */
class SkinListActivity : AppCompatActivity() {
    private val skinFactory = SkinFactory()

    private val data = ArrayList<String>()
    private val simpleAdapter = object : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.layout_skin_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.get<TextView>(R.id.textView).text = data[position]
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory(layoutInflater, skinFactory)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_list)

        supportActionBar?.title = javaClass.simpleName.replace("Activity", "")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val decoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        decoration.setDrawable(resources.getDrawable(R.drawable.shape_divider_v))

        repeat(20) {
            data.add("item $it")
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SkinListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = simpleAdapter
            addItemDecoration(decoration)
        }
    }

    override fun onDestroy() {
        skinFactory.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_skin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        } else if (item?.itemId == R.id.menu_switch) {
            SkinManager.switchMode()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cache = SparseArray<View>()

        @Suppress("UNCHECKED_CAST")
        fun <T : View> get(id: Int): T {
            val view = cache[id] ?: kotlin.run {
                val v = itemView.findViewById(id) as T
                cache.put(id, v)
                v
            }
            return view as T
        }
    }
}