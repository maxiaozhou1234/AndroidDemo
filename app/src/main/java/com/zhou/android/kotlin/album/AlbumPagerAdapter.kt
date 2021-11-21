package com.zhou.android.kotlin.album

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.zhou.android.R

class AlbumPagerAdapter : PagerAdapter {

    private val context: Context
    private val data = arrayListOf<String>()
    private val images = ArrayList<ImageView>()
    private var removePosition = -1
    private var needFreshCount = 0

    constructor(context: Context, data: ArrayList<String>) {
        this@AlbumPagerAdapter.context = context
        this@AlbumPagerAdapter.data.clear()
        this@AlbumPagerAdapter.data.addAll(data)
        for (index in data) {
            createItem(index)
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItemPosition(`object`: Any): Int {
        if (needFreshCount > 0) {
            needFreshCount--
            return POSITION_NONE
        } else {
            return super.getItemPosition(`object`)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = images[position]
        container?.addView(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(images[position])
//        images.removeAt(position)
//        (container as ViewPager).removeView(`object` as View)
    }

    private fun createItem(path: String): ImageView {
        val iv: ImageView = LayoutInflater.from(context).inflate(R.layout.layout_picture, null, false) as ImageView
        Picasso.with(context)
                .load(path)
                .error(R.drawable.ic_place)
                .placeholder(R.drawable.ic_place)
                .into(iv)
        images.add(iv)
        return iv
    }

    fun remove(position: Int) {
        if (position < 0 || position > data.size)
            return

        if (data.size > 0) {//无效
            val view = images[position + 1 % (count)]
            view.scaleX = 1f
            view.scaleY = 1f

        }
        removePosition = position
        data.removeAt(position)
        val view = images.removeAt(position)
        view.visibility = View.GONE

        needFreshCount = data.size
        notifyDataSetChanged()
    }

}