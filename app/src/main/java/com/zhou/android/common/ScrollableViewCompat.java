package com.zhou.android.common;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

public class ScrollableViewCompat {

    ScrollableViewCompat() {

    }

    public interface IScrollView {
        boolean viewCanScrollUp();
    }

    public static IScrollView getScrollView(View view) {
        IScrollView iScrollView = null;
        if (view instanceof AbsListView) {
            iScrollView = new AbsListViewCompat((AbsListView) view);
        } else if (view instanceof ScrollView) {
            iScrollView = new ScrollViewCompat((ScrollView) view);
        } else if (view instanceof RecyclerView) {
            iScrollView = new RecyclerViewCompat((RecyclerView) view);
        } else if (view instanceof IScrollView) {
            iScrollView = (IScrollView) view;
        }
        return iScrollView;
    }

    static class AbsListViewCompat implements IScrollView {

        AbsListView view;

        AbsListViewCompat(AbsListView view) {
            this.view = view;
        }

        @Override
        public boolean viewCanScrollUp() {
            if (view == null)
                return false;
            return view.getChildCount() > 0 && (view.getFirstVisiblePosition() > 0 || view.getChildAt(0).getTop() < view.getPaddingTop());
        }
    }

    static class ScrollViewCompat implements IScrollView {

        ScrollView view;

        ScrollViewCompat(ScrollView view) {
            this.view = view;
        }

        @Override
        public boolean viewCanScrollUp() {
            if (view == null)
                return false;
            View child = view.getChildAt(0);
            if (child != null) {
                int childHeight = child.getHeight();
                return view.getHeight() < childHeight + view.getPaddingTop() + view.getPaddingBottom();
            }
            return false;
        }
    }

    static class RecyclerViewCompat implements IScrollView {

        RecyclerView view;

        RecyclerViewCompat(RecyclerView view) {
            this.view = view;
        }

        @Override
        public boolean viewCanScrollUp() {
            if (view == null)
                return false;
            RecyclerView.LayoutManager manager = view.getLayoutManager();
            if (manager == null)
                return false;
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager lm = (LinearLayoutManager) manager;
                if (lm.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    return false;
                }
                return lm.findFirstVisibleItemPosition() > 0 || lm.findViewByPosition(0).getTop() < view.getPaddingTop();
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager g = (StaggeredGridLayoutManager) manager;
                int[] info = g.findFirstVisibleItemPositions(null);
                return info[0] > 0;
            }
            return false;
        }
    }


}
