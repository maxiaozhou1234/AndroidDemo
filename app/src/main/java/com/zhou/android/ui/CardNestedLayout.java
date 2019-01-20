package com.zhou.android.ui;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class CardNestedLayout extends ViewGroup implements NestedScrollingParent {

    public CardNestedLayout(Context context) {
        this(context, null);
    }

    public CardNestedLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardNestedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
