package com.zhou.android.main;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌套滑动测试
 */
public class NestedScrollActivity extends BaseActivity {

    private final static String TAG = "scroll";

    private TextView text;
    private ListView listView;
    private float disX = 0f, disY = 0f;
    private int lastScrollY = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_nestedscroll);
    }

    @Override
    protected void init() {

        text = findViewById(R.id.text);
        listView = findViewById(R.id.listView);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            data.add("simple test " + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
        listView.setAdapter(adapter);

        disX = Tools.dip2pxf(this, 10);
        disY = -Tools.dip2pxf(this, 30);
        Log.d(TAG, "disY = " + disY);

    }

    @Override
    protected void addListener() {
        findViewById(R.id.btn).setOnClickListener(v -> {

            if (text.getTranslationY() == 0) {
                text.setTranslationY(disY);
            } else {
                text.setTranslationY(0);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View child = view.getChildAt(0);
                if (child == null)
                    return;
                int y = firstVisibleItem * (child.getHeight()) - (child.getTop());
                Log.d(TAG, "scrollY = " + y);
                scroll(y, lastScrollY <= y);
                lastScrollY = y;
            }
        });

//        listView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                return false;
//            }
//        });
    }

    private void scroll(int _y, boolean up) {//true:由下往上划
        float y = _y * -1f;
        int dex = _y - lastScrollY;
        if (up && text.getTranslationY() > disY) {//disY 是负的
            if (lastScrollY > -disY) {
                y = text.getTranslationY() - dex;
                if (y < disY) {
                    y = disY;
                }
            }
            text.setTranslationY(y);
            text.setTranslationX(disX * y / disY);

        } else if (!up && text.getTranslationY() != 0) {

            y = text.getTranslationY() - dex;

            if (y > 0) {
                y = 0;
            }
            text.setTranslationY(y);
            text.setTranslationX(disX * y / disY);
//            text.setTranslationY(0);
//            text.setTranslationX(0);
        }
    }

}
