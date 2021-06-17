package gr.exm.agroxm.data.network

import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.AuthToken
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/auth/login")
    suspend fun login(
        @Body credentials: LoginBody,
    ): NetworkResponse<AuthToken, AuthError>

    @POST("/auth/register")
    suspend fun register(
        @Body registration: RegistrationBody,
    ): NetworkResponse<AuthToken, AuthError>

    @POST("/auth/refresh")
    suspend fun refresh(
        @Body refresh: RefreshBody,
    ): NetworkResponse<AuthToken, AuthError>
}
