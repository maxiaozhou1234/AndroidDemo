package com.zhou.android.model.presenter;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.zhou.android.common.LogInterceptor;
import com.zhou.android.model.view.IOkHttpView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;

/**
 * Created by ZhOu on 2018/4/19.
 */

public class OkHttpPresenter {

    private IOkHttpView iOkHttpView;
    private Handler handler = new Handler();
    private OkHttpClient client;

    public OkHttpPresenter(IOkHttpView iOkHttpView) {
        this.iOkHttpView = iOkHttpView;
        client = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor())
                .build();
    }

    public void load() {
        Request request = new Request.Builder()
                .url("https://passport.csdn.net")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iOkHttpView.showResponse(response);
                    }
                });
            }
        });
    }

    public void getMethod() {

        Request request = new Request.Builder()
                .url("http://www.baidu.com/s?ie=utf-8&wd=csdn")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iOkHttpView.showResponse(response);
                    }
                });
            }
        });
    }

    public void postMethod() {

        FormBody body = new FormBody.Builder()
                .add("ie", "utf-8")
                .add("wd", "csdn")
                .build();
        Request request = new Request.Builder()
                .url("http://www.baidu.com/s")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String text = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iOkHttpView.showResponse(text);
                    }
                });
            }
        });
    }

    public void downloadFile() {
        String path = "https://github.com/Zhouzhouzhou/AndroidDemo/blob/master/app/build.gradle";
        final String fileName = path.substring(path.lastIndexOf("/"), path.length());
        iOkHttpView.updateProgress(0L);
        client.newCall(new Request.Builder().get().url(path).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                iOkHttpView.showToast("下载失败");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        final File file = new File(Environment.getExternalStorageDirectory() + "/file/" + fileName);
                        if (!file.exists()) {
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            file.createNewFile();
                        }
                        FileOutputStream fos = null;
                        InputStream in = null;
                        try {
                            fos = new FileOutputStream(file);
                            in = response.body().byteStream();
                            final long contentLength = response.body().contentLength();
                            long currentLength = 0;
                            byte[] buffer = new byte[1024];
                            if (in != null) {
                                int len;
                                while ((len = in.read(buffer)) != -1) {
                                    fos.write(buffer, 0, len);
                                    currentLength += len;
                                    final long progress = currentLength * 100 / contentLength;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            iOkHttpView.updateProgress(progress);
                                        }
                                    });
                                }
                                fos.flush();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        iOkHttpView.showToast("下载完成");
                                        iOkHttpView.showResponse(file.getAbsolutePath());
                                    }
                                });
                            }
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    iOkHttpView.showToast("下载失败");
                                }
                            });
                            e.printStackTrace();
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (Exception no) {
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (Exception no) {
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    iOkHttpView.updateProgress(100L);
                                }
                            });
                        }
                    }
                });
    }
}
