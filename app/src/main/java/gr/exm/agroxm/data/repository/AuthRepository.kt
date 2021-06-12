package gr.exm.agroxm.data.repository

import arrow.core.Either
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.Role
import gr.exm.agroxm.data.datasource.AuthTokenDataSource
import gr.exm.agroxm.data.datasource.CredentialsDataSource
import gr.exm.agroxm.data.network.AuthService
import gr.exm.agroxm.data.network.Credentials
import gr.exm.agroxm.data.network.LoginBody
import gr.exm.agroxm.data.network.RegistrationBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

interface AuthRepository {
    suspend fun login(username: String, password: String): Either<Error, String>
    suspend fun logout()
    suspend fun isLoggedIn(): Either<Error, String>
    suspend fun signup(
        username: String,
        password: String,
        role: Role,
        firstName: String?,
        lastName: String?
    ): Either<Error, String>
}

class AuthRepositoryImpl(
    private val authTokenDatasource: AuthTokenDataSource,
    private val credentialslDatasource: CredentialsDataSource,
) : AuthRepository, KoinComponent {

    private val service: AuthService by inject()

    override suspend fun login(username: String, password: String): Either<Error, String> {
        return when (val response = service.login(LoginBody(username, password))) {
            is NetworkResponse.Success -> {
                Timber.d("Login success. Saving credentials.")
                credentialslDatasource.setCredentials(Credentials(username, password))
                Either.Right(username)
            }
            is NetworkResponse.ServerError -> {
                Either.Left(
                    Error(
                        response.body?.message ?: response.error.message,
                        response.error
                    )
                )
            }
            is NetworkResponse.NetworkError -> {
                Either.Left(Error("Network Error", response.error))
            }
            is NetworkResponse.UnknownError -> {
                Either.Left(Error("Unknown Error", response.error))
            }
        }
    }

    override suspend fun logout() {
        authTokenDatasource.clear()
        credentialslDatasource.clear()
    }

    override suspend fun isLoggedIn(): Either<Error, String> {
        return credentialslDatasource.getCredentials().map { it.username }
    }

    override suspend fun signup(
        username: String,
        password: String,
        role: Role,
        firstName: String?,
        lastName: String?
    ): Either<Error, String> {
        val registration = RegistrationBody(
            username = username,
            password = password,
            role = role,
            firstName = firstName,
            lastName = lastName
        )
        return when (val response = service.register(registration)) {
            is NetworkResponse.Success -> {
                Timber.d("Signup success. Saving credentials.")
                credentialslDatasource.setCredentials(Credentials(username, password))
                Either.Right(username)
            }
            is NetworkResponse.ServerError -> {
                Either.Left(
                    Error(
                        response.body?.message ?: response.error.message,
                        response.error
                    )
                )
            }
            is NetworkResponse.NetworkError -> {
                Either.Left(Error("Network Error", response.error))
            }
            is NetworkResponse.UnknownError -> {
                Either.Left(Error("Unknown Error", response.error))
            }
        }
    }
}
