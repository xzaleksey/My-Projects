package com.valyakinaleksey.retrofitexample;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface IApiMethods {

    @GET("/get/curators.xml")
    Call<Curator> getCurators(
            @Query("api_key") String key
    );
}