package com.zhou.android.kotlin

import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import kotlinx.android.synthetic.main.activity_order_fragemnt.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 测试 ViewPager + Fragment 随机切换顺序
 * Created by zhou on 2020/8/2.
 */
class OrderFragmentActivity : BaseActivity() {

    private val cache = SparseArray<TextFragment>()

    private val data = ArrayList<TextFragment>()
    private lateinit var adapter: Adapter

    private val random = Random()

    override fun setContentView() {
        setContentView(R.layout.activity_order_fragemnt)
    }

    override fun init() {

        cache.put(0, TextFragment().apply {
            setText("0")
            bg(Color.GREEN)
        })
        cache.put(1, TextFragment().apply {
            setText("1")
            bg(Color.RED)
        })
        cache.put(2, TextFragment().apply {
            setText("2")
            bg(Color.BLUE)
        })
        cache.put(3, TextFragment().apply {
            setText("3")
            bg(Color.YELLOW)
        })

        for (i in 0.until(4)) {
            val f = cache[i]
            f.mark = i
            data.add(f)
        }

        adapter = Adapter(supportFragmentManager, data)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 4
    }

    override fun addListener() {
        btnChange.setOnClickListener {
            val array = IntArray(4) { it }
            val d = ArrayList<Int>()

            repeat(3) {
                var i = random.nextInt(4)
                while (-1 == array[i]) {
                    i++
                    if (i >= 4) {
                        i = 0
                    }
                }
                d.add(array[i])
                array[i] = -1
            }
            for (i in array) {
                if (i != -1) {
                    d.add(i)
                }
            }
            data.clear()
            for (i in d) {
                data.add(cache[i])
            }

            tvArray.text = d.joinToString()

            adapter.notifyDataSetChanged()
            viewPager.currentItem = 0
        }
    }

    class Adapter(fragmentManager: FragmentManager, private val data: ArrayList<TextFragment>)
        : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return data[position]
        }

        override fun getCount() = data.size

        override fun getItemId(position: Int): Long {
            return data[position].hashCode().toLong()
        }

        override fun getItemPosition(obj: Any): Int {

            if (count == 0)
                return POSITION_UNCHANGED

            val hash = obj.hashCode()
            val i = (obj as TextFragment).mark
            if (hash == data[i].hashCode()) {
                return POSITION_UNCHANGED
            } else {
                for ((i, f) in data.withIndex()) {
                    if (obj == f) {
                        f.mark = i
                        break
                    }
                }

                return POSITION_NONE
            }
        }
    }

}