package com.f1watch.data.api

import com.f1watch.data.model.JolpicaRootResponse
import com.f1watch.data.model.RaceTableResponse
import com.f1watch.data.model.StandingsTableResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface JolpicaF1Api {

    /**
     * Fetch season calendar. Passing "current" automatically resolves to the ongoing season,
     * ensuring the app continues to function seamlessly year after year (2026, 2027, etc.)
     */
    @GET("f1/{season}.json")
    suspend fun getSeasonSchedule(
        @Path("season") season: String = "current"
    ): JolpicaRootResponse<RaceTableResponse>

    @GET("f1/{season}/{round}/results.json")
    suspend fun getRaceResults(
        @Path("season") season: String = "current",
        @Path("round") round: String
    ): JolpicaRootResponse<RaceTableResponse>

    @GET("f1/{season}/driverStandings.json")
    suspend fun getDriverStandings(
        @Path("season") season: String = "current"
    ): JolpicaRootResponse<StandingsTableResponse>

    @GET("f1/{season}/constructorStandings.json")
    suspend fun getConstructorStandings(
        @Path("season") season: String = "current"
    ): JolpicaRootResponse<StandingsTableResponse>
}

object ApiClient {
    private const val BASE_URL = "https://api.jolpica.com/ergast/"

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val f1Api: JolpicaF1Api by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(JolpicaF1Api::class.java)
    }
}
