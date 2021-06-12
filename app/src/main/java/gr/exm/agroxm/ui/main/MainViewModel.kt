package gr.exm.agroxm.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.data.repository.AuthTokenRepository
import gr.exm.agroxm.data.repository.CredentialsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {

    private val authTokenRepository: AuthTokenRepository by inject()
    private val credentialsRepository: CredentialsRepository by inject()

    private val mutableSelectedField = MutableLiveData<Field>()
    val selectedField: LiveData<Field> get() = mutableSelectedField

    fun selectField(field: Field) {
        mutableSelectedField.value = field
    }

    fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            authTokenRepository.clear()
            credentialsRepository.clear()
        }
    }
}