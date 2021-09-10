package com.zhou.android.main;

import android.widget.TextView;
import android.widget.Toast;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.Tools;
import com.zhou.ipay.IPay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

/**
 * 测试动态加载 dex
 * Created by mxz on 2021/9/10.
 */
public class DexClassLoaderActivity extends BaseActivity {

    private TextView text;

    private String jarSourcePath;

    private DexClassLoader dexLoader = null;

    private volatile boolean copyFlag = false;

    private String childApkName = "child.apk";

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_dex_class_loader);
    }

    @Override
    protected void init() {
        text = findViewById(R.id.text);

        jarSourcePath = getFilesDir() + "/" + childApkName;

        checkSourceFileExits(jarSourcePath);

        findViewById(R.id.btnLoad).setOnClickListener(v -> {
            if (copyFlag) {
                if (dexLoader == null) {
                    String dexSourcePath = getFilesDir() + "/plugin/";
                    File outPut = new File(dexSourcePath);
                    if (!outPut.exists()) {
                        outPut.mkdirs();
                    }
                    try {
                        dexLoader = new DexClassLoader(jarSourcePath, dexSourcePath, null, getClassLoader());
                        showLog("load apk success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showLog(e.getClass().getName() + ": " + e.getMessage());
                        showLog("loaded error: " + e.getMessage());
                    }
                } else {
                    showLog("dex already loaded!");
                }
            } else {
                Toast.makeText(this, "jar 文件找不到", Toast.LENGTH_SHORT).show();
                showLog("source apk not found error");
            }
        });

        findViewById(R.id.btnRead).setOnClickListener(v -> {

            try {

                Class pay;
                if (dexLoader == null) {
                    pay = Class.forName("at.zhou.child.PayImpl");
                } else {
                    pay = dexLoader.loadClass("at.zhou.child.PayImpl");
                }

                IPay iPay = (IPay) pay.newInstance();
                String payMoney = "pay money: " + iPay.getMoney();
                iPay.pay(100);

                showLog(payMoney);
                showLog("Test success!!");

            } catch (Exception e) {
                showLog(e.getClass().getName() + ": " + e.getMessage());
            }
        });
    }

    @Override
    protected void addListener() {

    }

    private void checkSourceFileExits(final String path) {
        final File source = new File(path);
        if (!source.exists()) {
            new Thread(() -> {
                FileOutputStream fos = null;
                InputStream inputStream = null;
                try {
                    fos = new FileOutputStream(path);
                    inputStream = getAssets().open(source.getName());
                    int len;
                    byte[] buf = new byte[1024 * 8];
                    while ((len = inputStream.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    copyFlag = true;
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            showLog("copy success");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    source.delete();
                } finally {
                    Tools.closeIO(fos);
                }
            }).start();
        } else {
            copyFlag = true;
            showLog("child.apk exit");
        }
    }

    private void showLog(String msg) {
        text.append(msg);
        text.append("\n");
    }
}
