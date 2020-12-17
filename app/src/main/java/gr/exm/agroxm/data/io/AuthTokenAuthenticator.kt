package gr.exm.agroxm.data.io

import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.AuthToken
import gr.exm.agroxm.util.AuthHelper
import gr.exm.agroxm.util.AuthHelper.hasAuthHeader
import gr.exm.agroxm.util.AuthHelper.signedRequest
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

        // Skip authentication if this is an explicit call to /auth endpoints
        if (request.header(AuthHelper.NO_AUTH_HEADER).equals("true")) {
            Timber.d("Skipping authentication for path: ${request.url.encodedPath}")
            return null
        }

        // We've already added auth header, but are still unauthorized
        if (request.hasAuthHeader()) {
            // Try to re-login with saved credentials.
            return authenticateWithCredentials(request)
        }

        // Add auth header using the auth token with Bearer schema
        return authenticateWithAccessToken(request)
    }

    private fun authenticateWithAccessToken(request: Request): Request? {
        Timber.d("Refreshing token")

        // Get stored auth token
        val authToken = AuthHelper.getAuthToken()

        // If token is valid, add it in auth header
        if (authToken.isTokenValid()) {
            return request.newBuilder()
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
                    return request.signedRequest()
                }
                else -> {
                    Timber.d("Token refresh failed")
                }
            }
        }

        // We cannot use access token. Try to use credentials.
        Timber.d("Cannot authenticate with access token.")
        return authenticateWithCredentials(request)
    }

    private fun authenticateWithCredentials(request: Request): Request? {
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
                return request.signedRequest()
            }
            else -> {
                Timber.d("Authentication in background failed")
                return null
            }
        }
    }
}
