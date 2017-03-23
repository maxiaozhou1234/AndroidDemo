package com.zhou.android.main;

import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 禁止屏幕截屏
 * <p>
 * Created by ZhOu on 2017/3/23.
 */

public class ForbidScreenshotActivity extends BaseActivity {

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_forbid_screenshot);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void addListener() {

    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    public void onClick(View v) {

        v.setVisibility(View.GONE);

        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap bitmap = view.getDrawingCache();
        Bitmap _b = Bitmap.createBitmap(bitmap, 0, 0, Tools.getScreenWidth(this), Tools.getScreenHeight(this));
        view.destroyDrawingCache();
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/zhou/screenshot/" + format.format(new Date()) + ".png");
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            _b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
            _b.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            v.setVisibility(View.VISIBLE);
        }
    }
}
