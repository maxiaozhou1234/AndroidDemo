package com.zhou.android.common;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ZhOu on 2017/10/8.
 */

public class ToastUtils {

    public static void show(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
