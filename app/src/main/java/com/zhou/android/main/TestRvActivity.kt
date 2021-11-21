package com.zhou.android.main

import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_test_rv.*
import java.util.concurrent.TimeUnit

/**
 * Created by mxz on 2019/11/20.
 * 试用 BaseRecyclerViewAdapterHelper，能支持自动加载，但刷新需要再嵌套一层布局
 *
 * TwinklingRefreshLayout、SmartRefreshHeader
 * 外层添加了刷新头、加载尾，后者借鉴了前者，
 * 但在头部上支持更好，更多效果，如果动效复杂可以用后者，区别不大，性能未测试
 */
class TestRvActivity : BaseActivity() {

    private val data = ArrayList<String>()
    private val data2 = ArrayList<String>()
    private val composite = CompositeDisposable()

    override fun setContentView() {
        setContentView(R.layout.activity_test_rv)
    }

    override fun init() {

        for (i in 1..50) {
            data.add("item $i")
            data2.add("<< item $i")
        }

        val quickAdapter = DynamicAdapter(data).apply {

            //            setEnableLoadMore(false)
//            setOnLoadMoreListener({
//                composite.add(Observable.timer(3, TimeUnit.SECONDS)
//                        .doOnSubscribe {
//                            toast("开始加载更多")
//                        }
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe {
//                            val s = data.size
//                            for (i in 0.until(20)) {
//                                data.add("item ${s + i}")
//                            }
//                            this.loadMoreComplete()
//                        })
//            }, recyclerView)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TestRvActivity)
            adapter = quickAdapter
        }
        recyclerView2.apply {
            layoutManager = LinearLayoutManager(this@TestRvActivity)
            adapter = DynamicAdapter(data2)
        }

        refreshLayout.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout) {
                composite.add(Observable.timer(3, TimeUnit.SECONDS)
                    .doOnSubscribe {
                        toast("开始刷新")
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val tmp = ArrayList<String>(20)
                        for (i in 1.until(20)) {
                            tmp.add("new item $i")
                        }
                        data.addAll(0, tmp)
                        recyclerView.adapter.notifyDataSetChanged()
                        refreshLayout.finishRefreshing()
                        toast("刷新完成")
                    })
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout) {
                composite.add(Observable.timer(3, TimeUnit.SECONDS)
                    .doOnSubscribe {
                        toast("开始加载更多")
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val s = data.size
                        for (i in 0.until(20)) {
                            data.add("<< item ${s + i}")
                        }
                        recyclerView.adapter.notifyDataSetChanged()
                        refreshLayout.finishLoadmore()
                        toast("加载完成")
                    })
            }
        })

        smartRefreshLayout.setOnRefreshListener {
            composite.add(Observable.timer(3, TimeUnit.SECONDS)
                .doOnSubscribe {
                    toast("开始刷新")
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val tmp = ArrayList<String>(20)
                    for (i in 1.until(20)) {
                        tmp.add("<< item $i")
                    }
                    data2.addAll(0, tmp)
                    recyclerView2.adapter.notifyDataSetChanged()
                    toast("刷新完成")
                    smartRefreshLayout.finishRefresh()
                })
        }

        smartRefreshLayout.setOnLoadMoreListener {
            composite.add(Observable.timer(5, TimeUnit.SECONDS)
                .doOnSubscribe {
                    toast("开始加载更多")
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val s = data2.size
                    for (i in 0.until(20)) {
                        data2.add("item ${s + i}")
                    }
                    recyclerView2.adapter.notifyDataSetChanged()
                    toast("加载完成")
                    smartRefreshLayout.finishLoadMore()
                })
        }
    }

    override fun addListener() {
    }

    class DynamicAdapter(data: ArrayList<String>, id: Int = android.R.layout.simple_list_item_1) :
        BaseQuickAdapter<String, BaseViewHolder>(id, data) {
        override fun convert(helper: BaseViewHolder, item: String) {
            helper.getView<TextView>(android.R.id.text1).text = item
        }
    }

    override fun onStop() {
        super.onStop()
        composite.dispose()
    }

}