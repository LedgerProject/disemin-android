package gr.exm.agroxm.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gr.exm.agroxm.data.Field

class MainViewModel : ViewModel() {
    private val mutableSelectedField = MutableLiveData<Field>()
    val selectedField: LiveData<Field> get() = mutableSelectedField

    fun selectField(field: Field) {
        mutableSelectedField.value = field
    }
}