package gr.exm.agroxm.ui.field

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.ValueFormatter
import gr.exm.agroxm.databinding.ListItemSensorDataBinding
import gr.exm.agroxm.util.Measurements
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

class LineDataAdapter :
    ListAdapter<LineData, LineDataAdapter.LineDataViewHolder>(LineDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineDataViewHolder {
        return LineDataViewHolder(
            ListItemSensorDataBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LineDataViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LineDataViewHolder(private val binding: ListItemSensorDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.chart.description = null
            binding.chart.legend.isEnabled = false
            binding.chart.isScaleYEnabled = false
            //binding.chart.isScaleXEnabled = true

            val xAxis: XAxis = binding.chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = TimeUnit.HOURS.toMillis(3).toFloat()

            val locale = Locale.getDefault()
            xAxis.valueFormatter = object : ValueFormatter() {
                val DATE_FORMAT = DateTimeFormatter.ofPattern(
                    DateFormat.getBestDateTimePattern(locale, "dd/MM, HH:mm")
                )

                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val date = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(value.toLong()), ZoneId.systemDefault()
                    ).truncatedTo(ChronoUnit.HOURS)
                    return date.format(DATE_FORMAT)
                }
            }
        }

        fun bind(item: LineData) {
            binding.title.text = Measurements.getName(itemView.context, item.dataSets[0].label)
            binding.chart.data = item

            // Limit the viewport. All viewport operations
            // must happen after setting the chart data.
            binding.chart.setVisibleXRange(
                TimeUnit.MINUTES.toMillis(30).toFloat(),
                TimeUnit.HOURS.toMillis(3).toFloat()
            )

            // Scroll right to the latest X value
            binding.chart.moveViewToX(item.xMax)

            binding.chart.postInvalidate()
        }
    }

    class LineDataDiffCallback : DiffUtil.ItemCallback<LineData>() {

        override fun areItemsTheSame(oldItem: LineData, newItem: LineData): Boolean {
            return oldItem.dataSets[0].label == newItem.dataSets[0].label
        }

        override fun areContentsTheSame(oldItem: LineData, newItem: LineData): Boolean {
            return oldItem == newItem
        }
    }
}