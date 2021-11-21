package com.zhou.android.kotlin.album

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.zhou.android.R

class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    val data: List<String>
    val context: Context

    constructor (context: Context, data: List<String>) {

        this@AlbumAdapter.context = context

        this@AlbumAdapter.data = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_picture, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.with(context)
                .load(data[position])
                .error(R.drawable.ic_place)
                .placeholder(R.drawable.ic_place)
                .into(holder?.image)
    }


    class ViewHolder : RecyclerView.ViewHolder {

        val image: ImageView

        constructor(view: View) : super(view) {
            image = view.findViewById(R.id.imageView)
        }


    }
}