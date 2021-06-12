package gr.exm.agroxm.data.network.interceptor

import com.squareup.moshi.Moshi
import gr.exm.agroxm.data.network.AuthToken
import gr.exm.agroxm.data.path
import gr.exm.agroxm.data.repository.AuthTokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * {@see okhttp3.Interceptor} that saves an AuthToken in the response of the /auth endpoints.
 */
class AuthRequestInterceptor : Interceptor, KoinComponent {
    private val authTokenRepository by inject<AuthTokenRepository>()

    override fun intercept(chain: Interceptor.Chain): Response {
        // Original request
        val request: Request = chain.request()

        // Try to extract auth token from response before proceeding
        return chain.proceed(request).extractAuthToken(authTokenRepository)
    }
}

private fun Response.extractAuthToken(authTokenRepository: AuthTokenRepository): Response {
    if (this.isSuccessful) {
        try {
            Timber.d("[${this.path()}] Trying to extract auth token.")
            val body = this.peekBody(Long.MAX_VALUE).string()
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(AuthToken::class.java).lenient()
            val authToken = adapter.fromJson(body)
            authToken?.let {
                Timber.d("[${this.path()}] Saving token from response.")
                runBlocking { authTokenRepository.setAuthToken(authToken) }
            }
        } catch (e: Exception) {
            Timber.d(e, "[${this.path()}] Failed to extract auth token.")
        }
    }
    return this
}
