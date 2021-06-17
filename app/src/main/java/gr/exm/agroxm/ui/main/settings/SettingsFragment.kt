package gr.exm.agroxm.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import gr.exm.agroxm.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var model: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding

    init {
        lifecycleScope.launchWhenCreated {
            model = ViewModelProvider(this@SettingsFragment).get(SettingsViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
}