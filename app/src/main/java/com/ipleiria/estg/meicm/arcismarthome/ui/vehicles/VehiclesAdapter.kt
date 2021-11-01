package com.ipleiria.estg.meicm.arcismarthome.ui.vehicles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ipleiria.estg.meicm.arcismarthome.database.VehicleDataModel
import com.ipleiria.estg.meicm.arcismarthome.databinding.VehiclesListItemBinding

import java.util.*

class VehiclesAdapter(private val clickListener: VehicleListener) :
    ListAdapter<VehicleDataModel, VehiclesAdapter.ViewHolder>(VehicleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    class ViewHolder private constructor(val binding: VehiclesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VehicleDataModel, clickListener: VehicleListener) {
            binding.vehicle = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = VehiclesListItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    class VehicleDiffCallback : DiffUtil.ItemCallback<VehicleDataModel>() {

        override fun areItemsTheSame(
            oldItem: VehicleDataModel,
            newItem: VehicleDataModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: VehicleDataModel,
            newItem: VehicleDataModel
        ): Boolean {
            return oldItem == newItem
        }
    }


    class VehicleListener(val clickListener: (vehicleId: Int) -> Unit) {
        fun onClick(vehicle: VehicleDataModel) = clickListener(vehicle.id)
    }
}


