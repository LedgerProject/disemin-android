package gr.exm.agroxm.util

import android.content.Context
import android.content.SharedPreferences
import gr.exm.agroxm.data.AuthToken
import gr.exm.agroxm.data.Credentials
import okhttp3.Request
import timber.log.Timber

object AuthHelper {

    const val AUTH_HEADER = "X-Authorization"
    const val NO_AUTH_HEADER = "No-Authorization"

    private const val NAME = "auth"
    private const val MODE = Context.MODE_PRIVATE

    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_TOKEN = "token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    fun isLoggedIn(): Boolean {
        if (!getCredentials().isValid()) {
            Timber.d("Invalid credentials")
            return false
        } else if (!getAuthToken().isTokenValid() && getAuthToken().isRefreshTokenValid()) {
            Timber.d("Invalid token, but refresh token still valid")
            //return false
        } else if (!getAuthToken().isRefreshTokenValid()) {
            Timber.d("Invalid refresh token")
            //return false
        }
        return true
    }

    fun logOut() {
        preferences.edit().clear().apply()
    }

    fun getAuthToken(): AuthToken {
        return AuthToken(
            token = preferences.getString(KEY_TOKEN, null),
            refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)
        )
    }

    fun setAuthToken(authToken: AuthToken) {
        preferences.edit()
            .putString(KEY_TOKEN, authToken.token)
            .putString(KEY_REFRESH_TOKEN, authToken.token)
            .apply()
    }

    fun getCredentials(): Credentials {
        return Credentials(
            username = preferences.getString(KEY_USERNAME, null),
            password = preferences.getString(KEY_PASSWORD, null)
        )
    }

    fun getUsername(): String? {
        return preferences.getString(KEY_USERNAME, null)
    }

    fun setUsername(username: String) {
        preferences.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getPassword(): String? {
        return preferences.getString(KEY_PASSWORD, null)
    }

    fun setPassword(password: String) {
        preferences.edit()
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun setCredentials(credentials: Credentials) {
        preferences.edit()
            .putString(KEY_USERNAME, credentials.username)
            .putString(KEY_PASSWORD, credentials.password)
            .apply()
    }

    fun Request.hasAuthHeader(): Boolean {
        return this.header(AUTH_HEADER) != null
    }

    fun Request.signedRequest(): Request {
        // Get stored auth token
        val authToken = getAuthToken()

        // Check validity
        if (!authToken.isTokenValid()) {
            // Log, but proceed
            Timber.w("Invalid token %s", authToken)
        }

        // Return a new request, adding auth header
        return this.newBuilder()
            .header(AUTH_HEADER, "Bearer ${authToken.token}")
            .build()
    }
}
