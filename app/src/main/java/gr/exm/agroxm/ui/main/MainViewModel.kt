package gr.exm.agroxm.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {

    private val repository: AuthRepository by inject()

    private val mutableSelectedField = MutableLiveData<Field>()
    val selectedField: LiveData<Field> get() = mutableSelectedField

    fun selectField(field: Field) {
        mutableSelectedField.value = field
    }

    fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            repository.logout()
        }
    }
}