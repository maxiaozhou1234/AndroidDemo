package com.zhou.android.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.GV;
import com.zhou.android.ui.PwdEditText;

/**
 * Created by ZhOu on 2017/2/9.
 */

public class FingerPrintActivity extends BaseActivity {

    private final static String TAG = "zhou";

    private final static int FINGERPRINT = 0x1001;

    private EditText et_pay;

    private FingerprintManager fingerprintManager;

//    private KeyguardManager keyguardManager;

    private InputMethodManager imm;

    private int count = 3;

    private Dialog fingerDialog, keyDialog;

    private boolean fingerPrintIsUsable = true;

    private FingerPrintCallback fingerPrintCallback = new FingerPrintCallback();

    private CancellationSignal cancellationSignal = new CancellationSignal();

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_fingerprint);
    }

    @Override
    protected void init() {

        if (!getSharedPreferences(GV.Config, 0).getBoolean(GV.HasFingerPrintApi, false)) {
            Log.d(TAG, "Your device unsupported FingerPrint!");
            fingerPrintIsUsable = false;
        }

        fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
//        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        //判断是否有指纹模块
        try {
            if (fingerPrintIsUsable && !fingerprintManager.isHardwareDetected()) {
                Log.d(TAG, "Your device unsupported FingerPrint!");
                fingerPrintIsUsable = false;
            }
            if (fingerPrintIsUsable && !fingerprintManager.hasEnrolledFingerprints()) {
                Log.e(TAG, "Your device dose not have a fingerprint.");
                fingerPrintIsUsable = false;

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Use FingerPrint Api failed!");
            fingerPrintIsUsable = false;
        }

        if (fingerPrintIsUsable) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.USE_FINGERPRINT)) {
                    new AlertDialog.Builder(this)
                            .setTitle("权限提示")
                            .setMessage("本应用需要添加指纹权限才可以使用指纹，请允许。")
                            .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ActivityCompat.requestPermissions(FingerPrintActivity.this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGERPRINT);
                                }
                            }).create().show();
                } else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGERPRINT);
            }
        }

        et_pay = (EditText) findViewById(R.id.et_pay);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Log.d(TAG, "result:" + fingerPrintIsUsable);
    }

    @Override
    protected void addListener() {
    }

    public void onClick(View view) {
        et_pay.clearFocus();
        if (fingerPrintIsUsable &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED)
            useFingerPrint();
        else useKeyboard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (FINGERPRINT == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(FingerPrintActivity.this, "Permission pass.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FingerPrintActivity.this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void useFingerPrint() {
        count = 3;
        View view = LayoutInflater.from(this).inflate(R.layout.listformat_dialog_finger, null);
        ImageButton ib_close = (ImageButton) view.findViewById(R.id.ib_close);
        Button btn_key = (Button) view.findViewById(R.id.btn_pwd);

        fingerDialog = new Dialog(this);
        fingerDialog.setContentView(view);
        fingerDialog.setCancelable(true);
        fingerDialog.setCanceledOnTouchOutside(false);
        ib_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fingerDialog.dismiss();
            }
        });

        btn_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fingerDialog.dismiss();
                useKeyboard();
            }
        });
        fingerDialog.show();

        fingerprintManager.authenticate(null, cancellationSignal, 0, fingerPrintCallback, handler);

    }

    private void useKeyboard() {

        if (fingerPrintIsUsable) cancellationSignal.cancel();

        View view = LayoutInflater.from(this).inflate(R.layout.listformat_dialog_usepwd, null);
        ImageButton ib_close = (ImageButton) view.findViewById(R.id.ib_close);
        final PwdEditText pwdEditText = (PwdEditText) view.findViewById(R.id.pwdEditText);
        keyDialog = new Dialog(this);
        keyDialog.setContentView(view);
        keyDialog.setCancelable(true);
        keyDialog.setCanceledOnTouchOutside(false);
        ib_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyDialog.dismiss();
            }
        });
        keyDialog.show();
        pwdEditText.setOnFinishListener(new PwdEditText.OnFinishListener() {
            @Override
            public void onFinish(String pwd) {
                Toast.makeText(FingerPrintActivity.this, "PassWord:" + pwd, Toast.LENGTH_SHORT).show();
                imm.hideSoftInputFromWindow(pwdEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                handler.sendEmptyMessageDelayed(0x003, 1000);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x001) {
                fingerDialog.dismiss();
                Toast.makeText(FingerPrintActivity.this, "Success", Toast.LENGTH_LONG).show();
            } else if (msg.what == 0x002) {
                fingerDialog.dismiss();
                useKeyboard();
            } else if (msg.what == 0x003) {
                keyDialog.dismiss();
            }
        }
    };

    class FingerPrintCallback extends FingerprintManager.AuthenticationCallback {

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Log.d(TAG, "success");
            handler.sendEmptyMessage(0x001);
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            Log.e(TAG, "error:" + errorCode + "--" + errString);
        }

        @Override
        public void onAuthenticationFailed() {
            Log.e(TAG, "failed");
            count--;
            if (count < 0)
                handler.sendEmptyMessage(0x002);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
        }
    }
}
