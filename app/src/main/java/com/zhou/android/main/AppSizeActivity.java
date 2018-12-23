package com.zhou.android.main;

import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.AppQueryUtil;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.CommonAdapter;
import com.zhou.android.common.ViewHolder;

import java.util.ArrayList;
import java.util.List;

//查看已安装 App 大小
public class AppSizeActivity extends BaseActivity {

    private PackageManager pm;
    private CommonAdapter<AppQueryUtil.Item> adapter;

    private List<AppQueryUtil.Item> data = new ArrayList<>();

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_app_size);
    }

    @Override
    protected void init() {
        ListView listView = findViewById(R.id.listView);

        adapter = new CommonAdapter<AppQueryUtil.Item>(this, data, R.layout.layout_app) {
            @Override
            protected void fillData(ViewHolder holder, int position) {
                AppQueryUtil.Item item = data.get(position);
                ((TextView) holder.getView(R.id.packageName)).setText(item.packageName);
                ((TextView) holder.getView(R.id.size)).setText(item.getAppSizeString());
                ImageView icon = holder.getView(R.id.icon);
                icon.setImageDrawable(item.icon);
            }
        };
        listView.setAdapter(adapter);

        pm = getPackageManager();
    }

    @Override
    protected void addListener() {

    }

    public void onClick(View v) {
        AppQueryUtil.queryAppSize(this, new AppQueryUtil.Callback() {
            @Override
            public void onSuccess(List<AppQueryUtil.Item> _data) {
                data.clear();
                data.addAll(_data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(List<AppQueryUtil.Item> _data, int error) {
                data.clear();
                data.addAll(_data);
                adapter.notifyDataSetChanged();

                if (error == AppQueryUtil.WITHOUT_PERMISSION) {
                    AppQueryUtil.requestPermission(AppSizeActivity.this);
                }
            }
        });
    }
}
