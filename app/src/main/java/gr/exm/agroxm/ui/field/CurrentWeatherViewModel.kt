package gr.exm.agroxm.ui.field

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gr.exm.agroxm.data.CurrentWeather
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.repository.FieldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class CurrentWeatherViewModel : ViewModel(), KoinComponent {

    private val repository: FieldRepository by inject()

    private val weather = MutableLiveData<Resource<CurrentWeather>>().apply {
        value = Resource.loading()
    }

    fun weather(): LiveData<Resource<CurrentWeather>> = weather

    private var job: Job? = null

    fun fetch(fieldId: String) {
        // Set status to loading
        weather.postValue(Resource.loading())

        // Cancel any pending job
        job?.cancel()

        // Start new job
        job = CoroutineScope(Dispatchers.IO).launch {
            repository.getCurrentWeather(fieldId)
                .mapLeft {
                    Timber.d(it, "Could not get current weather")
                    weather.postValue(Resource.error(it.message ?: "No error message"))
                }
                .map {
                    Timber.d("Got current weather")
                    weather.postValue(Resource.success(it))
                }
        }
    }
}