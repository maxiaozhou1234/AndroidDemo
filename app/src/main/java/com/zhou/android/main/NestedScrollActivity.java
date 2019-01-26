package com.zhou.android.main;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.ui.CardStackLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌套滑动测试
 */
public class NestedScrollActivity extends BaseActivity {

    private final static String TAG = "scroll";

    private CardStackLayout cardStack;
    private ListView listView;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_nestedscroll);
    }

    @Override
    protected void init() {

        cardStack = findViewById(R.id.cardStack);
        listView = findViewById(R.id.listView);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            data.add("simple test " + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
        listView.setAdapter(adapter);

    }

    @Override
    protected void addListener() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardStack.post(() -> {
            Log.d("scroll", "w h = " + cardStack.getWidth() + " , " + cardStack.getHeight());
        });
    }
}
