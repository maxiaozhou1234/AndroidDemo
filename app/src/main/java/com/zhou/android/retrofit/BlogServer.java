package com.zhou.android.retrofit;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Blog 接口
 * Created by ZhOu on 2017/10/8.
 */

public interface BlogServer {

    @GET("query")
    Call<Result<JsonObject>> getBlog(@Query("id") String id);

    @POST("create")
    Call<Result<String>> createBlog(@Body Blog blog);

    @PUT("update")
    Call<Result<String>> updateBlog(@Query("id")String id,@Body Blog blog);

    @DELETE("delete")
    Call<Result<String>> deleteBlog(@Query("id") String id);
}
