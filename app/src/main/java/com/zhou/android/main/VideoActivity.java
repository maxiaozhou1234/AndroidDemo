package com.zhou.android.main;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

import java.io.File;

/**
 * Created by ZhOu on 2018/4/18.
 */

public class VideoActivity extends BaseActivity {

    private VideoView videoView;
    private boolean stop = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_video);
    }

    @Override
    protected void init() {
        videoView = (VideoView) findViewById(R.id.videoView);

        String path = Environment.getExternalStorageDirectory() + "/Pictures/test.mp4";
        File file = new File(path);
        if (!file.exists()) {
            path = "android.resource://" + getPackageName() + "/" + R.raw.test_2;
        }
        videoView.setVideoURI(Uri.parse(path));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (stop) return;
                videoView.start();
            }
        });
        videoView.start();

    }

    @Override
    protected void addListener() {

    }

    @Override
    public void back() {
        if (videoView != null) {
            stop = true;
            try {
                videoView.stopPlayback();
                videoView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.back();
    }
}
