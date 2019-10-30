package com.zhou.android.main

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.ToastUtils
import com.zhou.android.common.Tools
import kotlinx.android.synthetic.main.activity_timeline.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

/**
 * Created by mxz on 2019/10/9.
 */
class TimeLineActivity : BaseActivity() {

    private val host = "http://192.168.6.124:8080/"

    private val picData = arrayListOf<PictureInfo>()

    override fun setContentView() {
        setContentView(R.layout.activity_timeline)
    }

    override fun init() {

        val list = arrayListOf(
                "http://i2.sinaimg.cn/fashion/cr/2015/0206/2010174774.jpg",
                "http://b-ssl.duitang.com/uploads/item/201808/12/20180812163053_kjxhe.thumb.700_0.png",
                "http://img.yzcdn.cn/upload_files/2014/12/31/95fbce8cb9623e841875289c90e3ddac.jpg",
                "http://hbimg.b0.upaiyun.com/164e0ddc1277149c8113e0115db417bb90ec51d347514-B6W8w0_fw658",
                "http://a4.att.hudong.com/25/11/300026827739133147118221511_950.jpg",
                "http://img.zcool.cn/community/017e8e5909df33a801214550e6d71c.jpg",
                "http://hbimg.b0.upaiyun.com/aaf79e379d139a09f8db1406fcd57c1c0359c2722ab9e-NzJLjB_fw658")

        for (url in list) {
            picData.add(PictureInfo(url, 0, 0))
        }

        Log.i("zhou", "width >> ${Tools.getScreenWidth(this) / 2}")

        val retrofit = Retrofit.Builder()
                .baseUrl(host)
                .build()

        retrofit.create(PictureApi::class.java).getPicture().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("zhou", "${t.message}")
                ToastUtils.show(this@TimeLineActivity, "没有服务器，使用网络数据")
                recyclerView.adapter.notifyItemRangeChanged(0, picData.size)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val data = response.body().string()
                    Log.d("zhou", "data >> $data")
                    try {
//                        [{"id":1,"name":"001.jpg","url":"file/001.jpg","width":550.0,"height":686.0}]
                        val array = JSONArray(data)
                        if (array.length() > 0) {
                            picData.clear()
                            for (i in 0 until array.length()) {
                                val obj = array.optJSONObject(i)
                                picData.add(PictureInfo("$host${obj.optString("url")}", obj.optInt("width"), obj.optInt("height")))
                            }
                            recyclerView.adapter.notifyDataSetChanged()
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })

        val dotItemDecoration = DotItemDecoration.Builder(this)
                .setOrientation(DotItemDecoration.VERTICAL)
                .setItemStyle(DotItemDecoration.STYLE_RESOURCE)
                .setDotRes(R.drawable.ic_smile)
//                .setItemStyle(DotItemDecoration.STYLE_DRAW)
                .setTopDistance(20f)
                .setItemPaddingLeft(10f)
                .setItemPaddingRight(10f)
                .setDotColor(Color.GREEN)
                .setDotRadius(8)
                .setDotPaddingTop(0)
                .setDotInItemOrientationCenter(true)
                .setLineColor(Color.RED)
                .setLineWidth(1f)
                .setEndText("end")
                .setTextColor(Color.BLACK)
                .setTextSize(10f)
                .setDotPaddingTop(2)
                .setBottomDistance(40f)
                .create()

        recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
//                addItemDecoration(DividerItemDecoration(this@TimeLineActivity, DividerItemDecoration.HORIZONTAL))
                addItemDecoration(dotItemDecoration)
            }
            adapter = SimpleAdapter(this@TimeLineActivity, picData)
        }

    }

    override fun addListener() {
    }

    class SimpleAdapter(val context: Context, val list: ArrayList<PictureInfo>) : RecyclerView.Adapter<ViewHolder>() {

        val width = Tools.getScreenWidth(context) / 2

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.listformat_image_item, parent, false))
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val info = list[position]
            if (info.width != 0) {
//                val h = (width * 1.0 / info.width * info.height).toInt()
                val h = (width * 1.0 * info.height / info.width).toInt()
                Log.d("zhou", "${info.url}, > w:${info.width} ,h:${info.height} >>> $h")
                val lp = holder.image.layoutParams
                lp.apply {
                    width = this@SimpleAdapter.width
                    height = h
                }
                holder.image.layoutParams = lp

                Glide.with(context)
                        .load(info.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.image)
            } else {
                Glide.with(context)
                        .load(info.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.image)
            }

        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.iv)
    }

    interface PictureApi {

        @GET("/getPicture")
        fun getPicture(): Call<ResponseBody>
    }

    data class PictureInfo(var url: String, var width: Int, var height: Int)
}