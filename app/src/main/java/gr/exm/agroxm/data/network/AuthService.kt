package gr.exm.agroxm.data.network

import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.AuthToken
import gr.exm.agroxm.data.io.LoginBody
import gr.exm.agroxm.data.io.RefreshBody
import gr.exm.agroxm.data.io.RegistrationBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/auth/login")
    suspend fun login(
        @Body credentials: LoginBody,
    ): NetworkResponse<AuthToken, ErrorResponse>

    @POST("/auth/register")
    suspend fun register(
        @Body registration: RegistrationBody,
    ): NetworkResponse<AuthToken, ErrorResponse>

    @POST("/auth/refresh")
    suspend fun refresh(
        @Body refresh: RefreshBody,
    ): NetworkResponse<AuthToken, ErrorResponse>
}
