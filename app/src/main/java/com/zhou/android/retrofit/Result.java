package com.zhou.android.retrofit;

/**
 * Created by ZhOu on 2017/10/8.
 */

public class Result<T> {
    public int code = -1;
    public String msg = "";
    public T result = null;

    public String toString() {
        return "[ code = " + code + " , msg = " + msg + " , result = "
                + (result == null ? "null" : result.toString()) + " ]";
    }
}
