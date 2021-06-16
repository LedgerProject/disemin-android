package gr.exm.agroxm.ui.addfield

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import gr.exm.agroxm.databinding.ActivityAddFieldBinding
import gr.exm.agroxm.ui.addfield.*
import timber.log.Timber

interface DataProvider {
    fun isDataValid(): Boolean
    fun getData(): Bundle
}

interface OnCompleteListener {
    fun onComplete()
}

class AddFieldActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFieldBinding
    private lateinit var adapter: AddFieldPagerAdapter
    private val data = Bundle()

    private val viewModel: DataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFieldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.location.observe(this, {
            TODO()
        })

        binding.toolbar.setNavigationOnClickListener { onSupportNavigateUp() }

        adapter = AddFieldPagerAdapter(this)

        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = false
        binding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                this@AddFieldActivity.onPageSelected(position)
            }
        })

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = adapter.getItemTitle(position)
        }.attach()
    }

    private fun onPageSelected(position: Int) {
        Toast.makeText(this, "Position #$position", Toast.LENGTH_SHORT).show()

        val hasNext = position < adapter.itemCount - 1
        binding.next.text = if (hasNext) "Next" else "Submit"
        binding.next.setOnClickListener { if (hasNext) next(position) else submit(position) }
    }

    private fun getData(position: Int) {

    }

    private fun next(position: Int) {
        // Get current fragment
        val fragment = supportFragmentManager.findFragmentByTag("f${adapter.getItemId(position)}")
        fragment?.let {
            if ((it as DataProvider).isDataValid()) {
                Timber.d("Data valid: ${it.getData()}")
                this.data.putAll(it.getData())
                val hasNext = position <= adapter.itemCount - 1
                if (hasNext) {
                    binding.pager.setCurrentItem(position + 1, true)
                }
            } else {
                Timber.d("Invalid data")
            }
        }
    }

    private fun submit(position: Int) {
        // Get all data
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }
}

class AddFieldPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            STEP_LOCATION -> AddFieldLocation()
            STEP_INFO -> AddFieldInfo()
            STEP_DEVICE -> AddFieldDevice()
            STEP_FORECAST -> AddFieldForecast()
            else -> AddFieldInfo()
        }
    }

    override fun getItemCount(): Int = STEP_COUNT

    fun getItemTitle(position: Int): String {
        return when (position) {
            STEP_LOCATION -> "${position + 1}. Location"
            STEP_INFO -> "${position + 1}. Field Info"
            STEP_DEVICE -> "${position + 1}. Device"
            STEP_FORECAST -> "${position + 1}. Forecast"
            else -> ""
        }
    }

    override fun getItemId(position: Int): Long {
        return when (position) {
            STEP_LOCATION -> STEP_LOCATION.toLong()
            STEP_INFO -> STEP_INFO.toLong()
            STEP_DEVICE -> STEP_DEVICE.toLong()
            STEP_FORECAST -> STEP_FORECAST.toLong()
            else -> -1
        }
    }

    companion object {
        internal const val STEP_LOCATION = 0
        internal const val STEP_INFO = 1
        internal const val STEP_DEVICE = 2
        internal const val STEP_FORECAST = 3
        internal const val STEP_COUNT = 4
    }
}
