package com.zhou.android.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by ZhOu on 2017/2/9.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        getSupportActionBar().setTitle(getClass().getSimpleName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        addListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
    
    public void back(){
        finish();
    }

    protected void setContentView() {
    }

    protected void initView() {
    }

    protected void addListener() {
    }
}
