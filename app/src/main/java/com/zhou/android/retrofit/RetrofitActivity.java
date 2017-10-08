package com.zhou.android.retrofit;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit
 * Created by ZhOu on 2017/10/8.
 */

public class RetrofitActivity extends BaseActivity implements View.OnClickListener {

    private EditText et_query, et_id, et_title, et_content, et_delete;
    private Button btn_query, btn_new, btn_update, btn_delete;
    private TextView tv;
    private ScrollView scrollView;

    private Retrofit retrofit;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_retrofit);
    }

    @Override
    protected void init() {
        et_query = (EditText) findViewById(R.id.et_query);
        et_id = (EditText) findViewById(R.id.et_id);
        et_title = (EditText) findViewById(R.id.et_title);
        et_content = (EditText) findViewById(R.id.et_content);
        et_delete = (EditText) findViewById(R.id.et_delete);

        btn_query = (Button) findViewById(R.id.btn_query);
        btn_new = (Button) findViewById(R.id.btn_new);
        btn_update = (Button) findViewById(R.id.btn_update);
        btn_delete = (Button) findViewById(R.id.btn_delete);

        tv = (TextView) findViewById(R.id.tv);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                //打印retrofit日志
                Log.i("RetrofitLog", "retrofitBack = " + message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("http://192.168.1.104:4567/")
                .client(client)
                .build();
    }

    @Override
    protected void addListener() {
        btn_query.setOnClickListener(this);
        btn_new.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.btn_query == id) {
            String blogId = et_query.getText().toString();
            if (TextUtils.isEmpty(blogId)) {
                ToastUtils.show(this, et_query.getHint().toString());
                return;
            }
            //get
            BlogServer blogServer = retrofit.create(BlogServer.class);
            Call<Result<JsonObject>> call = blogServer.getBlog(blogId);
            call.enqueue(new Callback<Result<JsonObject>>() {

                @Override
                public void onResponse(Call<Result<JsonObject>> call, Response<Result<JsonObject>> response) {
                    tv.setText(response.body().toString());
                }

                @Override
                public void onFailure(Call<Result<JsonObject>> call, Throwable t) {
                    tv.setText(t.toString());
                }
            });

        } else if (R.id.btn_new == id) {
            String title = et_title.getText().toString();
            String content = et_content.getText().toString();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                ToastUtils.show(this, "Input title or content please.");
                return;
            }
            Blog blog = new Blog();
            blog.title = title;
            blog.content = content;
            //post
            BlogServer blogServer = retrofit.create(BlogServer.class);
            Call<Result<String>> call = blogServer.createBlog(blog);
            call.enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    tv.setText(response.body().toString());
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    tv.setText(t.toString());
                }
            });

        } else if (R.id.btn_update == id) {
            String blogId = et_id.getText().toString();
            String title = et_title.getText().toString();
            String content = et_content.getText().toString();
            //put
            if (TextUtils.isEmpty(blogId)) {
                ToastUtils.show(this, et_id.getHint().toString());
                return;
            }
            int newId;
            try {
                newId = Integer.parseInt(blogId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                newId = -1;
            }
            if (-1 == newId) {
                ToastUtils.show(this, "id error.");
                return;
            }
            Blog blog = new Blog();
            blog.id = newId;
            blog.title = title;
            blog.content = content;
            BlogServer blogServer = retrofit.create(BlogServer.class);
            Call<Result<String>> call = blogServer.updateBlog(blogId,blog);
            call.enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    tv.setText(response.body().toString());
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    tv.setText(t.toString());
                }
            });

        } else if (R.id.btn_delete == id) {
            String blogId = et_delete.getText().toString();
            if (TextUtils.isEmpty(blogId)) {
                ToastUtils.show(this, et_delete.getHint().toString());
                return;
            }
            //delete
            BlogServer blogServer = retrofit.create(BlogServer.class);
            Call<Result<String>> call = blogServer.deleteBlog(blogId);
            call.enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                    tv.setText(response.body().toString());
                }

                @Override
                public void onFailure(Call<Result<String>> call, Throwable t) {
                    tv.setText(t.toString());
                }
            });
        }
    }
}
