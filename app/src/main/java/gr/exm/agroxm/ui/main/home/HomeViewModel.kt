package gr.exm.agroxm.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.io.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel : ViewModel() {

    private val _data = MutableLiveData<Resource<List<Field>>>().apply {
        value = Resource.loading()
    }
    val data: LiveData<Resource<List<Field>>> = _data

    fun fetch() {
        _data.postValue(Resource.loading())

        CoroutineScope(Dispatchers.IO).launch {
            when (val response = ApiService.get().fields()) {
                is NetworkResponse.Success -> {
                    Timber.d("Got fields")
                    _data.postValue(Resource.success(response.body))
                }
                is NetworkResponse.ServerError -> {
                    Timber.d("ServerError getting fields")
                    _data.postValue(response.body?.let { Resource.error(it.message, null) })
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d(response.error, "NetworkError getting fields")
                    _data.postValue(response.error.message?.let { Resource.error(it, emptyList()) })
                }
                is NetworkResponse.UnknownError -> {
                    Timber.d(response.error, "UnknownError getting fields")
                    _data.postValue(response.error.message?.let { Resource.error(it, emptyList()) })
                }
            }
        }
    }
}