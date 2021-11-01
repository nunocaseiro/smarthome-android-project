package com.ipleiria.estg.meicm.arcismarthome.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.database.NotificationDatabase
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentNotificationsBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Notification
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationsFragment :
    Fragment() { //, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener

    lateinit var binding: FragmentNotificationsBinding
    lateinit var adapter: NotificationsAdapter
    lateinit var notificationViewModel: NotificationsViewModel
    var year: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = NotificationDatabase.getInstance(application).notificationDatabaseDao
        val viewModelFactory = NotificationsViewModelFactory(dataSource, application)

        notificationViewModel =
            ViewModelProvider(this, viewModelFactory).get(NotificationsViewModel::class.java)

        binding.notificationViewModel = notificationViewModel

        adapter = NotificationsAdapter(NotificationsAdapter.NotificationListener { notificationId ->
            notificationViewModel.onNotificationClicked(
                notificationId
            )
        })

        binding.notificationList.adapter = adapter

        notificationViewModel.notificationsDB.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isEmpty()) {
                    binding.lbNoNotifications.visibility = View.VISIBLE
                }
                else {
                    binding.lbNoNotifications.visibility = View.GONE
                    adapter.submitList(it)
                }


            }
        })

        notificationViewModel.getNotificationClickedId.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController().navigate(
                    NotificationsFragmentDirections.actionNavigationNotificationsToNotificationDetail2(
                        it
                    )
                )

                notificationViewModel.onNotificationDetailNavigated()
            }

        })

        AppData.instance.notification.observe(viewLifecycleOwner, Observer {
            if (it != 0) {
                notificationViewModel.onStart()
                AppData.instance.notification.value = 0
            }
        })

        val itemTouchHelper = ItemTouchHelper(touchHelper(notificationViewModel, adapter))
        itemTouchHelper.attachToRecyclerView(binding.notificationList)

        notificationViewModel.getNotificationDeleted.observe(viewLifecycleOwner, {
            if (it != 0) {
                delete(it)
                notificationViewModel.onNotificationAfterDelete()
            }
        })

        binding.lifecycleOwner = this

        return binding.root
    }


    fun delete(notificationId: Int) {
        val notificationResponseCall = ApiClient.service.deleteNotification(
            AppData.instance.user.token,
            notificationId
        )
        notificationResponseCall.enqueue((object : Callback<Notification> {
            override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                if (response.isSuccessful) {

                    showSnack("Notification deleted")
                } else {
                    val message = "Notification not deleted"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Notification>, t: Throwable) {
                Log.e("Request failed", "failed deleting notification")
                val message = t.localizedMessage
                if (message!=null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun touchHelper(
        notificationViewModel: NotificationsViewModel,
        adapter: NotificationsAdapter
    ): ItemTouchHelper.SimpleCallback {

        return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                notificationViewModel.onNotificationBeforeDelete(adapter.currentList[position].id)
                notificationViewModel.delete(adapter.currentList[position])
                Log.d("SWIPE", position.toString())
            }

        }
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }
}


