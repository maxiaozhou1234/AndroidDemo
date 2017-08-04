package com.zhou.android.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.zhou.android.R;
import com.zhou.android.adapter.RecyclerAdapter;
import com.zhou.android.adapter.RecyclerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择器
 * Created by ZhOu on 2017/8/4.
 */

public class PhotoPicker extends LinearLayout {

    private Context context;
    private List<String> path = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private int spanCount = 4;

    public PhotoPicker(Context context) {
        this(context, null);
    }

    public PhotoPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.listformat_photo_picker, this);
        this.context = context;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        recyclerAdapter.setListener(new RecyclerListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onItemLongClick(int position) {

            }

            @Override
            public void onItemAdd() {

            }
        });
    }
}
