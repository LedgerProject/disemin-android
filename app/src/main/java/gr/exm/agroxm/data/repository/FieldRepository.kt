package gr.exm.agroxm.data.repository

import arrow.core.Either
import arrow.core.flatMap
import gr.exm.agroxm.data.Aggregation
import gr.exm.agroxm.data.CurrentWeather
import gr.exm.agroxm.data.Telemetry
import gr.exm.agroxm.data.datasource.FieldDataSource
import gr.exm.agroxm.util.Weather
import gr.exm.agroxm.util.endOfHour
import gr.exm.agroxm.util.millis
import gr.exm.agroxm.util.startOfHour
import java.time.LocalDateTime

class FieldRepository(
    val dataSource: FieldDataSource
) {
    suspend fun getCurrentWeather(fieldId: String): Either<Error, CurrentWeather> {
        return dataSource.getFieldDevice(fieldId)
            .flatMap { device ->
                dataSource.getFieldDeviceData(
                    deviceId = device.id,
                    startTs = LocalDateTime.now().minusHours(3).startOfHour().millis(),
                    endTs = LocalDateTime.now().endOfHour().millis(),
                    aggregation = Aggregation.NONE,
                    limit = 1,
                    keys = Weather.getHourlyKeys().joinToString(",")
                )
            }.flatMap {
                toCurrentWeather(it)
            }
    }

    private fun toCurrentWeather(data: Map<String, List<Telemetry>>): Either<Error, CurrentWeather> {
        val weather = CurrentWeather(
            ts = LocalDateTime.now().millis(),
            temperature = data[Weather.Hourly.Temperature]?.last()?.asFloat(),
            precipitation = data[Weather.Hourly.Precipitation]?.last()?.asFloat(),
            windSpeed = data[Weather.Hourly.WindSpeed]?.last()?.asFloat(),
            windGust = data[Weather.Hourly.WindGust]?.last()?.asFloat(),
            windDirection = data[Weather.Hourly.WindDirection]?.last()?.asInt(),
            humidity = data[Weather.Hourly.Humidity]?.last()?.asFloat(),
            pressure = data[Weather.Hourly.Pressure]?.last()?.asFloat(),
            icon = data[Weather.Hourly.Icon]?.last()?.asString(),
            cloud = data[Weather.Hourly.Cloud]?.last()?.asFloat(),
            uv = data[Weather.Hourly.UV]?.last()?.asInt(),
        )
        return Either.Right(weather)
    }
}