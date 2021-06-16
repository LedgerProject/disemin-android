package gr.exm.agroxm.data.datasource

import arrow.core.Either
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.Aggregation
import gr.exm.agroxm.data.Device
import gr.exm.agroxm.data.Telemetry
import gr.exm.agroxm.data.network.ApiService
import gr.exm.agroxm.data.network.ErrorResponse

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
        return apiService.getFieldDevices(fieldId).map().map {
            if (it.isNotEmpty()) {
                it.first()
            } else {
                error("No devices found in the field!")
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
            if (it.isNotEmpty()) {
                it
            } else {
                error("No telemetry data for the field!")
            }
        }
    }

    private fun <T : Any> NetworkResponse<T, ErrorResponse>.map(): Either<Error, T> {
        return when (this) {
            is NetworkResponse.Success -> {
                Either.Right(this.body)
            }
            is NetworkResponse.ServerError -> {
                error(this.body?.message ?: this.error.message, this.error)
            }
            is NetworkResponse.NetworkError -> {
                error("Network Error", this.error)
            }
            is NetworkResponse.UnknownError -> {
                error("Unknown Error", this.error)
            }
        }
    }

    private fun error(message: String?, cause: Throwable?): Either.Left<Error> {
        return Either.Left(Error(message, cause))
    }
}