package com.zhou.android.model.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

    private RecyclerView recyclerView;
    private LinearLayout ll_other;
    private CheckBox cb_cache, cb_disk, cb_transformation;
    private ImageView imageView;

    private PictureAdapter pictureAdapter;
    private List<String> data = new ArrayList<>();
    private PicturePresenter picturePresenter = new PicturePresenter(this, this);

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_picture_view);
    }

    @Override
    protected void init() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        ll_other = (LinearLayout) findViewById(R.id.ll_other);
        cb_cache = (CheckBox) findViewById(R.id.cb_cache);
        cb_disk = (CheckBox) findViewById(R.id.cb_disk);
        cb_transformation = (CheckBox) findViewById(R.id.cb_transformation);
        imageView = (ImageView) findViewById(R.id.imageView);

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

        findViewById(R.id.btn_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picturePresenter.loadImageView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picasso, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_other == item.getItemId()) {
            switchView(true);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void switchView(boolean showOther) {
        if (!showOther) {
            Picasso.with(this).setIndicatorsEnabled(false);
        }
        recyclerView.setVisibility(showOther ? View.GONE : View.VISIBLE);
        ll_other.setVisibility(showOther ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        picturePresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void back() {
        if (ll_other.isShown())
            switchView(false);
        else
            super.back();
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

    @Override
    public boolean getCache() {
        return cb_cache.isChecked();
    }

    @Override
    public boolean getDisk() {
        return cb_disk.isChecked();
    }

    @Override
    public boolean getTransformation() {
        return cb_transformation.isChecked();
    }

    @Override
    public ImageView getTarget() {
        return imageView;
    }

    @Override
    public void clearImageView() {
        imageView.setImageDrawable(null);
    }

}
