package com.zhou.android.main;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.adapter.SimpleRecyclerAdapter;
import com.zhou.android.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 Behavior 来实现堆叠效果
 */
public class NestedBehaviorActivity extends BaseActivity {

    private TextView text;
    private RecyclerView recyclerView;

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_behavior);
    }

    @Override
    protected void init() {
//        text = findViewById(R.id.text);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            data.add("behavior data " + i);
        }
        SimpleRecyclerAdapter simpleRecyclerAdapter = new SimpleRecyclerAdapter(this, data);
        recyclerView.setAdapter(simpleRecyclerAdapter);

//        bottomSheetBehavior = BottomSheetBehavior.from(text);
//        bottomSheetBehavior.setHideable(true);
    }

    @Override
    protected void addListener() {
//        findViewById(R.id.button).setOnClickListener(v -> {
//            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            } else {
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            }
//        });
    }
}
