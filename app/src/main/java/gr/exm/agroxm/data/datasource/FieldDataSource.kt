package gr.exm.agroxm.data.datasource

import arrow.core.Either
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.Aggregation
import gr.exm.agroxm.data.Device
import gr.exm.agroxm.data.Telemetry
import gr.exm.agroxm.data.network.ApiService
import gr.exm.agroxm.data.network.ErrorResponse
import timber.log.Timber

interface FieldDataSource {
    suspend fun getFieldDevice(fieldId: String): Either<Error, Device>
    suspend fun getFieldDeviceData(
        deviceId: String,
        startTs: Long,
        endTs: Long,
        limit: Int,
        keys: String,
        aggregation: Aggregation,
        interval: Long? = null,
    ): Either<Error, Map<String, List<Telemetry>>>
}

class FieldDataSourceImpl(
    private val apiService: ApiService
) : FieldDataSource {

    override suspend fun getFieldDevice(
        fieldId: String
    ): Either<Error, Device> {
        return apiService.getFieldDevices(fieldId).map().map { devices ->
            Timber.d("Got devices: $devices")
            return if (devices.isNotEmpty()) {
                Timber.d("Returning first device: ${devices.first()}")
                Either.Right(devices.first())
            } else {
                Timber.d("Device list is empty.")
                eitherError("No devices found in the field!")
            }
        }
    }

    override suspend fun getFieldDeviceData(
        deviceId: String,
        startTs: Long,
        endTs: Long,
        limit: Int,
        keys: String,
        aggregation: Aggregation,
        interval: Long?
    ): Either<Error, Map<String, List<Telemetry>>> {
        return apiService.deviceData(
            deviceId, startTs, endTs, limit, keys, aggregation.name, interval
        ).map().map {
            Timber.d("Got device data")
            return if (it.isNotEmpty()) {
                Either.Right(it)
            } else {
                eitherError("No telemetry data for the field!")
            }
        }.mapLeft { error ->
            Timber.d(error, "Could not get devices")
            error
        }
    }

    private fun <T : Any> NetworkResponse<T, ErrorResponse>.map(): Either<Error, T> {
        Timber.d("Mapping network response")
        return try {
            when (this) {
                is NetworkResponse.Success -> {
                    Timber.d("Network response: Success")
                    Either.Right(this.body)
                }
                is NetworkResponse.ServerError -> {
                    Timber.d("Network response: ServerError")
                    eitherError(this.body?.message ?: this.error.message, this.error)
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d("Network response: NetworkError")
                    eitherError("Network Error", this.error)
                }
                is NetworkResponse.UnknownError -> {
                    Timber.d("Network response: UnknownError")
                    eitherError("Unknown Error", this.error)
                }
            }
        } catch (exception: Exception) {
            eitherError("Error Mapping Network Response", exception)
        }
    }

    private fun eitherError(message: String?, cause: Throwable? = null): Either.Left<Error> {
        return Either.Left(Error(message, cause))
    }
}