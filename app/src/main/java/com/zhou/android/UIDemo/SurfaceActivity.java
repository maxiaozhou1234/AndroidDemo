package com.zhou.android.UIDemo;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import com.zhou.android.R;

/**
 * Created by ZhOu on 2017/2/8.
 */

public class SurfaceActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_surface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle("Surface");
    }
}
