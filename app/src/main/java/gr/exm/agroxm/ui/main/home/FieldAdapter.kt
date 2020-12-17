package gr.exm.agroxm.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.databinding.ListItemFieldBinding

class FieldAdapter(val listener: OnFieldSelectedListener) :
    ListAdapter<Field, FieldAdapter.FieldViewHolder>(FieldDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val binding =
            ListItemFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FieldViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FieldViewHolder(
        private val binding: ListItemFieldBinding,
        listener: OnFieldSelectedListener,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var field: Field

        init {
            binding.root.setOnClickListener {
                listener.onFieldSelected(field)
            }
        }

        fun bind(item: Field) {
            this.field = item
            binding.name.text = item.name
            binding.crop.text = item.currentCrop?.name ?: "No Crop Information"
        }
    }

    class FieldDiffCallback : DiffUtil.ItemCallback<Field>() {

        override fun areItemsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem == newItem
        }
    }
}