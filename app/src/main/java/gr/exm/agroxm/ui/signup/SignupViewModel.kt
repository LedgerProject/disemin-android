package gr.exm.agroxm.ui.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.Role
import gr.exm.agroxm.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SignupViewModel : ViewModel(), KoinComponent {

    private val repository: AuthRepository by inject()

    private val isSignedUp = MutableLiveData<Resource<Unit>>()
    fun isSignedUp() = isSignedUp

    fun signup(
        username: String,
        password: String,
        role: Role,
        firstName: String?,
        lastName: String?,
    ) {
        isSignedUp.postValue(Resource.loading())
        CoroutineScope(Dispatchers.IO).launch {
            repository.signup(
                username = username,
                password = password,
                role = role,
                firstName = firstName,
                lastName = lastName
            ).mapLeft {
                Timber.d(it, "Login Error")
                isSignedUp.postValue(Resource.error("Signup Error. ${it.message}"))
            }.map {
                isSignedUp.postValue(Resource.success(Unit))
            }
        }
    }
}