package gr.exm.agroxm.util

import android.content.Context

object Measurements {

    class Limits(val min: Float? = null, val max: Float? = null)

    fun getName(context: Context, key: String): String {
        return when (key) {
            "hourly_temperature" -> "Temperature (°C)"
            "hourly_precip_intensity" -> "Precipitation (mm)"
            "hourly_humidity" -> "Humidity (%)"
            "hourly_wind_speed" -> "Wind Speed (km/h)"
            "hourly_wind_gust" -> "Wind Gust (km/h)"
            "hourly_wind_direction" -> "Wind Direction (°)"
            "hourly_pressure" -> "Pressure (hPa)"
            else -> "Unknown"
        }
    }

    // TODO Proper Y axis max & min
    fun getLimits(context: Context, key: String): Limits {
        return when (key) {
            "hourly_temperature" -> Limits()
            "hourly_precip_intensity" -> Limits()
            "hourly_humidity" -> Limits()
            "hourly_wind_speed" -> Limits()
            "hourly_wind_gust" -> Limits()
            "hourly_wind_direction" -> Limits()
            "hourly_pressure" -> Limits()
            else -> Limits()
        }
    }
}