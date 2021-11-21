package com.zhou.android.main

import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.editable
import kotlinx.android.synthetic.main.activity_slide_menu.*

/**
 * 滑动显示更多菜单练习
 * 项目使用，建议 'com.github.mcxtzhang:SwipeDelMenuLayout:V1.3.0'
 * Created by mxz on 2020/10/15.
 */
class SlideMenuActivity : BaseActivity() {

    override fun setContentView() {
        setContentView(R.layout.activity_slide_menu)
    }

    override fun addListener() {

        //----------------------------------------------
        tv3.setOnClickListener {
            tvLog.text = "cover 左滑模式".editable()
        }
        btn6.setOnClickListener {
            tvLog.text = "置顶 cover".editable()
        }
        btn7.setOnClickListener {
            tvLog.text = "删除 cover".editable()
        }

        //----------------------------------------------
        tv4.setOnClickListener {
            tvLog.text = "cover 右滑模式".editable()
        }
        btn8.setOnClickListener {
            tvLog.text = "取消置顶".editable()
        }
        btn9.setOnClickListener {
            tvLog.text = "清空".editable()
        }
        //----------------------------------------------
        tv5.setOnClickListener {
            tvLog.text = "expand 左滑模式".editable()
        }
        btn10.setOnClickListener {
            tvLog.text = "expand 打开".editable()
        }
        btn11.setOnClickListener {
            tvLog.text = "expand 关闭".editable()
        }
        //----------------------------------------------
        tv6.setOnClickListener {
            tvLog.text = "expand 右滑模式".editable()
        }
        btn12.setOnClickListener {
            tvLog.text = "右滑打开".editable()
        }
        btn13.setOnClickListener {
            tvLog.text = "右滑关闭".editable()
        }
    }

    override fun init() {

//        ViewDragHelper
    }
}