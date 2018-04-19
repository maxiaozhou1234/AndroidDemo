package com.zhou.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zhou.android.R;
import com.zhou.android.common.Tools;

import java.util.List;

/**
 * 网格图片适配器
 * Created by ZhOu on 2018/4/14.
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<String> data;
    private int width = 0;

    public PictureAdapter(Context context, List<String> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        width = Tools.getScreenWidth(context) / 4;
    }

    @Override
    public PictureAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.listformat_photo_item, parent, false));
    }

    @Override
    public void onBindViewHolder(PictureAdapter.ViewHolder holder, int position) {
        final String uri = data.get(position);
        Picasso.with(context)
                .load(uri)
                .config(Bitmap.Config.RGB_565)
                .resize(width, width)
                .centerCrop()
                .into(holder.image);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(uri);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public interface OnItemClickListener {
        void onItemClick(String uri);
    }

    private OnItemClickListener listener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.iv);
        }

        ImageView image;
    }
}
