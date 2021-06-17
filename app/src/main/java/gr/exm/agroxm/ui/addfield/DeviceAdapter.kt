package gr.exm.agroxm.ui.addfield

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import gr.exm.agroxm.data.Device
import gr.exm.agroxm.databinding.ListItemDeviceBinding

class DeviceAdapter : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    var tracker: SelectionTracker<String>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            ListItemDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device, tracker?.isSelected(device.id))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    class DeviceViewHolder(private val binding: ListItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {

            }
        }

        var item: Device? = null

        @SuppressLint("SetTextI18n")
        fun bind(item: Device, selected: Boolean?) {
            this.item = item
            binding.name.text = item.name
            binding.location.text = "${item.location.latitude}, ${item.location.longitude}"
            binding.selected.visibility = if (selected == true) View.VISIBLE else View.GONE
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = item?.id!!
                override fun inSelectionHotspot(e: MotionEvent): Boolean = true
            }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {

        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem == newItem
        }
    }

    class DeviceKeyProvider(private val adapter: DeviceAdapter) :
        ItemKeyProvider<String>(SCOPE_CACHED) {
        override fun getKey(position: Int): String = adapter.currentList[position].id
        override fun getPosition(key: String): Int =
            adapter.currentList.indexOfFirst { it.id == key }
    }

    class DeviceDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            return if (view != null) {
                (recyclerView.getChildViewHolder(view) as DeviceAdapter.DeviceViewHolder).getItemDetails()
            } else null
        }
    }
}