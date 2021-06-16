package gr.exm.agroxm.ui.field

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.databinding.ActivityFieldDetailBinding

class FieldDetailActivity : AppCompatActivity() {

    companion object {
        const val ARG_FIELD = "field"
    }

    private lateinit var binding: ActivityFieldDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFieldDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val field = intent.getParcelableExtra<Field>(ARG_FIELD)
        binding.toolbar.title = field?.name
        binding.toolbar.setNavigationOnClickListener { onSupportNavigateUp() }

        val adapter = FieldDetailPagerAdapter(this, requireNotNull(field))
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = adapter.getItemTitle(position)
        }.attach()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }
}

class FieldDetailPagerAdapter(activity: FragmentActivity, val field: Field) :
    FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_CURRENT -> CurrentWeatherFragment.newInstance(field)
            PAGE_DEVICE -> DeviceFragment.newInstance(field.id)
            PAGE_FORECAST -> ForecastFragment.newInstance(field.id)
            else -> Fragment()
        }
    }

    override fun getItemCount(): Int = PAGE_COUNT

    fun getItemTitle(position: Int): String {
        return when (position) {
            PAGE_CURRENT -> "Current"
            PAGE_DEVICE -> "Monitoring"
            PAGE_FORECAST -> "Forecast"
            else -> ""
        }
    }

    override fun getItemId(position: Int): Long {
        return when (position) {
            PAGE_DEVICE -> PAGE_DEVICE.toLong()
            PAGE_FORECAST -> PAGE_FORECAST.toLong()
            else -> -1
        }
    }

    companion object {
        internal const val PAGE_CURRENT = 0
        internal const val PAGE_DEVICE = 1
        internal const val PAGE_FORECAST = 2
        internal const val PAGE_COUNT = 3
    }
}