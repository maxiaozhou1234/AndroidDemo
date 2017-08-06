package com.zhou.android.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zhou.android.R;
import com.zhou.android.ui.SquaredImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 适配器
 * Created by ZhOu on 2017/8/4.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Uri> path = new ArrayList<>();
    private Context context;

    public RecyclerAdapter(Context context, List<Uri> path) {
        this.context = context;
        if (path != null) {
            this.path.clear();
            this.path.addAll(path);
        }
    }

    public void notifyData(List<Uri> path) {
        if (path == null || path.size() == 0)
            return;
        this.path.addAll(path);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position != -1 && this.path.size() > position) {
            this.path.remove(position);
            notifyDataSetChanged();
        }
    }

    public Uri getItem(int position) {
        Uri uri = null;
        if (position != -1 && path.size() > position) {
            uri = path.get(position);
        }
        return uri;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.listformat_photo_item, null));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (path.size() == position) {
            holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_add));
            holder.iv.setOnClickListener(addListener);
        } else {
            holder.iv.setTag(-1, position);
            Uri p = path.get(position);
            Picasso.with(context).load(p).into(holder.iv);
            holder.iv.setOnClickListener(onClickListener);
            holder.iv.setOnLongClickListener(onLongClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return path.size() + 1;
    }

    public List<Uri> getData() {
        return path;
    }

    private RecyclerListener listener;

    public void setListener(RecyclerListener listener) {
        this.listener = listener;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(-1);
            if (listener != null) {
                listener.onItemClick(position);
            }
        }
    };

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            int position = (int) v.getTag(-1);
            if (listener != null) {
                listener.onItemLongClick(position);
                return true;
            } else
                return false;
        }
    };

    private View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onItemAdd();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
            iv = (SquaredImageView) view.findViewById(R.id.iv);
        }

        SquaredImageView iv;
    }
}
