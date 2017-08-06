package com.zhou.android.adapter;

/**
 * 点击监听器
 * Created by ZhOu on 2017/8/4.
 */

public interface RecyclerListener {
    void onItemClick(int position);

    void onItemLongClick(int position);

    void onItemAdd();
}
