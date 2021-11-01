package com.ipleiria.estg.meicm.arcismarthome.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ipleiria.estg.meicm.arcismarthome.database.FavouriteDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.database.SensorDataModel
import com.ipleiria.estg.meicm.arcismarthome.databinding.FavouriteItemBinding
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor


class FavouritesAdapter(val clickListener: FavouriteListener): ListAdapter<Sensor, FavouritesAdapter.ViewHolder>(SensorDiffCallback(), ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    class ViewHolder private constructor(val binding: FavouriteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sensor, clickListener: FavouriteListener) {
            binding.sensor = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FavouriteItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class SensorDiffCallback : DiffUtil.ItemCallback<Sensor>() {

    override fun areItemsTheSame(oldItem: Sensor, newItem: Sensor): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Sensor, newItem: Sensor): Boolean {
        return oldItem == newItem
    }
}

class FavouriteListener(val clickListener: (sensorId: Int) -> Unit) {
    fun onClick(sensor: Sensor) = clickListener(sensor.id!!)
}
