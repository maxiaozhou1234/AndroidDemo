package com.zhou.android.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用适配器
 * </p>
 * Created by ZhOu on 2017/5/23.
 */

public abstract class CommonAdapter<T> extends BaseAdapter {

    private List<T> data = new ArrayList<>();
    private Context context;
    private int layoutId;

    public CommonAdapter(Context context, List<T> data, int layoutId) {
        this.context = context;
        this.data = data;
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.getViewHolder(context, convertView, layoutId);
        fillData(holder, position);
        return holder.getContentView();
    }

    protected abstract void fillData(ViewHolder holder, int position);
}
