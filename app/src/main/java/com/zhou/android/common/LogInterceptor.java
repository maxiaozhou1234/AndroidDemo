package com.zhou.android.common;

import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ZhOu on 2018/7/23.
 */

public class LogInterceptor implements Interceptor {
    private static final String TAG = "LogInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        String method = request.method();
        Log.d(TAG, "\r\n");
        Log.d(TAG, "----------START----------");
        Log.d(TAG, "| url: " + request.url().toString());
        String content = response.body().string();
        if ("POST".equals(method)) {
            RequestBody body = request.body();
            if (body instanceof FormBody) {
                StringBuilder builder = new StringBuilder();
                FormBody formBody = (FormBody) body;
                for (int i = 0; i < formBody.size(); i++) {
                    builder.append(formBody.encodedName(i) + "=" + formBody.encodedValue(i) + ",");
                }
                builder.delete(builder.length() - 1, builder.length());
                Log.d(TAG, "| params: {" + builder.toString() + "}");
            }
        }
        Log.d(TAG, "-----------END-----------");
        return response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), content))
                .build();
    }
}
