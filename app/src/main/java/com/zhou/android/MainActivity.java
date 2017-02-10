package com.zhou.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.zhou.android.SysDemo.FingerPrintActivity;
import com.zhou.android.UIDemo.SurfaceActivity;
import com.zhou.android.adapter.GridViewAdapter;
import com.zhou.android.item.GridViewItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridView);
        final List<GridViewItem> list = new ArrayList<>();
        list.add(new GridViewItem(SurfaceActivity.class, "界面绘制"));
        list.add(new GridViewItem(FingerPrintActivity.class, "指纹解锁"));
        list.add(new GridViewItem("Test", "测试"));
        list.add(new GridViewItem("Apple", "苹果"));
        list.add(new GridViewItem("Banana", "香蕉"));
        list.add(new GridViewItem("camera", "相机"));
        list.add(new GridViewItem("roma", null));
        GridViewAdapter gridViewAdapter = new GridViewAdapter(this, list);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridViewItem item = list.get(position);
                try {
                    startActivity(new Intent(MainActivity.this, item.clz));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, String.format("start %s failed", TextUtils.isEmpty(item.zhName) ? item.targetClass : item.zhName), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
