package gr.exm.agroxm.data.repository

import android.content.SharedPreferences
import arrow.core.Either
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.io.LoginBody
import gr.exm.agroxm.data.network.AuthService
import gr.exm.agroxm.data.network.Credentials
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

interface CredentialsRepository {
    suspend fun getCredentials(): Either<Error, Credentials>
    suspend fun setCredentials(credentials: Credentials)
    suspend fun getUsername(): Either<Error, String>
    suspend fun getPassword(): Either<Error, String>
    suspend fun setUsername(username: String)
    suspend fun setPassword(password: String)
    suspend fun login(username: String, password: String): Either<Error, Unit>
    suspend fun clear()
    suspend fun isLoggedIn(): Either<Error, String>
}

class CredentialsRepositoryImpl(
    private val preferences: SharedPreferences,
) : CredentialsRepository, KoinComponent {

    companion object {
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
    }

    private val service: AuthService by inject()

    override suspend fun getCredentials(): Either<Error, Credentials> {
        val username = preferences.getString(KEY_USERNAME, null)
        val password = preferences.getString(KEY_PASSWORD, null)
        return when {
            username.isNullOrEmpty() -> Either.Left(Error("Invalid username"))
            password.isNullOrEmpty() -> Either.Left(Error("Invalid password"))
            else -> Either.Right(Credentials(username, password))
        }
    }

    override suspend fun setCredentials(credentials: Credentials) {
        preferences.edit().apply {
            putString(KEY_USERNAME, credentials.username)
            putString(KEY_PASSWORD, credentials.password)
        }.apply()
    }

    override suspend fun getUsername(): Either<Error, String> {
        val username = preferences.getString(KEY_USERNAME, null)
        return if (!username.isNullOrEmpty()) {
            Either.Right(username)
        } else {
            Either.Left(Error("Invalid username"))
        }
    }

    override suspend fun getPassword(): Either<Error, String> {
        val password = preferences.getString(KEY_PASSWORD, null)
        return if (!password.isNullOrEmpty()) {
            Either.Right(password)
        } else {
            Either.Left(Error("Invalid password"))
        }
    }

    override suspend fun setUsername(username: String) {
        preferences.edit().putString(KEY_USERNAME, username).apply()
    }

    override suspend fun setPassword(password: String) {
        preferences.edit().putString(KEY_PASSWORD, password).apply()
    }

    override suspend fun login(username: String, password: String): Either<Error, Unit> {
        return when (val response = service.login(LoginBody(username, password))) {
            is NetworkResponse.Success -> {
                Timber.d("Login success. Saving credentials.")
                setCredentials(Credentials(username, password))
                Either.Right(Unit)
            }
            is NetworkResponse.ServerError -> {
                Either.Left(Error(
                    response.body?.message ?: response.error.message,
                    response.error
                ))
            }
            is NetworkResponse.NetworkError -> {
                Either.Left(Error("Network Error", response.error))
            }
            is NetworkResponse.UnknownError -> {
                Either.Left(Error("Unknown Error", response.error))
            }
        }
    }

    override suspend fun clear() {
        preferences.edit().clear().apply()
    }

    override suspend fun isLoggedIn(): Either<Error, String> {
        return getCredentials().map { it.username }
    }
}
