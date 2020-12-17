package gr.exm.agroxm.ui.addfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import gr.exm.agroxm.databinding.FragmentAddFieldInfoBinding
import gr.exm.agroxm.ui.DataProvider
import gr.exm.agroxm.util.onTextChanged

class AddFieldInfo : Fragment(), DataProvider {

    private lateinit var binding: FragmentAddFieldInfoBinding
    private val data = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFieldInfoBinding.inflate(inflater, container, false)
        binding.fieldName.onTextChanged {
            data.putString(ARG_FIELD_NAME, it)
            binding.fieldNameContainer.error = null
        }
        binding.fieldDescription.onTextChanged {
            data.putString(ARG_FIELD_DESCRIPTION, it)
            binding.fieldDescriptionContainer.error = null
        }
        binding.cropName.onTextChanged {
            data.putString(ARG_CROP_NAME, it)
            binding.cropNameContainer.error = null
        }
        binding.cropDescription.onTextChanged {
            data.putString(ARG_CROP_DESCRIPTION, it)
            binding.cropDescriptionContainer.error = null
        }
        return binding.root
    }

    override fun isDataValid(): Boolean {
        if (data.getString(ARG_FIELD_NAME).isNullOrEmpty()) {
            binding.fieldNameContainer.error = "Field name cannot be empty"
            return false
        }
        return true
    }

    override fun getData(): Bundle {
        return Bundle().apply {
            putString(ARG_FIELD_NAME, binding.fieldName.text.toString())
            putString(ARG_FIELD_DESCRIPTION, binding.fieldDescription.text.toString())
            putString(ARG_CROP_NAME, binding.cropName.text.toString())
            putString(ARG_CROP_DESCRIPTION, binding.cropDescription.text.toString())
        }
    }

    companion object {
        const val ARG_FIELD_NAME = "field_name"
        const val ARG_FIELD_DESCRIPTION = "field_description"
        const val ARG_CROP_NAME = "crop_name"
        const val ARG_CROP_DESCRIPTION = "crop_description"
    }
}