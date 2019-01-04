package com.zhou.android.main;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;
import com.zhou.android.common.Tools;
import com.zhou.android.ui.CardStackLayout;

/**
 * 嵌套滑动测试
 */
public class NestedScrollActivity extends BaseActivity {

    private final static String TAG = "scroll";

    private CardStackLayout cardStack;
    private int scrollY = 0;
    private boolean flag = true;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_nestedscroll);
    }

    @Override
    protected void init() {

        cardStack = findViewById(R.id.cardStack);
//        List<String> data = new ArrayList<>();
//        for (int i = 0; i < 50; i++) {
//            data.add("simple test " + i);
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
//        listView.setAdapter(adapter);

        scrollY = Tools.dip2px(this, 50);
        cardStack.setTargetOffset(scrollY);

//        TextView label = findViewById(R.id.label);
//        label.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d("zhou", "onTouch");
//                return false;
//            }
//        });
    }

    @Override
    protected void addListener() {
        findViewById(R.id.btn).setOnClickListener(v -> {
            cardStack.testCompat(flag ? -50 : 20);
            flag = !flag;
        });
    }



}
