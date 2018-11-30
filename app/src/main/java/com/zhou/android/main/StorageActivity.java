package com.zhou.android.main;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * 存储查询
 * Created by Administrator on 2018/11/30.
 */

public class StorageActivity extends BaseActivity {

    private TextView text;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_storage);
    }

    @Override
    protected void init() {
        text = findViewById(R.id.text);
    }

    @Override
    protected void addListener() {

    }

    StatFs statFs;

    public void onClick(View v) {

        statFs = new StatFs(Environment.getRootDirectory().getPath());
        append("Root = " + getUnit(statFs.getTotalBytes()));

        statFs = new StatFs(Environment.getDataDirectory().getPath());
        append("Data = " + getUnit(statFs.getTotalBytes()));

        statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
//        long blockSize = statFs.getBlockSizeLong();
//        long blockCount = statFs.getBlockCountLong();

        long totalSize = statFs.getTotalBytes();
        long availableSize = statFs.getAvailableBytes();

        append("total = " + getUnit(totalSize));
        append("availableSize = " + getUnit(availableSize));
    }

    private String[] units = {"B", "KB", "MB", "GB", "TB"};

    private String getUnit(float size) {
        int index = 0;
        while (size > 1024 && index < 4) {
            size = size / 1024;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s", size, units[index]);
    }

    private void append(String _text) {
        text.append(_text);
        text.append("\n");
    }
}
