package com.zhou.android.retrofit;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by ZhOu on 2018/4/25.
 */

public class CustomResponseConverter implements Converter<ResponseBody, String> {

    @Override
    public String convert(ResponseBody value) throws IOException {
        return value.toString();
    }
}
