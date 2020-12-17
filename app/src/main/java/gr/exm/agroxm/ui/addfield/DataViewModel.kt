package gr.exm.agroxm.ui.addfield

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import gr.exm.agroxm.data.Device
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.data.Forecast

class DataViewModel : ViewModel() {

    private val mutableLocation = MutableLiveData<Location>()
    val location: LiveData<Location> get() = mutableLocation

    private val mutableField = MutableLiveData<Field>()
    val field: LiveData<Field> get() = mutableField

    private val mutableDevice = MutableLiveData<Device>()
    val device: LiveData<Device> get() = mutableDevice

    private val mutableDeviceId = MutableLiveData<String>()
    val deviceId: LiveData<String> get() = mutableDeviceId

    private val mutableForecast = MutableLiveData<Forecast>()
    val forecast: LiveData<Forecast> get() = mutableForecast

    fun setLocation(location: Location) {
        mutableLocation.value = location
    }

    fun setField(field: Field) {
        mutableField.value = field
    }

    fun setDevice(device: Device) {
        mutableDevice.value = device
    }

    fun setDeviceId(deviceId: String?) {
        mutableDeviceId.value = deviceId
    }

    fun setForecast(forecast: Forecast) {
        mutableForecast.value = forecast
    }
}