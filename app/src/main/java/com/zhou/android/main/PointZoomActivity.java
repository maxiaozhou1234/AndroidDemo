package com.zhou.android.main;

import android.graphics.BitmapFactory;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.ui.PointZoomView;

import java.util.Locale;

/**
 * 记录点击点及点在图片相对位置
 * Created by ZhOu on 2017/7/9.
 */

public class PointZoomActivity extends BaseActivity {

    PointZoomView point;
    ScrollView scrollView;
    TextView tv;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_point_zoom);
    }

    @Override
    protected void init() {
        point = (PointZoomView) findViewById(R.id.point);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tv = (TextView) findViewById(R.id.tv);
    }

    @Override
    protected void addListener() {
        point.setListener(new PointZoomView.PercentListener() {
            @Override
            public void getPercent(float x, float y) {
                String text = String.format(Locale.getDefault(), "x(%s): %.2f, y(%s): %.2f\n", "%", x, "%", y);
                tv.append(text);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            @Override
            public void outRoom() {
                Toast.makeText(PointZoomActivity.this, "超出图片位置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            point.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.create));
        }
    }
}
