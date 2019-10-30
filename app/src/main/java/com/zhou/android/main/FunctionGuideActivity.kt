package com.zhou.android.main

import android.graphics.Color
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import kotlinx.android.synthetic.main.activity_function_guide.*
import tourguide.tourguide.Overlay
import tourguide.tourguide.TourGuide

/**
 * 功能高亮引导
 *
 * 示例使用的是 TourGuide，虽然封装不错，但是很明显不好用<Br/>
 * </p>
 *
 * 推荐使用 <a href='https://github.com/deano2390/MaterialShowcaseView'>MaterialShowcaseView</a>
 * 支持多个高亮点显示
 * </p>
 *
 * Created by mxz on 2019/9/23.
 */
class FunctionGuideActivity : BaseActivity() {

    lateinit var firstGuide: TourGuide
    lateinit var secondGuide: TourGuide

    override fun setContentView() {
        setContentView(R.layout.activity_function_guide)
    }

    override fun init() {
        firstGuide = TourGuide.create(this) {
            pointer {}

            toolTip {
                title { "界面展示区域" }
                description { "你的产品会在这里显示" }
                setOnClickListener(View.OnClickListener {
                    cleanUp()
                    secondGuide.playOn(btnLike)
                })
            }
            overlay {
                style { Overlay.Style.ROUNDED_RECTANGLE }
                backgroundColor { Color.parseColor("#ffc3c3c3") }
            }
        }.with(TourGuide.Technique.HORIZONTAL_LEFT)
                .motionType(TourGuide.MotionType.ALLOW_ALL)
                .playOn(tvShow)

        secondGuide = TourGuide.create(this) {
            pointer {}

            toolTip {
                title { "给你喜欢的产品点赞" }
                setOnClickListener(View.OnClickListener { cleanUp() })
                gravity {
                    Gravity.LEFT
                }
            }
            overlay {
                backgroundColor { Color.parseColor("#ffc3c3c3") }
            }
        }.with(TourGuide.Technique.CLICK)
                .motionType(TourGuide.MotionType.ALLOW_ALL)
    }

    override fun addListener() {

        btnSwitch.setOnClickListener {
            etPwd.visibility = if (etPwd.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_photo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_ok) {
            firstGuide.playOn(tvShow)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}