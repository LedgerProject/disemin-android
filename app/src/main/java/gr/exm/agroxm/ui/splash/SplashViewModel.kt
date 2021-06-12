package gr.exm.agroxm.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import gr.exm.agroxm.data.repository.CredentialsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SplashViewModel : ViewModel(), KoinComponent {

    private val credentialsRepository: CredentialsRepository by inject()

    private val isLoggedIn = MutableLiveData<Either<Error, String>>().apply {
        Timber.d("Getting credentials in the background")
        viewModelScope.launch(Dispatchers.IO) {
            postValue(credentialsRepository.isLoggedIn())
        }
    }

    fun isLoggedIn(): LiveData<Either<Error, String>> = isLoggedIn

}