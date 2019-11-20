package com.zhou.android.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.toast
import kotlinx.android.synthetic.main.activity_verify_code.*

/**
 * Created by mxz on 2019/11/14.
 */
class VerifyCodeActivity : BaseActivity() {

    override fun setContentView() {
        setContentView(R.layout.activity_verify_code)
    }

    override fun init() {

    }

    override fun addListener() {
        btnShared.setOnClickListener {
            pop.showAtLocation(window.decorView, Gravity.BOTTOM, 0, 0)
        }
    }

    private val pop: PopupWindow by lazy {
        PopupWindow(this).apply {
            val layout = LayoutInflater.from(this@VerifyCodeActivity)
                    .inflate(R.layout.layout_shared, null)

            layout.findViewById<Button>(R.id.btnFriend).setOnClickListener {

                toast("朋友圈")
                //https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/Android.html
                //文字
//                val req = SendMessageToWX.Req().apply {
//                    transaction = "acb"
//                    message = WXMediaMessage().apply {
//                        mediaObject = WXTextObject().apply {
//                            text = "分享微信文字测试"
//                        }
//                        description = "测试"
//                    }
//                    scene = SendMessageToWX.Req.WXSceneTimeline
//                }
//                WXAPIFactory.createWXAPI(this@VerifyCodeActivity, "123456").sendReq(req)
            }
            layout.findViewById<Button>(R.id.btnWx).setOnClickListener {

                toast("微信")
//                val req = SendMessageToWX.Req().apply {
//                    transaction = "acb"
//                    message = WXMediaMessage().apply {
//                        mediaObject = WXTextObject().apply {
//                            text = "分享微信文字测试"
//                        }
//                        description = "测试"
//                    }
//                    scene = SendMessageToWX.Req.WXSceneSession
//                }
//
//                WXAPIFactory.createWXAPI(this@VerifyCodeActivity, "123456").sendReq(req)

            }
            layout.findViewById<Button>(R.id.btnLink).setOnClickListener {
                val clipboard = this@VerifyCodeActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData("link", arrayOf("text/plain"), ClipData.Item("https:www.baidu.com"))
                toast("复制成功")
            }

            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            contentView = layout

            animationStyle = R.style.sharedPopStyle
            isFocusable = true
            isOutsideTouchable = true
        }
    }
}