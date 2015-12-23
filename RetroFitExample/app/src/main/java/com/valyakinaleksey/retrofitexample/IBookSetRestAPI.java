package com.valyakinaleksey.retrofitexample;

import retrofit.http.GET;
import retrofit.http.Path;

public interface IBookSetRestAPI {

    @GET("/v1/books/{book_id}")
    Book getBook(@Path("book_id") int book_id);

}