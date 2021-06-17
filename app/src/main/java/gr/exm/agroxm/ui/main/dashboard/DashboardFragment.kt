package gr.exm.agroxm.ui.main.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import gr.exm.agroxm.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch
import java.util.*


class DashboardFragment : Fragment() {

    private lateinit var model: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding

    init {
        lifecycleScope.launch {
            whenCreated {
                model =
                    ViewModelProvider(this@DashboardFragment).get(DashboardViewModel::class.java)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
}