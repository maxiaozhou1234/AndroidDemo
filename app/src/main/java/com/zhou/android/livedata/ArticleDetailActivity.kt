package com.zhou.android.livedata

import android.content.Intent
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.Tools
import com.zhou.android.common.text
import kotlinx.android.synthetic.main.activity_article_detail.*

/**
 * Created by mxz on 2020/5/22.
 */
class ArticleDetailActivity : BaseActivity() {

    //    private lateinit var model: ArticleModel
    private var article: Article? = null
    private var pos = -1

    override fun setContentView() {
        setContentView(R.layout.activity_article_detail)
    }

    override fun addListener() {

        btnClean.setOnClickListener {
            etImage.setText("")
        }

        btnSave.setOnClickListener {

            Tools.hideSoftInput(this, it.windowToken)

            val title = etTitle.text.toString()
            val content = etContent.text.toString()
            val image = etImage.text.toString()
            if (title.isEmpty() && content.isEmpty() && image.isEmpty()) {
                article = null
            } else {
                article?.apply {
                    this.title = title
                    this.content = content
                    this.image = if (image.isEmpty()) null else image
                }
            }
            setResult(RESULT_OK, Intent().apply {
                putExtra("Article", article)
                putExtra("index", pos)
            })
            finish()
        }
    }

    override fun init() {

//        model = ViewModelProviders.of(this).get(ArticleModel::class.java)

        article = intent.getParcelableExtra<Article>("Article")
        pos = intent.getIntExtra("index", -1)
        if (article != null) {
            etTitle.text = article!!.title.text()
            etContent.text = article!!.content.text()
            etImage.text = (article!!.image ?: "").text()
        }
    }
}