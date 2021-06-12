package gr.exm.agroxm.ui.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.Role
import gr.exm.agroxm.data.io.LoginBody
import gr.exm.agroxm.data.io.RegistrationBody
import gr.exm.agroxm.data.network.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SignupViewModel : ViewModel(), KoinComponent {

    private val authService: AuthService by inject()

    private val isSignedUp = MutableLiveData<Resource<Unit>>()
    fun isSignedUp() = isSignedUp

    fun login(username: String, password: String) {
        isSignedUp.postValue(Resource.loading())
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = authService.login(LoginBody(username, password))) {
                is NetworkResponse.Success -> {
                    isSignedUp.postValue(Resource.success(Unit))
                }
                is NetworkResponse.ServerError -> {
                    val error = response.body
                    Timber.d(response.error, "Server Error ${error?.message} [${error?.code}]")
                    isSignedUp.postValue(Resource.error(error?.message ?: "Server Error"))
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d(response.error, "Network Error.")
                    isSignedUp.postValue(Resource.error("Network Error. Please try again."))
                }
                is NetworkResponse.UnknownError -> {
                    Timber.d(response.error, "Unknown Error.")
                    isSignedUp.postValue(Resource.error("Unknown Error. Please try again."))
                }
            }
        }
    }

    fun signup(
        username: String,
        password: String,
        role: Role,
        firstName: String?,
        lastName: String?,
    ) {
        val registration = RegistrationBody(
            username = username,
            password = password,
            role = role,
            firstName = firstName,
            lastName = lastName
        )

        isSignedUp.postValue(Resource.loading())
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = authService.register(registration)) {
                is NetworkResponse.Success -> {
                    isSignedUp.postValue(Resource.success(Unit))
                }
                is NetworkResponse.ServerError -> {
                    val error = response.body
                    Timber.d(response.error, "Server Error ${error?.message} [${error?.code}]")
                    isSignedUp.postValue(Resource.error(error?.message ?: "Server Error"))
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d(response.error, "Network Error.")
                    isSignedUp.postValue(Resource.error("Network Error. Please try again."))
                }
                is NetworkResponse.UnknownError -> {
                    Timber.d(response.error, "Unknown Error.")
                    isSignedUp.postValue(Resource.error("Unknown Error. Please try again."))
                }
            }
        }
    }
}