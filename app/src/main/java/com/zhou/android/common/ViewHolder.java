package com.zhou.android.common;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 通用适配器Holder
 * <p>
 * Created by ZhOu on 2017/5/23.
 */

@SuppressWarnings("unchecked")
public class ViewHolder {

    private SparseArray<View> views;
    private View contentView;

    public ViewHolder(Context context, int layoutId) {
        this.views = new SparseArray<>();
        contentView = LayoutInflater.from(context).inflate(layoutId, null);
        contentView.setTag(this);
    }

    public static ViewHolder getViewHolder(Context context, View convertView,int layoutId) {
        if (convertView == null)
            return new ViewHolder(context, layoutId);
        else
            return (ViewHolder) convertView.getTag();

    }

    public <T extends View> T getView(int id) {
        View view = views.get(id);
        if (view == null) {
            view = contentView.findViewById(id);
            views.put(id, view);
        }
        return (T) view;
    }

    public View getContentView() {
        return contentView;
    }

}
