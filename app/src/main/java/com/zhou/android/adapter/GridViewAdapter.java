package com.zhou.android.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.item.GridViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhOu on 2017/2/8.
 */

public class GridViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<GridViewItem> list = new ArrayList<>();
    private int[] color = {0xffd0a933, 0xffd45f5f, 0xff669cf7, 0xff7e44a5,
            0xff6c9f56, 0xff6A5ACD, 0xff00EE76, 0xffB8B8B8};

    public GridViewAdapter(Context context, List<GridViewItem> list) {
        inflater = LayoutInflater.from(context);
        if (list != null)
            this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listformat_gridviewitem, null);
            holder = new ViewHolder();
            holder.ll_bg = (LinearLayout) convertView.findViewById(R.id.ll_bg);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_zh = (TextView) convertView.findViewById(R.id.tv_zh);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        GridViewItem item = list.get(position);
        String simpleName = "";
        try {
            simpleName = Class.forName(item.targetClass).getSimpleName();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        holder.ll_bg.setBackgroundColor(color[position % color.length]);
        holder.tv_name.setText(TextUtils.isEmpty(simpleName) ? item.targetClass.substring(0, 1).toUpperCase() : simpleName.substring(0, 1).toUpperCase());
        holder.tv_zh.setText(TextUtils.isEmpty(item.zhName) ? (TextUtils.isEmpty(simpleName) ? item.targetClass : simpleName) : item.zhName);

        return convertView;
    }

    class ViewHolder {
        LinearLayout ll_bg;
        TextView tv_name, tv_zh;
    }
}
