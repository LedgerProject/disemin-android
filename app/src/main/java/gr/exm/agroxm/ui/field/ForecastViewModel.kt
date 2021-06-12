package gr.exm.agroxm.ui.field

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.R
import gr.exm.agroxm.data.Aggregation
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.TimeWindow
import gr.exm.agroxm.data.network.ApiService
import gr.exm.agroxm.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class ForecastViewModel : ViewModel(), KoinComponent {

    enum class Mode {
        NEXT_HOUR,
        TODAY,
        NEXT_24H,
        NEXT_WEEK
    }

    private val apiService: ApiService by inject()

    private val _data = MutableLiveData<Resource<List<LineData>>>().apply {
        value = Resource.loading()
    }
    val data: LiveData<Resource<List<LineData>>> = _data

    private var job: Job? = null

    private val formatter = object : ValueFormatter() {
        private val DECIMAL_FORMAT = DecimalFormat("#.#")

        override fun getPointLabel(entry: Entry?): String {
            return DECIMAL_FORMAT.format(entry?.y)
        }
    }

    fun fetch(fieldId: String, mode: Mode) {
        // Set status to loading
        _data.postValue(Resource.loading())

        // Cancel any pending job
        job?.cancel()

        // Start new job
        job = CoroutineScope(Dispatchers.IO).launch {
            val devices = apiService.getFieldForecasts(fieldId)
            if (devices !is NetworkResponse.Success || devices.body.isNullOrEmpty()) {
                Timber.d("No forecasts to query data for: $devices")
                return@launch _data.postValue(Resource.error("No forecasts in this field."))
            }

            val deviceId = devices.body.first().id
            val timeWindow = getTimeWindow(mode)

            val response = apiService.forecastData(
                deviceId = deviceId,
                startTs = timeWindow.startTs,
                endTs = timeWindow.endTs,
                aggregation = timeWindow.aggregation.name,
                interval = timeWindow.interval
            )

            when (response) {
                is NetworkResponse.Success -> {
                    Timber.d("Got timeseries.")
                    val datasets = response.body
                        .map { telemetry ->
                            val entries = telemetry.value
                                .filter { it.value != null }
                                .sortedBy { it.ts }
                                .map { it.entry() }
                            val dataset = newDataSet(entries, telemetry.key)
                            val lineData = LineData(dataset)
                            lineData.setValueFormatter(formatter)
                            return@map lineData
                        }
                    _data.postValue(Resource.success(datasets))
                }
                is NetworkResponse.ServerError -> {
                    Timber.d("ServerError getting timeseries. ${response.body?.message}")
                    _data.postValue(response.body?.let { Resource.error(it.message, null) })
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d(response.error, "NetworkError getting timeseries")
                    _data.postValue(response.error.message?.let { Resource.error(it) })
                }
                is NetworkResponse.UnknownError -> {
                    Timber.d(response.error, "UnknownError getting timeseries")
                    _data.postValue(response.error.message?.let { Resource.error(it) })
                }
            }
        }
    }

    private fun getTimeWindow(mode: Mode): TimeWindow {
        return when (mode) {
            Mode.NEXT_HOUR -> {
                TimeWindow(
                    startTs = LocalDateTime.now().minusHours(1).startOfHour().millis(),
                    endTs = LocalDateTime.now().plusHours(1).endOfHour().millis(),
                    aggregation = Aggregation.AVG,
                    interval = TimeUnit.MINUTES.toMillis(10)
                )
            }
            Mode.TODAY -> {
                TimeWindow(
                    startTs = LocalDateTime.now().startOfHour().millis(),
                    endTs = LocalDateTime.now().plusDays(1).endOfDay().millis(),
                    aggregation = Aggregation.AVG,
                    interval = TimeUnit.HOURS.toMillis(1)
                )
            }
            Mode.NEXT_24H -> {
                TimeWindow(
                    startTs = LocalDateTime.now().startOfHour().millis(),
                    endTs = LocalDateTime.now().plusDays(1).endOfHour().millis(),
                    aggregation = Aggregation.AVG,
                    interval = TimeUnit.HOURS.toMillis(6)
                )
            }
            Mode.NEXT_WEEK -> {
                TimeWindow(
                    startTs = LocalDateTime.now().startOfHour().millis(),
                    endTs = LocalDateTime.now().plusWeeks(1).endOfHour().millis(),
                    aggregation = Aggregation.AVG,
                    interval = TimeUnit.DAYS.toMillis(1)
                )
            }
        }
    }

    private fun newDataSet(entries: List<Entry>, label: String): LineDataSet {
        return LineDataSet(entries, label).apply {
            color = ResourcesHelper.getColor(R.color.chart_line_color)
            circleHoleColor = ResourcesHelper.getColor(R.color.chart_point_hole_color)
            circleRadius = ResourcesHelper.getDimension(R.dimen.chart_point_radius)
            circleHoleRadius = ResourcesHelper.getDimension(R.dimen.chart_point_hole_radius)
            circleColors = listOf(ResourcesHelper.getColor(R.color.chart_point_color))
            highLightColor = ResourcesHelper.getColor(R.color.chart_highlight_color)
        }
    }
}