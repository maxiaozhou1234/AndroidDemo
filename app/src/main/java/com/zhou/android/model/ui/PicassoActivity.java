package com.zhou.android.model.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.zhou.android.R;
import com.zhou.android.adapter.PictureAdapter;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;
import com.zhou.android.model.presenter.PicturePresenter;
import com.zhou.android.model.view.IPictureView;
import com.zhou.android.ui.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片显示
 * Created by ZhOu on 2018/4/14.
 */

public class PicassoActivity extends BaseActivity implements IPictureView {

    private PictureAdapter pictureAdapter;
    private List<String> data = new ArrayList<>();
    private PicturePresenter picturePresenter = new PicturePresenter(this, this);

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_picture_view);
    }

    @Override
    protected void init() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        pictureAdapter = new PictureAdapter(this, data);
        recyclerView.setAdapter(pictureAdapter);

        if (picturePresenter.checkPermission()) {
            picturePresenter.onLoad();
        }
    }

    @Override
    protected void addListener() {
        pictureAdapter.setOnItemClickListener(new PictureAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String uri) {
                picturePresenter.onItemClick(uri);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        picturePresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void requestPermission() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage("本应用需要外部存储权限才可使用，请允许")
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        picturePresenter.requestPermissions();
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("已拒绝，请在应用设置中开启存储权限");
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void onShow(List<String> pictures) {
        this.data.clear();
        this.data.addAll(pictures);
        pictureAdapter.notifyDataSetChanged();
    }

    @Override
    public void showToast(String message) {
        ToastUtils.show(PicassoActivity.this, message);
    }

}
