package gr.exm.agroxm.data.io

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class AuthResponse(
    val token: String,
    val refreshToken: String
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: String,
    val path: String?
) : Parcelable
