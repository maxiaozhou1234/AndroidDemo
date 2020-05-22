package com.zhou.android.common

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

/**
 * Created by mxz on 2020/5/22.
 */
abstract class CommonRecyclerAdapter<T, R : CommonRecyclerAdapter.ViewHolder>(val context: Context, val data: List<T>)
    : RecyclerView.Adapter<R>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): R {
        return getViewHolder(context, parent, viewType)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: R, position: Int) {
        onBind(holder, data[position], position)
    }

    abstract fun onBind(holder: R, item: T, pos: Int)

    abstract fun getViewHolder(context: Context, parent: ViewGroup, viewType: Int): R

    open class ViewHolder(private val layout: View) : RecyclerView.ViewHolder(layout) {

        private val cache = SparseArray<View>()

        @Suppress("UNCHECKED_CAST")
        fun <T : View> getView(id: Int): T {
            var view = cache[id]
            if (view == null) {
                view = layout.findViewById(id)
                cache.put(id, view)
            }
            return view as T
        }
    }
}
