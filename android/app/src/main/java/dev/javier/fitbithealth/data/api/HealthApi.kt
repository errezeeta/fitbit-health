package dev.javier.fitbithealth.data.api

import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HealthApi {
    @GET("health")
    suspend fun health(): Map<String, String>

    @GET("api/v1/dashboard")
    suspend fun dashboard(@Query("day") day: String): DashboardResponse

    @GET("api/v1/sleep")
    suspend fun sleep(
        @Query("start") start: String,
        @Query("end") end: String,
    ): List<SleepSession>

    @GET("api/v1/metrics/{metric}")
    suspend fun metric(
        @Path("metric") metric: String,
        @Query("start") start: String,
        @Query("end") end: String,
    ): List<MetricPoint>

    @POST("api/v1/sync")
    suspend fun sync(): SyncJobResponse

    @GET("api/v1/trends")
    suspend fun trends(
        @Query("start") start: String,
        @Query("end") end: String,
    ): TrendsResponse

    @GET("api/v1/heart-rate")
    suspend fun heartRate(@Query("day") day: String): List<MetricPoint>

    @POST("api/v1/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

class HealthApiFactory {
    fun create(baseUrl: String, token: String): HealthApi {
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))
        require(token.isNotBlank())
        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }.asConverterFactory(MediaType.parse("application/json")!!),
            )
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("Authorization", "Bearer $token")
                            .header("ngrok-skip-browser-warning", "true")
                            .header("User-Agent", "FitbitHealth/1.0")
                            .build()
                        chain.proceed(request)
                    }
                    .build(),
            )
            .build()
            .create(HealthApi::class.java)
    }
}
