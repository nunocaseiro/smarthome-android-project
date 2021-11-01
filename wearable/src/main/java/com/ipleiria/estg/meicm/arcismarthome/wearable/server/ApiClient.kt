package com.ipleiria.estg.meicm.arcismarthome.wearable.server

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit: Retrofit
        get() {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClient = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://161.35.8.148/")
                .client(okHttpClient)
                .build()
        }

    val service: UserService
        get() = retrofit.create(UserService::class.java)
}