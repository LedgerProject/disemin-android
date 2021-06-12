package gr.exm.agroxm.data.network

import com.auth0.android.jwt.JWT
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Credentials(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
) {
    fun isValid(): Boolean = username.isNotEmpty() && password.isNotEmpty()
}

@JsonClass(generateAdapter = true)
data class AuthToken(
    @Json(name = "token") val access: String,
    @Json(name = "refreshToken") val refresh: String
) {
    fun isAccessTokenValid(): Boolean = isTokenValid(access)

    fun isRefreshTokenValid(): Boolean = isTokenValid(refresh)

    private fun isTokenValid(token: String): Boolean {
        return try {
            !JWT(token).isExpired(0)
        } catch (e: Exception) {
            false
        }
    }
}

@JsonClass(generateAdapter = true)
data class AuthError(
    @Json(name = "message") val message: String,
    @Json(name = "error") val error: String,
    @Json(name = "path") val path: String,
    @Json(name = "status") val status: Int,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "detail") val message: String,
    @Json(name = "code") val code: String
)

