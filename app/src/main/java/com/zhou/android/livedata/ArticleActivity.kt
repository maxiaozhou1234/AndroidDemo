package com.zhou.android.livedata

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.zhou.android.R
import com.zhou.android.R.layout.listformat_article_item
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.CommonRecyclerAdapter
import com.zhou.android.common.PaddingItemDecoration
import com.zhou.android.common.toast
import kotlinx.android.synthetic.main.activity_article.*

/**
 * Created by mxz on 2020/5/22.
 */
class ArticleActivity : BaseActivity() {

    private val tag = "article"
    private val ArticleCode = 100
    private lateinit var model: ArticleModel

    private val data = ArrayList<Article>()

    override fun setContentView() {
        setContentView(R.layout.activity_article)
    }

    override fun addListener() {

    }

    override fun init() {

        model = ViewModelProviders.of(this).get(ArticleModel::class.java)

        model.liveData.observe(this, Observer {
            Log.i(tag, "on change >> ${it?.size}")
            if (it != null) {
                data.clear()
                data.addAll(it)
                articleRecycler.adapter.notifyDataSetChanged()
            }
        })

        val articleAdapter = object : CommonRecyclerAdapter<Article, CommonRecyclerAdapter.ViewHolder>(this, data) {
            override fun onBind(holder: ViewHolder, item: Article, pos: Int) {
                val image = holder.getView<ImageView>(R.id.imageCover)
                if (item.image.isNullOrEmpty()) {
                    image.visibility = View.GONE
                } else {
                    image.visibility = View.VISIBLE
                    Glide.with(this@ArticleActivity)
                            .load(item.image)
                            .centerCrop()
                            .into(image)
                }
                holder.getView<TextView>(R.id.tvTitle).text = item.title
                holder.getView<TextView>(R.id.tvContent).text = item.content

                holder.getView<View>(R.id.layout).setOnClickListener {
                    startActivityForResult(Intent(this@ArticleActivity, ArticleDetailActivity::class.java).apply {
                        putExtra("Article", item)
                        putExtra("index", pos)
                    }, ArticleCode)
                }
            }

            override fun getViewHolder(context: Context, parent: ViewGroup, viewType: Int): ViewHolder {
                val view = layoutInflater.inflate(listformat_article_item, parent, false)
                return ViewHolder(view)
            }

        }
        articleRecycler.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(this@ArticleActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(PaddingItemDecoration(this@ArticleActivity, 8, 8, 8, 0))
        }

        LiveEventBus.getChannel("test", String::class.java).observe(this, Observer { value ->
            toast(value!!)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bluetooth, menu)
        menu?.findItem(R.id.menu_status)?.title = "测试Bus"
        return true
//        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_status) {
            startActivity(Intent(this, LDBusActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ArticleCode == requestCode && Activity.RESULT_OK == resultCode) {
            val article = data?.getParcelableExtra<Article>("Article")
            val pos = data?.getIntExtra("index", -1) ?: -1
            if (pos != -1) {
                model.updateArticle(article, pos)
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}