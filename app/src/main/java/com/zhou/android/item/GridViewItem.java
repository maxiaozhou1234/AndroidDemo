package com.zhou.android.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by ZhOu on 2017/2/8.
 */

public class GridViewItem implements Parcelable {
    public String targetClass;
    public String zhName;

    public GridViewItem(@NonNull String targetClass, @NonNull String zhName) {
        this.targetClass = targetClass;
        this.zhName = zhName;
    }

    protected GridViewItem(Parcel in) {
        targetClass = in.readString();
        zhName = in.readString();
    }

    public static final Creator<GridViewItem> CREATOR = new Creator<GridViewItem>() {
        @Override
        public GridViewItem createFromParcel(Parcel in) {
            return new GridViewItem(in);
        }

        @Override
        public GridViewItem[] newArray(int size) {
            return new GridViewItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(targetClass);
        dest.writeString(zhName);
    }
}
