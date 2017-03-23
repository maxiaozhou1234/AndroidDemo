package com.zhou.android.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhou.android.common.Tools;
import com.zhou.android.ui.FloatView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 悬浮球服务
 * Created by ZhOu on 2017/3/23.
 */

public class FloatBallService extends Service {

    private FloatView floatView;
    private WindowManager wm;
    private Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        floatView = new FloatView(this);
        floatView.setOnClickListener(new FloatView.OnClickListener() {
            @Override
            public void onViewClick(View v) {
                Toast.makeText(context, "悬浮球点击", Toast.LENGTH_LONG).show();
//                String[] info = Tools.getTopActivity(context);
//                try {
//                    Context c = createPackageContext(info[0], Context.CONTEXT_IGNORE_SECURITY);
//                    Class clz = c.getClassLoader().loadClass(info[1]);
//                    Activity _a = (Activity) clz.newInstance();
//                    getScreen(_a);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.x = Tools.getScreenWidth(this) - floatView.getMeasuredWidth() - 30;
        layoutParams.y = Tools.getScreenHeight(this) / 2;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wm.addView(floatView, layoutParams);
    }

    @Override
    public void onDestroy() {
        if (floatView != null)
            wm.removeView(floatView);
        super.onDestroy();
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private void getScreen(Activity activity) {
        View view = activity.getWindow().getDecorView();
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
        }
    }
}
