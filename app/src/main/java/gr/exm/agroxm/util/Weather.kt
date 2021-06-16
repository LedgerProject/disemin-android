package gr.exm.agroxm.util

import androidx.annotation.RawRes
import androidx.annotation.StringDef
import gr.exm.agroxm.R
import kotlin.math.floor

object Weather {

    private const val EMPTY_VALUE = "-"

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        value = [
            Hourly.Temperature,
            Hourly.Precipitation,
            Hourly.Humidity,
            Hourly.WindSpeed,
            Hourly.WindGust,
            Hourly.WindDirection,
            Hourly.Pressure
        ]
    )
    annotation class Hourly {
        companion object {
            const val Temperature = "hourly_temperature"
            const val Precipitation = "hourly_precip_intensity"
            const val Humidity = "hourly_humidity"
            const val WindSpeed = "hourly_wind_speed"
            const val WindGust = "hourly_wind_gust"
            const val WindDirection = "hourly_wind_direction"
            const val Pressure = "hourly_pressure"
            const val Cloud = "hourly_cloud_cover"
            const val UV = "hourly_uv_index"
            const val Icon = "hourly_icon"
        }
    }

    fun getHourlyKeys(): Array<String> {
        return arrayOf(
            Hourly.Temperature,
            Hourly.Precipitation,
            Hourly.Humidity,
            Hourly.WindSpeed,
            Hourly.WindGust,
            Hourly.WindDirection,
            Hourly.Pressure,
            Hourly.Cloud,
            Hourly.UV,
            Hourly.Icon
        )
    }

    @RawRes
    fun getWeatherAnimation(icon: String?): Int {
        return when (icon) {
            "clear-day" -> R.raw.anim_weather_clear_day
            "clear-night" -> R.raw.anim_weather_clear_night
            "rain" -> R.raw.anim_weather_rain
            "snow" -> R.raw.anim_weather_snow
            "sleet" -> R.raw.anim_weather_snow
            "wind" -> R.raw.anim_weather_wind
            "fog" -> R.raw.anim_weather_fog
            "cloudy" -> R.raw.anim_weather_cloudy
            "partly-cloudy-day" -> R.raw.anim_weather_partly_cloudy_day
            "partly-cloudy-night" -> R.raw.anim_weather_partly_cloudy_night
            else -> 0
        }
    }

    fun getFormattedTemperature(value: Float?) = getFormattedValueOrEmpty(value, "°C", 0)

    fun getFormattedPrecipitation(value: Float?) = getFormattedValueOrEmpty(value, "mm")

    fun getFormattedHumidity(value: Float?) = getFormattedValueOrEmpty(value, "%")

    fun getFormattedCloud(value: Float?) = getFormattedValueOrEmpty(value, "%")

    fun getFormattedWindSpeed(value: Float?) = getFormattedValueOrEmpty(value, "km/h")

    fun getFormattedWindDirection(value: Int): String {
        val normalized = floor((value / 22.5) + 0.5).toInt()
        val cardinal = listOf(
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
        )
        return cardinal[normalized.mod(16)]
    }

    fun getFormattedWind(windSpeed: Float?, windDirection: Int?): String {
        return if (windSpeed != null && windDirection != null) {
            "${getFormattedWindSpeed(windSpeed)} ${getFormattedWindDirection(windDirection)}"
        } else EMPTY_VALUE
    }

    private fun getFormattedValueOrEmpty(
        value: Number?,
        units: String,
        decimals: Int? = null
    ): String {
        if (value == null) {
            return EMPTY_VALUE
        }
        if (decimals == null) {
            return "$value $units"
        }
        return "%.${decimals}f $units".format(value)
    }
}