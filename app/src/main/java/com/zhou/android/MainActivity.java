package com.zhou.android;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.zhou.android.adapter.GridViewAdapter;
import com.zhou.android.bluetooth.BluetoothActivity;
import com.zhou.android.item.GridViewItem;
import com.zhou.android.kotlin.AnalysisDecorActivity;
import com.zhou.android.kotlin.NtpTimeActivity;
import com.zhou.android.kotlin.SimpleListKotlinActivity;
import com.zhou.android.kotlin.StorageCleanActivity;
import com.zhou.android.kotlin.ThrowExceptionActivity;
import com.zhou.android.kotlin.UndoTestActivity;
import com.zhou.android.kotlin.album.AsViewPagerActivity;
import com.zhou.android.main.AppSizeActivity;
import com.zhou.android.main.BroadcastReceiverActivity;
import com.zhou.android.main.CallLogActivity;
import com.zhou.android.main.CameraH264Activity;
import com.zhou.android.main.CameraUtilTestActivity;
import com.zhou.android.main.ContactActivity;
import com.zhou.android.main.FingerPrintActivity;
import com.zhou.android.main.FloatBallActivity;
import com.zhou.android.main.FocusAnimActivity;
import com.zhou.android.main.FocusDrawActivity;
import com.zhou.android.main.ForbidScreenshotActivity;
import com.zhou.android.main.FunctionGuideActivity;
import com.zhou.android.main.LocalNetActivity;
import com.zhou.android.main.NestedBehaviorActivity;
import com.zhou.android.main.NestedScrollActivity;
import com.zhou.android.main.PhotoPickerActivity;
import com.zhou.android.main.PointZoomActivity;
import com.zhou.android.main.PreviewWithCamera2Activity;
import com.zhou.android.main.RecyclerViewScrollActivity;
import com.zhou.android.main.RoundViewActivity;
import com.zhou.android.main.ScrollTestActivity;
import com.zhou.android.main.StorageActivity;
import com.zhou.android.main.SurfaceActivity;
import com.zhou.android.main.TestRvActivity;
import com.zhou.android.main.TimeLineActivity;
import com.zhou.android.main.VerifyCodeActivity;
import com.zhou.android.main.VideoActivity;
import com.zhou.android.model.ui.OkHttpActivity;
import com.zhou.android.model.ui.PicassoActivity;
import com.zhou.android.model.ui.WeatherActivity;
import com.zhou.android.retrofit.RetrofitActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private volatile List<GridViewItem> list = new ArrayList<>();
    private GridViewAdapter gridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridViewAdapter = new GridViewAdapter(this, list);

        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridViewItem item = list.get(position);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
                        ActivityCompat.startActivity(MainActivity.this, new Intent(MainActivity.this, item.clz), options.toBundle());
                    } else {
                        startActivity(new Intent(MainActivity.this, item.clz));
                    }
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
                    //
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list.size() == 0) {

            new Thread(() -> {

                list.add(new GridViewItem(TestRvActivity.class, "刷新加载RV"));
                list.add(new GridViewItem(VerifyCodeActivity.class, "短信验证码控件"));
                list.add(new GridViewItem(CameraH264Activity.class, "h264硬编码"));
                list.add(new GridViewItem(PreviewWithCamera2Activity.class, "预览帧拍照"));
                list.add(new GridViewItem(TimeLineActivity.class, "时间线"));
                list.add(new GridViewItem(FunctionGuideActivity.class, "功能引导"));
                list.add(new GridViewItem(RoundViewActivity.class, "圆形图片"));
                list.add(new GridViewItem(FocusDrawActivity.class, "图片动画"));
                list.add(new GridViewItem(FocusAnimActivity.class, "识别框动画"));
                list.add(new GridViewItem(NtpTimeActivity.class, "同步时间获取"));
                list.add(new GridViewItem(ThrowExceptionActivity.class, "崩溃重启"));
                list.add(new GridViewItem(AnalysisDecorActivity.class, "视图分析"));
                list.add(new GridViewItem(UndoTestActivity.class, "无操作"));
                list.add(new GridViewItem(LocalNetActivity.class, "局域网"));
                list.add(new GridViewItem(AsViewPagerActivity.class, "相册"));
                list.add(new GridViewItem(NestedBehaviorActivity.class, "Behavior"));
                list.add(new GridViewItem(NestedScrollActivity.class, "嵌套滑动"));
                list.add(new GridViewItem(StorageCleanActivity.class, "内存查询kt"));
                list.add(new GridViewItem(StorageActivity.class, "存储空间"));
                list.add(new GridViewItem(AppSizeActivity.class, "App空间"));
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
                list.add(new GridViewItem(PicassoActivity.class, "Picasso"));
                list.add(new GridViewItem(VideoActivity.class, "视频循环"));
                list.add(new GridViewItem(OkHttpActivity.class, "OkHttp"));
//            list.add(new GridViewItem(UdpReceiverActivity.class, "Udp 监听"));
                list.add(new GridViewItem(WeatherActivity.class, "和风天气"));
//                list.add(new GridViewItem(DoubleCameraActivity.class, "前后摄像"));
//                list.add(new GridViewItem(CameraImageActivity.class, "摄像预览"));
                list.add(new GridViewItem(CameraUtilTestActivity.class, "CameraUtil"));

                list.add(new GridViewItem("Test", "测试"));
                list.add(new GridViewItem("Apple", "苹果"));
                list.add(new GridViewItem("Banana", "香蕉"));
                list.add(new GridViewItem("roma", null));

                runOnUiThread(() -> gridViewAdapter.notifyDataSetChanged());
            }).start();

        }
    }
}
