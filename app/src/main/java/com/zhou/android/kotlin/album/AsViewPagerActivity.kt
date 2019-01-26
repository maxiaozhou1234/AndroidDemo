package com.zhou.android.kotlin.album

import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.zhou.android.R
import com.zhou.android.common.BaseActivity

class AsViewPagerActivity : BaseActivity() {

    private val TAG = "album"

    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var pagerSnap: PagerSnapHelper

    private lateinit var viewPager: ViewPager
    private lateinit var viewAdapter: AlbumPagerAdapter

    val data = arrayListOf("https://i.loli.net/2018/12/23/5c1f38959779b.png",
            "https://i.loli.net/2019/01/06/5c318cbc7fa4c.jpg",
            "https://i.loli.net/2018/12/23/5c1f3895a7787.png")

    private var recyclerPosition = 0
    private var viewPagerPosition = 0

    override fun setContentView() {
        setContentView(R.layout.activity_as_vp)
    }

    override fun init() {
        recyclerView = findViewById(R.id.recyclerView)
        viewPager = findViewById(R.id.viewPager)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pagerSnap = PagerSnapHelper()
        pagerSnap.attachToRecyclerView(recyclerView)
        albumAdapter = AlbumAdapter(this, data)
        recyclerView.adapter = albumAdapter

        viewAdapter = AlbumPagerAdapter(this, data)
        viewPager.adapter = viewAdapter
        viewPager.setPageTransformer(false, ({ view, position ->
            if (position < -1) {//不可见
                view.scaleX = 0.8f
                view.scaleY = 0.8f
            } else if (position <= 0) {//[-1,0]
                val scale = 0.8f + (0.2f * (1 + position))
                view.scaleX = scale
                view.scaleY = scale
            } else if (position <= 1) {//[0,1]
                val scale = 0.8f + (0.2f * (1 - position))
                view.scaleX = scale
                view.scaleY = scale
            } else {//不可见
                view.scaleX = 0.8f
                view.scaleY = 0.8f
            }
        }))

        viewPager.pageMargin = -50
        viewPager.offscreenPageLimit = 3
    }

    override fun addListener() {
        findViewById<View>(R.id.next).setOnClickListener {
            recyclerPosition++
            if (recyclerPosition >= data.size)
                recyclerPosition = 0
            recyclerView.smoothScrollToPosition(recyclerPosition)

            if (data.size == 0)
                return@setOnClickListener
            viewPager.currentItem = (recyclerPosition++) % data.size
        }

        findViewById<View>(R.id.remove).setOnClickListener {
            data.removeAt(recyclerPosition)
            albumAdapter.notifyItemRemoved(recyclerPosition)

//            val tmp = viewPagerPosition
//            viewPagerPosition++
//            if (viewPagerPosition > +data.size) {
//                viewPagerPosition = 0
//            }
//            viewPager.currentItem = viewPagerPosition
//            viewAdapter.remove(tmp)
            viewAdapter.remove(viewPagerPosition)
        }

        recyclerView.addOnScrollListener(object : AlbumScrollListener(pagerSnap) {

            override fun onPageSelected(position: Int) {
                recyclerPosition = position
                Log.d(TAG, "current position = $position")
            }
        })

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewPagerPosition = position
            }
        })

    }
}