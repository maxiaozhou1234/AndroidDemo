package com.zhou.android.kotlin

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import com.zhou.android.common.CommonRecyclerAdapter
import com.zhou.android.common.PaddingItemDecoration
import kotlinx.android.synthetic.main.activity_recycler_view.*
import java.util.*

/**
 * 长按拖拽 RV
 * Created by mxz on 2020/5/29.
 */
class AnimRecyclerActivity : BaseActivity() {

    private val data = arrayListOf<String>()
    private lateinit var simpleAdapter: CommonRecyclerAdapter<String, CommonRecyclerAdapter.ViewHolder>

    override fun setContentView() {
        setContentView(R.layout.activity_recycler_view)
    }

    override fun init() {

        for (i in 1..40) {
            data.add("example $i")
        }

        simpleAdapter = object : CommonRecyclerAdapter<String, CommonRecyclerAdapter.ViewHolder>(this, data) {
            override fun onBind(holder: ViewHolder, item: String, pos: Int) {
                holder.getView<TextView>(R.id.text).text = item
            }

            override fun getViewHolder(context: Context, parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(context).inflate(R.layout.layout_shape_text, parent, false)
                return ViewHolder(view)
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AnimRecyclerActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(PaddingItemDecoration(this@AnimRecyclerActivity, 20, 12, 20, 0))
            adapter = simpleAdapter
        }

        val itemHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                return makeMovementFlags(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
                        ItemTouchHelper.LEFT)
            }

            override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {

                simpleAdapter.notifyItemMoved(viewHolder?.adapterPosition
                        ?: 0, target?.adapterPosition ?: 0)
                Collections.swap(data, viewHolder?.adapterPosition ?: 0, target?.adapterPosition
                        ?: 0)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                val p = viewHolder?.adapterPosition ?: 0
                data.removeAt(p)
                simpleAdapter.notifyItemRemoved(p)
            }

            override fun canDropOver(recyclerView: RecyclerView?, current: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?) = true

            override fun isLongPressDragEnabled(): Boolean = true

            override fun isItemViewSwipeEnabled(): Boolean = true

            override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
                super.clearView(recyclerView, viewHolder)
                viewHolder?.itemView?.apply {
                    scaleX = 1.0f
                    scaleY = 1.0f
                }
            }

            override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.apply {
                        if (isCurrentlyActive) {
                            translationX = 60f
                            scaleX = 1.2f
                            scaleY = 1.2f
                        } else {
                            translationX = 0f
                            scaleX = 1.0f
                            scaleY = 1.0f
                        }
                    }
                } else
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        })
        itemHelper.attachToRecyclerView(recyclerView)

    }

    override fun addListener() {
    }

}