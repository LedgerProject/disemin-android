package gr.exm.agroxm.ui.addfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.haroldadmin.cnradapter.NetworkResponse
import gr.exm.agroxm.data.network.ApiService
import gr.exm.agroxm.databinding.FragmentAddFieldDeviceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class AddFieldDevice : Fragment(), DataProvider, KoinComponent {

    private val apiService: ApiService by inject()
    private val viewModel: DataViewModel by activityViewModels()
    private lateinit var binding: FragmentAddFieldDeviceBinding
    private var adapter: DeviceAdapter = DeviceAdapter()

    fun fetch() {
        CoroutineScope(Dispatchers.IO).launch {
            when (val response = apiService.getAvailableDevices()) {
                is NetworkResponse.Success -> {
                    Timber.d("Got devices.")
                    withContext(Dispatchers.Main) { adapter.submitList(response.body) }
                }
                is NetworkResponse.ServerError -> {
                    Timber.d("ServerError getting timeseries")
                }
                is NetworkResponse.NetworkError -> {
                    Timber.d(response.error, "NetworkError getting timeseries")
                }
                is NetworkResponse.UnknownError -> {
                    Timber.d(response.error, "UnknownError getting timeseries")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFieldDeviceBinding.inflate(inflater, container, false)
        adapter.setHasStableIds(true)
        binding.recycler.adapter = adapter
        val tracker = SelectionTracker.Builder(
            "DeviceSelectionTracker",
            binding.recycler,
            DeviceAdapter.DeviceKeyProvider(adapter),
            DeviceAdapter.DeviceDetailsLookup(binding.recycler),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val selectedDeviceId = tracker.selection?.firstOrNull()
                    viewModel.setDeviceId(selectedDeviceId)
                    Timber.d("Selection changed: $selectedDeviceId")
                }
            })
        adapter.tracker = tracker
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetch()
    }

    override fun isDataValid(): Boolean {
        return true
    }

    override fun getData(): Bundle {
        return Bundle()
    }

    companion object {
        const val ARG_FIELD_NAME = "field_name"
        const val ARG_FIELD_DESCRIPTION = "field_description"
        const val ARG_CROP_NAME = "crop_name"
        const val ARG_CROP_DESCRIPTION = "crop_description"
    }
}