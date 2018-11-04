package com.zhou.android.main;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.ui.PhotoPicker;

/**
 * 图片选择
 * Created by ZhOu on 2017/8/4.
 */

public class PhotoPickerActivity extends BaseActivity {

    private PhotoPicker photoPicker;
    private TextView tv;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_photo_picker);
    }

    @Override
    protected void init() {
        photoPicker = (PhotoPicker) findViewById(R.id.photoPicker);
        tv = (TextView) findViewById(R.id.tv);
    }

    @Override
    protected void addListener() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (PhotoPicker.REQUEST_CODE_CHOOSE == requestCode)
            photoPicker.onActivityResult(requestCode, resultCode, data);
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_ok) {
            if (tv.getText().length() > 0)
                tv.setText("");
            else
                tv.setText(photoPicker.getPhotos().toString().replaceAll("[\\[|\\]]", ""));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
