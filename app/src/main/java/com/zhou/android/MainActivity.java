package com.zhou.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zhou.android.adapter.GridViewAdapter;
import com.zhou.android.bluetooth.BluetoothActivity;
import com.zhou.android.item.GridViewItem;
import com.zhou.android.kotlin.SimpleListKotlinActivity;
import com.zhou.android.main.BroadcastReceiverActivity;
import com.zhou.android.main.CallLogActivity;
import com.zhou.android.main.CameraImageActivity;
import com.zhou.android.main.CameraUtilTestActivity;
import com.zhou.android.main.ContactActivity;
import com.zhou.android.main.DoubleCameraActivity;
import com.zhou.android.main.DoubleNewCameraActivity;
import com.zhou.android.main.FingerPrintActivity;
import com.zhou.android.main.FloatBallActivity;
import com.zhou.android.main.ForbidScreenshotActivity;
import com.zhou.android.main.PhotoPickerActivity;
import com.zhou.android.main.PointZoomActivity;
import com.zhou.android.main.RecyclerViewScrollActivity;
import com.zhou.android.main.ScrollTestActivity;
import com.zhou.android.main.SurfaceActivity;
import com.zhou.android.main.UdpReceiverActivity;
import com.zhou.android.main.VideoActivity;
import com.zhou.android.model.ui.WeatherActivity;
import com.zhou.android.model.ui.OkHttpActivity;
import com.zhou.android.model.ui.PicassoActivity;
import com.zhou.android.retrofit.RetrofitActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<GridViewItem> list = new ArrayList<>();
    private GridViewAdapter gridViewAdapter;
//    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this, list);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridViewItem item = list.get(position);
                try {
                    startActivity(new Intent(MainActivity.this, item.clz));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, String.format("start %s failed", TextUtils.isEmpty(item.zhName) ? item.targetClass : item.zhName), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Uri uri = getIntent().getData();
        if (null != uri) {
            String value = uri.getQueryParameter("data");
            if (!TextUtils.isEmpty(value))
                try {
                    int position = Integer.parseInt(value);
                    if (position < list.size()) {
                        GridViewItem item = list.get(position);
                        startActivity(new Intent(MainActivity.this, item.clz));
                    }
                } catch (Exception e) {
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list.size() == 0) {

            list.add(new GridViewItem(SurfaceActivity.class, "界面绘制"));
            list.add(new GridViewItem(FingerPrintActivity.class, "指纹解锁"));
            list.add(new GridViewItem(ScrollTestActivity.class, "滑动测试"));
            list.add(new GridViewItem(ForbidScreenshotActivity.class, "禁止截屏"));
            list.add(new GridViewItem(FloatBallActivity.class, "悬浮球"));
            list.add(new GridViewItem(BroadcastReceiverActivity.class, "App广播"));
            list.add(new GridViewItem(ContactActivity.class, "通讯录"));
            list.add(new GridViewItem(CallLogActivity.class, "通话记录"));
//        list.add(new GridViewItem(BluetoothMainActivity.class, "蓝牙通信"));
            list.add(new GridViewItem(BluetoothActivity.class, "蓝牙通信"));
            list.add(new GridViewItem(SimpleListKotlinActivity.class, "Kotlin"));
            list.add(new GridViewItem(PointZoomActivity.class, "Point Zoom"));
            list.add(new GridViewItem(PhotoPickerActivity.class, "图片选择"));
            list.add(new GridViewItem(RetrofitActivity.class, "Retrofit"));
            list.add(new GridViewItem(RecyclerViewScrollActivity.class, "RecyclerScroll"));
            list.add(new GridViewItem(PicassoActivity.class, "Picasso 显示"));
            list.add(new GridViewItem(VideoActivity.class, "视频播放"));
            list.add(new GridViewItem(OkHttpActivity.class, "OkHttp"));
            list.add(new GridViewItem(UdpReceiverActivity.class, "Udp 监听"));
            list.add(new GridViewItem(WeatherActivity.class, "和风天气"));
            list.add(new GridViewItem(DoubleCameraActivity.class, "前后摄像"));
            list.add(new GridViewItem(DoubleNewCameraActivity.class, "前后摄像2"));
            list.add(new GridViewItem(CameraImageActivity.class, "摄像预览"));
            list.add(new GridViewItem(CameraUtilTestActivity.class, "CameraUtil"));
            list.add(new GridViewItem("Test", "测试"));
            list.add(new GridViewItem("Apple", "苹果"));
            list.add(new GridViewItem("Banana", "香蕉"));
            list.add(new GridViewItem("camera", "相机"));
            list.add(new GridViewItem("roma", null));

            gridViewAdapter.notifyDataSetChanged();

        }
    }
}
