package com.example.tinkoff.retrofit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Path


interface PostApi {

    @GET("random?json=true")
   suspend fun getGifs(): Response<Post>

    @GET("latest/{numb}?json=true")
    suspend fun getLatest(@Path("numb") numb:Int):Response<Result>

    @GET("top/{numb}?json=true")
    suspend fun getTops(@Path("numb") numb:Int):Response<Result>


}