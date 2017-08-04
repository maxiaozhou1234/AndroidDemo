package com.zhou.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 适配器
 * Created by ZhOu on 2017/8/4.
 */

public class RecyclerAdapter extends RecyclerView.Adapter {

    private List<String> path = new ArrayList<>();

    public RecyclerAdapter(List<String> path) {
        this.path.clear();
        this.path.addAll(path);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private RecyclerListener listener;

    public void setListener(RecyclerListener listener) {
        this.listener = listener;
    }
}
