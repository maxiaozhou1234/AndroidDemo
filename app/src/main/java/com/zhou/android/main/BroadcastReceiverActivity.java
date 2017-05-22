package com.zhou.android.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;

public class BroadcastReceiverActivity extends BaseActivity {
    public final String TAG = "zhou";
    private TestReceiver testReceiver, testReceiver2;
    private MediaPlayer mp;
    private boolean isStop = false;
    private String uri = "";

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_broadcast);
        testReceiver = new TestReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LocalBroadcast");
        intentFilter.addAction("Broadcast");
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, intentFilter);
        testReceiver2 = new TestReceiver();
        registerReceiver(testReceiver2, intentFilter);

        RingtoneManager rm = new RingtoneManager(this);
        rm.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = rm.getCursor();
        if (cursor.moveToFirst()) {
            uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
        }
        if (TextUtils.isEmpty(uri))
            uri = "content://media/internal/audio/media/0";
        mp = new MediaPlayer();
//        mp.reset();
//        try {
//            mp.setDataSource(this, Uri.parse(uri));
//            mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
//            mp.setLooping(true);
////            mp.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void init() {

    }

    @Override
    protected void addListener() {

    }

    public void onClick(View v) {
        int id = v.getId();
        if (R.id.btn_local == id) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("LocalBroadcast"));
            Log.d(TAG, "Local send.");
        } else if (R.id.btn_system == id) {
            sendBroadcast(new Intent("Broadcast"));
            Log.d(TAG, "System send.");
        } else if (R.id.btn_start == id) {
            try {
                mp = new MediaPlayer();
                mp.setDataSource(this, Uri.parse(uri));
                mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mp.setLooping(true);
                mp.prepare();
                mp.start();
                isStop = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (R.id.btn_stop == id) {
            if (mp != null) {
                mp.stop();
                mp.release();
                isStop = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (testReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(testReceiver);
            testReceiver = null;
        }
        if (testReceiver2 != null) {
            unregisterReceiver(testReceiver2);
            testReceiver2 = null;
        }
    }

    class TestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("LocalBroadcast")) {
                Toast.makeText(context, "Local Broadcast", Toast.LENGTH_LONG).show();
            } else if (action.equals("Broadcast")) {
                Toast.makeText(context, "System Broadcast", Toast.LENGTH_LONG).show();
//            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo info = cm.getActiveNetworkInfo();
//                if (info != null && info.isAvailable()) {
//                    String name = info.getTypeName();
//                    Toast.makeText(BroadcastReceiverActivity.this, name + "", Toast.LENGTH_LONG).show();
//                }
            }
        }
    }
}
