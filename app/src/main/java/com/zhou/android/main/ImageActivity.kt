package com.zhou.android.main

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.Fade
import com.squareup.picasso.Picasso
import com.zhou.android.R
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File

/**
 *  Created by mxz on 2019/11/3
 */
class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                exitTransition = Fade().apply {
                    duration = 500
                }
                allowEnterTransitionOverlap = false
                allowReturnTransitionOverlap = false
            }
        }

        setContentView(R.layout.activity_image)

        val file = intent.getStringExtra("image")
        Picasso.with(this)
                .load(Uri.fromFile(File(file)))
                .config(Bitmap.Config.RGB_565)
                .error(R.mipmap.ic_launcher)
                .into(imageView)
    }

}