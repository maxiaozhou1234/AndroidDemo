package com.zhou.android.item;

/**
 * Created by ZhOu on 2017/2/8.
 */

public class GridViewItem {
    public String targetClass;
    public String zhName;
    public Class clz;

    public GridViewItem(String targetClass, String zhName) {
        this.targetClass = targetClass;
        this.zhName = zhName;
        try {
            this.clz = Class.forName(targetClass);
        } catch (Exception e) {
            this.clz = null;
        }
    }

    public GridViewItem(Class clz, String zhName) {
        this.clz = clz;
        this.targetClass = clz.getName();
        this.zhName = zhName;
    }

}
