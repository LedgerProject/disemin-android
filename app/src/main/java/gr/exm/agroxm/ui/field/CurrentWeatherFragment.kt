package gr.exm.agroxm.ui.field

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import gr.exm.agroxm.R
import gr.exm.agroxm.data.CurrentWeather
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.data.Resource
import gr.exm.agroxm.data.Status
import gr.exm.agroxm.databinding.FragmentCurrentWeatherBinding
import gr.exm.agroxm.databinding.ViewCurrentWeatherBinding
import gr.exm.agroxm.util.Weather
import gr.exm.agroxm.util.formatDefault
import gr.exm.agroxm.util.fromMillis
import kotlinx.coroutines.launch


class CurrentWeatherFragment : Fragment() {

    private val model: CurrentWeatherViewModel by viewModels()
    private lateinit var binding: FragmentCurrentWeatherBinding
    private lateinit var field: Field

    companion object {
        private const val ARG_FIELD = "field"

        fun newInstance(field: Field) = CurrentWeatherFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_FIELD, field) }
        }
    }

    init {
        lifecycleScope.launch {
            whenCreated {
                field = requireNotNull(arguments?.getParcelable(ARG_FIELD))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCurrentWeatherBinding.inflate(inflater, container, false)

        // Setup swipe-to-refresh
        binding.swiperefresh.setOnRefreshListener { model.fetch(field.id) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update UI when new data arrive
        model.weather().observe(viewLifecycleOwner, { updateUI(it) })

        // Fetch data
        model.fetch(field.id)
    }

    private fun updateUI(resource: Resource<CurrentWeather>) {
        when (resource.status) {
            Status.SUCCESS -> {
                resource.data?.let { weather ->
                    with(ViewCurrentWeatherBinding.bind(binding.root)) {
                        icon.setAnimation(R.raw.anim_weather_clear_day)
                        temperature.text = Weather.getFormattedTemperature(weather.temperature)
                        precipitation.text = Weather.getFormattedPrecipitation(weather.precipitation)
                        humidity.text = Weather.getFormattedHumidity(weather.humidity)
                        wind.text = Weather.getFormattedWind(weather.windSpeed, weather.windDirection)
                        cloud.text = Weather.getFormattedCloud(weather.cloud)
                        solar.text = Weather.getFormattedUV(weather.uv)
                        updated.text = weather.ts?.let {
                            "Updated on ${fromMillis(it).formatDefault()}"
                        } ?: ""
                    }
                }

                binding.swiperefresh.isRefreshing = false
                binding.content.visibility = View.VISIBLE
                binding.empty.visibility = View.GONE
                binding.progress.visibility = View.GONE
            }
            Status.ERROR -> {
                binding.swiperefresh.isRefreshing = false
                binding.empty.title("Oops! Could not find any data.")
                binding.empty.subtitle(resource.message)
                binding.empty.action("Retry")
                binding.empty.listener { model.fetch(field.id) }
                binding.empty.visibility = View.VISIBLE
                binding.content.visibility = View.GONE
                binding.progress.visibility = View.GONE
            }
            Status.LOADING -> {
                if (!binding.swiperefresh.isRefreshing) {
                    binding.progress.visibility = View.VISIBLE
                } else {
                    binding.swiperefresh.isRefreshing = true
                    binding.progress.visibility = View.GONE
                }
                binding.empty.visibility = View.GONE
            }
        }
    }
}