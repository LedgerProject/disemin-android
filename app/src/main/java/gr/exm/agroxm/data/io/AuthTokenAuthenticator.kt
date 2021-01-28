package gr.exm.agroxm.data.io

import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.*
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class AuthTokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // The original request
        val request = response.request

        Timber.d("[${request.path()}] Status: ${response.code}. Invoking authenticator.")

        // Skip authentication if this is an explicit call to /auth endpoints
        if (request.header(AuthHelper.NO_AUTH_HEADER).equals("true")) {
            Timber.d("[${request.path()}] Skipping authentication for path.")
            return null
        }

        // We've already added auth header, but are still unauthorized
        if (request.hasAuthHeader()) {
            Timber.d("[${request.path()}] Authorization header exists [${request.authHeader()}]. Will try credentials.")
            // Try to re-login with saved credentials.
            return authenticateWithCredentials(request)
        }

        // Add auth header using the auth token with Bearer schema
        return authenticateWithAccessToken(request)
    }

    private fun authenticateWithAccessToken(request: Request): Request? {
        Timber.d("[${request.path()}] Refreshing token")

        // Get stored auth token
        val authToken = AuthHelper.getAuthToken()

        // If token is valid, add it in auth header
        if (authToken.isTokenValid()) {
            Timber.d("[${request.path()}] Token is valid. Adding to request.")
            return request.newBuilder()
                .header(AuthHelper.AUTH_HEADER, "Bearer ${authToken.token}")
                .build()
        }

        // If token is invalid, but refresh token is still valid, refresh the token
        else if (authToken.isRefreshTokenValid()) {
            Timber.w("[${request.path()}] Invalid token. Trying to refresh.")

            // Refresh access token
            val response = runBlocking {
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
                    Timber.d("[${request.path()}] Token refresh success. Saving $authToken")
                    AuthHelper.setAuthToken(newAuthToken)

                    // Proceed with original request, adding token
                    return request.signedRequest()
                }
                else -> {
                    Timber.d("[${request.path()}] Token refresh failed")
                }
            }
        }

        // We cannot use access token. Try to use credentials.
        Timber.d("[${request.path()}] Cannot authenticate with access token. Trying credentials.")
        return authenticateWithCredentials(request)
    }

    private fun authenticateWithCredentials(request: Request): Request? {
        val credentials = AuthHelper.getCredentials()

        if (!credentials.isValid()) {
            Timber.d("[${request.path()}] Invalid credentials. Cannot re-authenticate.")
            return null
        }

        // Try to re-auth with credentials
        val response = runBlocking {
            Timber.d("[${request.path()}] Re-authenticating in the background using credentials.")
            ApiService.get().login(credentials)
        }

        return when (response) {
            is NetworkResponse.Success -> {
                Timber.d("[${request.path()}] Re-authentication success.")

                // Proceed with original request, adding token
                request.signedRequest()
            }
            else -> {
                Timber.d("[${request.path()}] Re-authentication failed.")
                null
            }
        }
    }
}
