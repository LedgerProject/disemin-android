package gr.exm.agroxm.data.network

import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.*
import gr.exm.agroxm.data.Field
import retrofit2.http.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

interface ApiService {
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

    @GET("/forecast/{deviceId}/data")
    suspend fun forecastData(
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
    suspend fun getFieldDevices(
        @Path("id") fieldId: String,
    ): NetworkResponse<List<Device>, ErrorResponse>

    @GET("/field/{id}/forecasts")
    suspend fun getFieldForecasts(
        @Path("id") fieldId: String,
    ): NetworkResponse<List<Forecast>, ErrorResponse>

    @GET("/devices")
    suspend fun getAvailableDevices(): NetworkResponse<List<Device>, ErrorResponse>

    @GET("/forecasts")
    suspend fun getAvailableForecasts(): NetworkResponse<List<Forecast>, ErrorResponse>
}
