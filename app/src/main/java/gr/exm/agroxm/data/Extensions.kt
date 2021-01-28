package gr.exm.agroxm.data

import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.io.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

fun Request.path(): String = this.url.encodedPath

fun Response.path(): String = this.request.path()

fun Request.authHeader(): String? = this.header(AuthHelper.AUTH_HEADER)

fun Request.hasAuthHeader(): Boolean = this.authHeader() != null

fun Request.signedRequest(): Request {
    // Get stored auth token
    val authToken = AuthHelper.getAuthToken()

    // Check validity
    if (!authToken.isTokenValid()) {
        // Log, but proceed
        Timber.w("[${this.path()}] Invalid token.")
    }

    // Return a new request, adding auth header
    return this.newBuilder()
        .header(AuthHelper.AUTH_HEADER, "Bearer ${authToken.token}")
        .build()
}

fun Request.authenticateWithCredentials(): Request? {
    Timber.d("Authenticating with credentials")

    val credentials = AuthHelper.getCredentials()

    if (!credentials.isValid()) {
        Timber.d("Invalid credentials %s", credentials)
        return null
    }

    // Try to re-auth with credentials
    val response = runBlocking {
        Timber.d("Authenticating in the background using credentials")
        ApiService.get().login(credentials)
    }

    when (response) {
        is NetworkResponse.Success -> {
            // Get token from response body
            val authToken = response.body

            // Save token
            Timber.d("Got token through login. Saving $authToken")
            AuthHelper.setAuthToken(authToken)

            // Proceed with original request, adding token
            return this.signedRequest()
        }
        else -> {
            Timber.d("Authentication in background failed")
            return null
        }
    }
}

fun Request.authenticateWithAccessToken(): Request? {
    Timber.d("${this.path()} Refreshing token")

    // Get stored auth token
    val authToken = AuthHelper.getAuthToken()

    // If token is valid, add it in auth header
    if (authToken.isTokenValid()) {
        return this.newBuilder()
            .header(AuthHelper.AUTH_HEADER, "Bearer ${authToken.token}")
            .build()
    }

    // If token is invalid, but refresh token is still valid, refresh the token
    else if (authToken.isRefreshTokenValid()) {
        Timber.w("Invalid token %s", authToken)

        // Refresh access token
        val response = runBlocking {
            Timber.d("Refreshing token in the background")
            ApiService.get().refresh(
                AuthToken(
                    token = null,
                    refreshToken = authToken.refreshToken
                )
            )
        }

        when (response) {
            is NetworkResponse.Success -> {
                // Get token from response body
                val newAuthToken = response.body

                // Save token
                Timber.d("Got token through refresh. Saving $authToken")
                AuthHelper.setAuthToken(newAuthToken)

                // Proceed with original request, adding token
                return this.signedRequest()
            }
            else -> {
                Timber.d("Token refresh failed")
            }
        }
    }

    // We cannot use access token. Try to use credentials.
    Timber.d("Cannot authenticate with access token.")
    return this.authenticateWithCredentials()
}