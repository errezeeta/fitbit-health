package dev.javier.fitbithealth.data.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.BufferedReader

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
    private val json = Json { ignoreUnknownKeys = true }

    fun create(baseUrl: String, token: String): HealthApi {
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))
        require(token.isNotBlank())
        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaTypeOrNull()!!))
            .client(httpClient(token))
            .build()
            .create(HealthApi::class.java)
    }

    private fun httpClient(token: String): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "FitbitHealth/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    /**
     * Chat con streaming (SSE): lee el endpoint /api/v1/chat/stream
     * y devuelve el texto completo acumulado. Los tokens llegan como
     * `data: {"token": "..."}` hasta `data: [DONE]`.
     * Si se pasa `onToken`, se invoca con cada chunk en tiempo real.
     */
    suspend fun chatStream(
        baseUrl: String,
        token: String,
        request: ChatRequest,
        onToken: ((String) -> Unit)? = null,
    ): String = withContextIO {
        val url = baseUrl.trimEnd('/') + "/api/v1/chat/stream"
        val body = json.encodeToString(ChatRequest.serializer(), request)
            .toRequestBody("application/json".toMediaTypeOrNull())
        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer $token")
            .header("ngrok-skip-browser-warning", "true")
            .header("Accept", "text/event-stream")
            .build()

        val sb = StringBuilder()
        httpClient(token).newCall(httpRequest).execute().use { response ->
            val httpCode = response.code
            if (httpCode !in 200..299) {
                error("Chat HTTP $httpCode")
            }
            val body = response.body
            val reader: BufferedReader = body?.charStream()?.buffered() ?: error("sin body")
            while (true) {
                val line = reader.readLine() ?: break
                if (line.startsWith("data:")) {
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    runCatching {
                        val obj = json.parseToJsonElement(data).jsonObject
                        val piece = obj["token"]?.jsonPrimitive?.content ?: ""
                        if (piece.isNotEmpty()) {
                            sb.append(piece)
                            onToken?.invoke(piece)
                        }
                    }
                }
            }
        }
        sb.toString().ifBlank { error("Respuesta vacía del chat") }
    }

    private suspend fun <T> withContextIO(block: () -> T): T =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { block() }
}
