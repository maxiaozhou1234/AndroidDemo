package com.zhou.android.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhou.android.R;
import com.zhou.android.adapter.RecyclerAdapter;
import com.zhou.android.adapter.RecyclerListener;

import java.util.List;

/**
 * 图片选择器
 * Created by ZhOu on 2017/8/4.
 */

public class PhotoPicker extends LinearLayout {

    public final static int REQUEST_CODE_CHOOSE = 10001;

    private Context context;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private int spanCount = 4;

    //    private Dialog dialog;
    private AlertDialog ad;
    private int removeIndex = -1, showIndex = -1;

    private RxPermissions rxPermissions;

    public PhotoPicker(Context context) {
        this(context, null);
    }

    public PhotoPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoPicker(final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.listformat_photo_picker, this);
        this.context = context;

        rxPermissions = new RxPermissions((Activity) context);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));

        recyclerAdapter = new RecyclerAdapter(context, null);
        recyclerView.setAdapter(recyclerAdapter);

        recyclerAdapter.setListener(new RecyclerListener() {
            @Override
            public void onItemClick(int position) {
                showPhoto(position);
            }

            @Override
            public void onItemLongClick(int position) {
                removeIndex = position;
                showDialog();
            }

            @Override
            public void onItemAdd() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (!rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(context, "没有存储读取权限，请在设置中开启", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                Matisse.from((Activity) context)
                        .choose(MimeType.allOf())
                        .countable(true)
                        .capture(true)
                        .captureStrategy(new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider"))
                        .maxSelectable(9)
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(new GlideEngine())
                        .forResult(REQUEST_CODE_CHOOSE);
            }
        });
    }

    public void onActivityResult(int requestCode, int result, Intent data) {
        if (REQUEST_CODE_CHOOSE == requestCode && Activity.RESULT_OK == result)
            recyclerAdapter.notifyData(Matisse.obtainResult(data));
    }

    private void showDialog() {
        if (ad == null) {
            ad = new AlertDialog.Builder(context)
                    .setMessage("是否删除这张图片？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            recyclerAdapter.remove(removeIndex);
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            removeIndex = -1;
                        }
                    }).create();
        }
        ad.show();
    }

    private void showPhoto(int position) {
        Uri uri = recyclerAdapter.getItem(position);
        if (uri == null)
            return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }

    public List<Uri> getPhotos() {
        return recyclerAdapter.getData();
    }
}
