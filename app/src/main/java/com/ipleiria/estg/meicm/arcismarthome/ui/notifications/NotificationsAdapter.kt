package com.ipleiria.estg.meicm.arcismarthome.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDataModel
import com.ipleiria.estg.meicm.arcismarthome.databinding.NotificationsListItemBinding

class NotificationsAdapter(val clickListener: NotificationListener): ListAdapter<NotificationDataModel, NotificationsAdapter.ViewHolder>(NotificationDiffCallback(), ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    class ViewHolder private constructor(val binding: NotificationsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationDataModel, clickListener: NotificationListener) {
            binding.notification = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NotificationsListItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationDataModel>() {

        override fun areItemsTheSame(oldItem: NotificationDataModel, newItem: NotificationDataModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationDataModel, newItem: NotificationDataModel): Boolean {
            return oldItem == newItem
        }
    }


    class NotificationListener(val clickListener: (notificationId: Int) -> Unit) {
        fun onClick(notification: NotificationDataModel) = clickListener(notification.id)
    }

}


