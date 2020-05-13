package com.weather.app.network

import com.weather.app.entity.detail.WeatherDetail
import com.weather.app.entity.summary.WeatherSummary
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {

    @GET(APIClient.detail)
    fun getDetail(
        @Query("q") city: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Observable<WeatherDetail>

    @GET(APIClient.summary)
    fun getSummary(
        @Query("q") city: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Observable<WeatherSummary>

}