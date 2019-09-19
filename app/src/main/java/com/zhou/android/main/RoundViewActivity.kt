package com.zhou.android.main

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import kotlinx.android.synthetic.main.activity_round_view.*

/**
 * Created by mxz on 2019/9/18.
 */
class RoundViewActivity : BaseActivity() {

    private val mode = arrayListOf(PorterDuff.Mode.CLEAR,
            PorterDuff.Mode.SRC, PorterDuff.Mode.DST, PorterDuff.Mode.SRC_OVER,
            PorterDuff.Mode.DST_OVER, PorterDuff.Mode.SRC_IN, PorterDuff.Mode.DST_IN,
            PorterDuff.Mode.SRC_OUT, PorterDuff.Mode.DST_OUT, PorterDuff.Mode.SRC_ATOP,
            PorterDuff.Mode.DST_ATOP, PorterDuff.Mode.XOR, PorterDuff.Mode.DARKEN,
            PorterDuff.Mode.LIGHTEN, PorterDuff.Mode.MULTIPLY, PorterDuff.Mode.SCREEN,
            PorterDuff.Mode.ADD, PorterDuff.Mode.OVERLAY)

    override fun setContentView() {
        setContentView(R.layout.activity_round_view)
    }

    override fun init() {

        round.bitmap = BitmapFactory.decodeResource(resources, R.drawable.pic_head)
        round2.bitmap = BitmapFactory.decodeResource(resources, R.drawable.pic_head)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RoundViewActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        val data = arrayListOf("CLEAR", "SRC", "DST", "SRC_OVER", "DST_OVER", "SRC_IN", "DST_IN", "SRC_OUT",
                "DST_OUT", "SRC_ATOP", "DST_ATOP", "XOR", "DARKEN", "LIGHTEN", "MULTIPLY", "SCREEN", "ADD", "OVERLAY")

        recyclerView.adapter = SimpleAdapter(this, data) {
            xfermodeView.xfermode = PorterDuffXfermode(mode[it])
        }
    }

    override fun addListener() {
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val percent = progress * 1f / seekBar.max
                round.radiusPercent = percent
                round2.radiusPercent = percent
            }
        })

        iv.setOnClickListener {
            iv.setImageBitmap(xfermodeView.output())
        }
    }

    class SimpleAdapter(val context: Context, val data: ArrayList<String>, val itemClick: ((position: Int) -> Unit)) : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH = VH(LayoutInflater.from(context).inflate(R.layout.layout_small_text, parent, false))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.text.apply {
                text = data[position]
                setOnClickListener {
                    itemClick.invoke(position)
                }
            }
        }

    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)
    }
}