package gr.exm.agroxm.data

import android.os.Parcelable
import com.auth0.android.jwt.JWT
import com.github.mikephil.charting.data.Entry
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Credentials(
    val username: String?,
    val password: String?,
    val role: Role? = null,
) : Parcelable {
    fun isValid(): Boolean = !username.isNullOrEmpty() && !password.isNullOrEmpty()
}

@JsonClass(generateAdapter = true)
@Parcelize
data class AuthToken(
    val token: String,
    val refreshToken: String,
) : Parcelable {
    fun isTokenValid(): Boolean =
        token.isNotEmpty() && !JWT(token).isExpired(60)

    fun isRefreshTokenValid(): Boolean =
        refreshToken.isNotEmpty() && !JWT(refreshToken).isExpired(60)
}

@Parcelize
enum class Role(
    val type: String,
) : Parcelable {
    FARMER("FARMER"),
    AGRONOMIST("AGRONOMIST")
}

@JsonClass(generateAdapter = true)
@Parcelize
data class User(
    val username: String,
    val role: Role,
    val firstName: String?,
    val lastName: String?,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Location(
    val latitude: Double,
    val longitude: Double,
) : Parcelable {
    companion object {
        fun empty() = Location(0.0, 0.0)
    }

    fun isEmpty() = this.latitude == 0.0 && this.longitude == 0.0
}

@JsonClass(generateAdapter = true)
@Parcelize
data class Field(
    val id: String,
    val name: String,
    val perimeter: String?,
    val location: Location,
    val currentCrop: Crop?, // Crop ts latest value
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Device(
    val id: String,
    val name: String,
    val type: String,
    val location: Location,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Forecast(
    val id: String,
    val name: String,
    val location: Location,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Crop(
    val name: String,
    val description: String,
    val timestamp: Long,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Log(
    val name: String,
    val description: String,
    val timestamp: Long,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Telemetry(
    val ts: Long,
    val value: Float?,
) : Parcelable {
    fun entry(): Entry = Entry(
        ts.toFloat(),
        value!!.toFloat()
    )
}

@Parcelize
enum class Aggregation : Parcelable {
    MIN, MAX, AVG, SUM, COUNT, NONE
}

@JsonClass(generateAdapter = true)
@Parcelize
data class TimeWindow(
    val startTs: Long,
    val endTs: Long,
    val aggregation: Aggregation = Aggregation.NONE,
    val interval: Long = 0L,
) : Parcelable
