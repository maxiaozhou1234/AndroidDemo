package com.zhou.android.common;

import android.content.Context;

/**
 * 工具类
 * Created by ZhOu on 2017/3/23.
 */

public class Tools {

    public static int dip2px(Context context, int d) {
        float density = context.getResources().getDisplayMetrics().density;
        int value = (int) (density * d + 0.5F);
        return value;
    }
}
