package com.weather.app.network

import com.weather.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object APIClient {

    const val summary: String = "https://api.openweathermap.org/data/2.5/weather/"

    const val detail: String = "https://api.openweathermap.org/data/2.5/forecast/"

    const val appid = "1114a050543fb75d424676612cb91c53"

    private var retrofit: Retrofit? = null

    val client: Retrofit?
        get() {
            val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level =
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                })
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(summary)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            return retrofit

        }
}