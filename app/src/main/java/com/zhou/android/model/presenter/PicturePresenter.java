package com.zhou.android.model.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;
import com.zhou.android.R;
import com.zhou.android.common.Tools;
import com.zhou.android.model.view.IPictureView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhOu on 2018/4/14.
 */

public class PicturePresenter {

    private final static int PermissionRequestCode = 10001;
    private WeakReference<Activity> context;

    private IPictureView iPictureView;

    public PicturePresenter(Activity activity, IPictureView iPictureView) {
        this.context = new WeakReference<>(activity);
        this.iPictureView = iPictureView;
    }

    //判断权限
    public boolean checkPermission() {

        if (ContextCompat.checkSelfPermission(context.get(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context.get(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions();
            } else {
                iPictureView.requestPermission();
            }
            return false;
        } else
            return true;
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(context.get(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionRequestCode);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PermissionRequestCode == requestCode && grantResults.length > 0) {

            List<String> request = null;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (request == null) {
                        request = new ArrayList<>();
                    }
                    request.add(permissions[i]);
                }
            }
            if (request == null) {
                onLoad();
            } else {
                iPictureView.showToast("权限申请失败,请在应用设置中开启存储权限");
            }
        }
    }

    //获取图片数据，使用异步来做更加合适，或者用 LoaderManager （异步）来加载更合适
    public void onLoad() {

//        List<Uri> data = loadContent();
        getBucket();

        List<String> data2 = load();
//        data.add("https://mmbiz.qpic.cn/mmbiz_jpg/TCHicQEF6XKAK4nL6WXniamS0Fgq1riaPsh9zagQVQHe5X5Zt4lNh1xa8JmdxfpEAmJQW0uQLoxtQocZNOllgibtsw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1");
        iPictureView.onShow(data2);
    }

    /**
     * 绝对路径
     */
    private List<String> load() {
        List<String> data = new ArrayList<>();
        ContentResolver contentResolver = context.get().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE},
                null, null, MediaStore.Images.Media._ID + " desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
//                String type = cursor.getString(3);
                String path = "file://" + cursor.getString(2);//cursor.getColumnIndex(MediaStore.Images.Media.DATA
                data.add(path);
            }
            cursor.close();
        }

        return data;
    }

    /**
     * 图片的 Uri
     */
    private List<Uri> loadContent() {
        List<Uri> data = new ArrayList<>();
        ContentResolver contentResolver = context.get().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DESCRIPTION},
                null, null, MediaStore.Images.Media._ID + " desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)));
                data.add(uri);
            }

            cursor.close();
        }

        return data;
    }

    /**
     * 获取专辑
     */
    private void getBucket() {
        ArrayList<String> album = new ArrayList<>();
        Cursor cursor = context.get().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "count(*)"},
                "0=0) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID, null, MediaStore.Images.Media.BUCKET_ID + " asc");
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String bucket = "[ id:" + cursor.getLong(0) + ",Bucket:" + cursor.getString(1) + ",size:" + cursor.getInt(2) + " ]";
                album.add(bucket);
            }
            cursor.close();
        }
        Log.d("zhou", album.toString());
    }

    public void onItemClick(String uri) {
        Uri target = null;
        if (uri.startsWith("file://"))
            target = Tools.parseImageAbsolutePath(context.get().getContentResolver(), uri);
        else if (uri.startsWith("content://"))
            target = Uri.parse(uri);

        if (target != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(target, "image/*");
            context.get().startActivity(intent);
        }
    }

    public void loadImageView() {

        iPictureView.clearImageView();

        boolean cache = iPictureView.getCache();
        boolean disk = iPictureView.getDisk();
        boolean transformation = iPictureView.getTransformation();

        Picasso picasso = Picasso.with(context.get());
        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);

        String path = "https://avatars2.githubusercontent.com/u/9563634?s=400&u=6c9844a5ee91e0385888cbd5708af59f4062d651&v=4";
        RequestCreator requestCreator = picasso.load(path)
                .config(Bitmap.Config.RGB_565)
                .placeholder(R.drawable.ic_empty_zhihu)
                .error(R.drawable.ic_failed)
                .fit();

        if (!cache) {
            requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE);
        }
        if (!disk) {
            requestCreator.networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE);
        }

        if (transformation) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(0xffcccccc);
            paint.setStyle(Paint.Style.FILL);
            final float round = Tools.dip2pxf(context.get(), 8);
            requestCreator.transform(new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {

                    Bitmap src = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight());
                    Canvas canvas = new Canvas(src);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        float r = source.getWidth() / 4 * 3;
                        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2, r, paint);
                    } else {
                        canvas.drawRoundRect(round, round, source.getWidth() - round, source.getHeight() - round,
                                round, round, paint);
                    }
                    canvas.drawBitmap(source, 0, 0, paint);
                    if (!source.isRecycled()) {
                        source.recycle();
                    }
                    return src;
                }

                @Override
                public String key() {
                    return "PicassoTransformation";
                }
            });
        }
        requestCreator.into(iPictureView.getTarget(), new Callback() {
            @Override
            public void onSuccess() {
                iPictureView.showToast("success");
            }

            @Override
            public void onError() {
                iPictureView.showToast("error");
            }
        });
    }
}
