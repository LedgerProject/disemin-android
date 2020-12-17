package gr.exm.agroxm.data.io

import android.content.Context
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.squareup.moshi.Moshi
import gr.exm.agroxm.BuildConfig
import gr.exm.agroxm.data.*
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.util.AuthHelper.NO_AUTH_HEADER
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("/auth/login")
    @Headers("$NO_AUTH_HEADER: true")
    suspend fun login(
        @Body credentials: Credentials,
    ): NetworkResponse<AuthToken, ErrorResponse>

    @POST("/auth/register")
    @Headers("$NO_AUTH_HEADER: true")
    suspend fun register(
        @Body registration: RegistrationBody,
    ): NetworkResponse<AuthToken, ErrorResponse>

    @POST("/auth/refresh")
    @Headers("$NO_AUTH_HEADER: true")
    suspend fun refresh(
        @Body authToken: AuthToken,
    ): NetworkResponse<AuthToken, ErrorResponse>

    @GET("/auth/user")
    suspend fun user(): NetworkResponse<User, ErrorResponse>

    @GET("/fields")
    suspend fun fields(): NetworkResponse<List<Field>, ErrorResponse>

    @GET("/device/{deviceId}/data")
    suspend fun deviceData(
        @Path("deviceId") deviceId: String,
        @Query("startTs") startTs: Long = LocalDateTime.now()
            .minusDays(1)
            .minusHours(1)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .toEpochSecond(ZoneOffset.UTC),
        @Query("endTs") endTs: Long = LocalDateTime.now()
            .plusHours(1)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .toEpochSecond(ZoneOffset.UTC),
        @Query("limit") limit: Int = 50000,
        @Query("keys") keys: String = "hourly_temperature,hourly_precip_intensity,hourly_humidity,hourly_wind_speed,hourly_wind_gust,hourly_wind_direction,hourly_pressure",
        @Query("agg") aggregation: String = "NONE",
        @Query("interval") interval: Long = TimeUnit.HOURS.toMillis(1),
    ): NetworkResponse<Map<String, List<Telemetry>>, ErrorResponse>

    @POST("/field")
    suspend fun createField(
        @Body field: Field,
    ): NetworkResponse<Field, ErrorResponse>

    @PUT("/field")
    suspend fun updateField(
        @Body field: Field,
    ): NetworkResponse<Field, ErrorResponse>

    @GET("/field/{id}")
    suspend fun getField(
        @Path("id") fieldId: String,
    ): NetworkResponse<Field, ErrorResponse>

    @DELETE("/field/{id}")
    suspend fun deleteField(
        @Path("id") fieldId: String,
    ): NetworkResponse<Field, ErrorResponse>

    @GET("/field/{id}/crop")
    suspend fun getCrops(
        @Path("id") fieldId: String,
    ): NetworkResponse<Crop, ErrorResponse>

    @POST("/field/{id}/crop")
    suspend fun addCrop(
        @Path("id") fieldId: String,
        @Body crop: Crop,
    ): NetworkResponse<Crop, ErrorResponse>

    @GET("/field/{id}/log")
    suspend fun getLog(
        @Path("id") fieldId: String,
    ): NetworkResponse<Log, ErrorResponse>

    @POST("/field/{id}/log")
    suspend fun addLog(
        @Path("id") fieldId: String,
        @Body crop: Crop,
    ): NetworkResponse<Log, ErrorResponse>

    @GET("/field/{id}/devices")
    suspend fun getLogFieldDevices(
        @Path("id") fieldId: String,
    ): NetworkResponse<List<Device>, ErrorResponse>

    @GET("/devices")
    suspend fun getAvailableDevices(): NetworkResponse<List<Device>, ErrorResponse>

    @GET("/forecasts")
    suspend fun getAvailableForecasts(): NetworkResponse<List<Forecast>, ErrorResponse>

    companion object {

        private const val CACHE_SIZE = 50L * 1024L * 1024L // 50MB

        private var instance: ApiService? = null

        fun init(context: Context) {
            if (instance != null) {
                throw Exception("ApiService.init() has already been called.")
            }
            instance = create(context)
        }

        @Synchronized
        fun get(): ApiService {
            if (instance == null) {
                throw NullPointerException("Instance is null. Have you called ApiService.init()?")
            }
            return instance as ApiService
        }

        private fun create(context: Context): ApiService {
            Timber.d("Creating ApiService instance.")

            // Install HTTP cache
            val cache = Cache(context.cacheDir, CACHE_SIZE)

            // Add http logging
            val logger = HttpLoggingInterceptor()
                .setLevel(if (BuildConfig.DEBUG) BODY else NONE)

            // Create client
            val client: OkHttpClient = OkHttpClient.Builder()
                .authenticator(AuthTokenAuthenticator())
                .addInterceptor(AuthTokenInterceptor())
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .cache(cache)
                .build()

            // Create JSON serializer
            val moshi = MoshiConverterFactory.create(Moshi.Builder().build()).asLenient()

            // Create retrofit instance
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(moshi)
                .addCallAdapterFactory(NetworkResponseAdapterFactory())
                .client(client)
                .build()

            // TODO Add error body converter

            // Create service
            return retrofit.create(ApiService::class.java)
        }
    }
}