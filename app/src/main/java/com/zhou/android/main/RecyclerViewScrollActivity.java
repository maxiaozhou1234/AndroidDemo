package com.zhou.android.main;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.zhou.android.R;
import com.zhou.android.adapter.SimpleRecyclerAdapter;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;
import com.zhou.android.common.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RecyclerView 滑动示例
 * Created by ZhOu on 2018/2/16.
 */

public class RecyclerViewScrollActivity extends BaseActivity implements View.OnClickListener {

    private ViewFlipper viewFlipper;
    private EditText et_position;
    private RadioGroup rg_tab;
    private RecyclerView recyclerView;
    private List<String> data = new ArrayList<>();
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;

    private InputMethodManager imm;

    private boolean isMove = false;
    private int scrollPosition = 0;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_recycler_scroll);
    }

    @Override
    protected void init() {
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        et_position = (EditText) findViewById(R.id.et_position);
        rg_tab = (RadioGroup) findViewById(R.id.rg_tab);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position % 5 == 0 ? 4 : 1;
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(linearLayoutManager);
        SimpleRecyclerAdapter simpleRecyclerAdapter = new SimpleRecyclerAdapter(this, data);
        recyclerView.setAdapter(simpleRecyclerAdapter);
        recyclerView.setOnScrollListener(onScrollListener);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        for (int i = 0; i < 100; i++) {
            data.add("Simple item " + i);
        }
        simpleRecyclerAdapter.notifyDataSetChanged();

        initTab();
    }

    @Override
    protected void addListener() {
        et_position.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                    return move();
                else
                    return false;
            }
        });

        findViewById(R.id.btn_search).setOnClickListener(this);
    }

    private void initTab() {
        int padding = Tools.dip2px(this, 10);
        for (int i = 0; i < 20; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setPadding(padding, 0, padding, 0);
            rb.setButtonDrawable(null);
            rb.setGravity(Gravity.CENTER);
            rb.setTag(i * 5);
            rb.setText("Group " + i);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            try {
                rb.setTextColor(getResources().getColorStateList(R.color.bg_tab_text));
            } catch (Exception e) {
                e.printStackTrace();
            }
            rb.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.drawable.bg_block_tab));
            rb.setOnCheckedChangeListener(onCheckedChangeListener);
            rg_tab.addView(rb);
        }
        ((RadioButton) rg_tab.getChildAt(0)).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recycler, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_list) {
            viewFlipper.setDisplayedChild(0);
            recyclerView.setLayoutManager(linearLayoutManager);
            return true;
        } else if (item.getItemId() == R.id.menu_grid) {
            viewFlipper.setDisplayedChild(1);
            recyclerView.setLayoutManager(gridLayoutManager);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.btn_search == id) {
            move();
        }
    }

    private boolean move() {
        int position;
        String key = et_position.getText().toString();
        if (TextUtils.isEmpty(key)) {
            ToastUtils.show(RecyclerViewScrollActivity.this, et_position.getHint());
            return false;
        } else {
            try {
                position = Integer.parseInt(key);
            } catch (Exception e) {
                e.printStackTrace();
                position = -1;
            }
        }
        if (position != -1) {
            hideSoftInput();
            et_position.clearFocus();
            moveToPosition(position);
        }
        return true;
    }

    private void hideSoftInput() {
        imm.hideSoftInputFromWindow(et_position.getWindowToken(), 0);
    }

    private void moveToPosition(int position) {
        if (position > data.size())
            return;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int end = layoutManager.findLastVisibleItemPosition();
        if (first == -1 || end == -1)
            return;
        if (position <= first) {
            layoutManager.scrollToPosition(position);
        } else if (position >= end) {
            isMove = true;
            scrollPosition = position;
            layoutManager.smoothScrollToPosition(recyclerView, null, position);
        } else {//中间部分
            int n = position - layoutManager.findFirstVisibleItemPosition();
            if (n > 0 && n < data.size()) {
                int top = layoutManager.findViewByPosition(position).getTop();
                recyclerView.scrollBy(0, top);
            }
        }
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (isMove && newState == RecyclerView.SCROLL_STATE_IDLE) {
                isMove = false;
                int top = layoutManager.findViewByPosition(scrollPosition).getTop();
                recyclerView.scrollBy(0, top);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int position = (int) buttonView.getTag();
            Log.d("zhou", "位置：" + position);
            if (isChecked)
                moveToPosition(position);
        }
    };
}
