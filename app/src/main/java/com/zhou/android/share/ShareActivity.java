package com.zhou.android.share;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

/**
 * Created by ZhOu on 2018/7/30.
 */

public class ShareActivity extends BaseActivity {

    private ImageView cover;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_share);
    }

    @Override
    protected void init() {
        cover = (ImageView) findViewById(R.id.cover);
    }

    @Override
    protected void addListener() {
        findViewById(R.id.ll_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ShareActivity.this,
                        cover, getString(R.string.cover));
                ActivityCompat.startActivity(ShareActivity.this,
                        new Intent(ShareActivity.this, ShareListActivity.class),
                        optionsCompat.toBundle());
            }
        });
    }
}
