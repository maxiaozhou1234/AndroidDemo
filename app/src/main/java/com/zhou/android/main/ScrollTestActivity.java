package com.zhou.android.main;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;

/**
 * ScrollView 自动滑动
 * <p>
 * Created by ZhOu on 2017/3/23.
 */

public class ScrollTestActivity extends BaseActivity {

    private ScrollView scrollView;
    private LinearLayout ll_content;
    private Handler handler = new Handler();
    private int padding = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_scroll);
    }

    @Override
    protected void init() {
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        padding = Tools.dip2px(this, 15);

        for (int i = 1; i < 8; i++) {
            addItem("ItemTitle_" + i, "ItemContent_" + i);
        }
    }

    @Override
    protected void addListener() {

    }

    private void addItem(String title, String content) {
        final TextView tv_title = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = padding / 3;
        tv_title.setLayoutParams(params);
        tv_title.setTextColor(Color.WHITE);
        tv_title.setPadding(padding, padding, padding, padding);
        tv_title.setBackgroundColor(Color.rgb(60, 120, 216));
        Drawable drawable = getResources().getDrawable(R.drawable.btn_status);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tv_title.setCompoundDrawables(null, null, drawable, null);
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv_title.setText(title);
        tv_title.setSelected(false);

        final TextView tv_content = new TextView(this);
        tv_content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dip2px(this, 200)));
        tv_content.setTextColor(Color.BLACK);
        tv_content.setGravity(Gravity.CENTER);
        tv_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv_content.setText(content);
        tv_content.setVisibility(View.GONE);

        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                boolean show = tv_title.isSelected();
                tv_title.setSelected(!show);
                tv_content.setVisibility(show ? View.GONE : View.VISIBLE);
                if (!show)
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int[] loc = new int[2];
                            v.getLocationOnScreen(loc);
                            int offset = loc[1] - v.getHeight() - padding * 2;//v.getTop();
                            if (offset < 0)
                                offset = 0;
                            scrollView.smoothScrollBy(0, offset);
                        }
                    }, 200);
            }
        });

        ll_content.addView(tv_title);
        ll_content.addView(tv_content);
    }
}
