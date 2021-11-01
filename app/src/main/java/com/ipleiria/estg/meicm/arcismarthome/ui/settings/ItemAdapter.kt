package com.ipleiria.estg.meicm.arcismarthome.ui.settings

import com.ipleiria.estg.meicm.arcismarthome.utils.email.GMailSender
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentSettingsBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.User
import com.ipleiria.estg.meicm.arcismarthome.server.ApiClient
import com.ipleiria.estg.meicm.arcismarthome.server.RoleGroupRequest
import kotlinx.android.synthetic.main.accounts_list_item.view.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemAdapter internal constructor(
    val binding: FragmentSettingsBinding,
    private val itemList: MutableList<User>
) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.accounts_list_item, parent, false)

        return ItemViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentUser = itemList[position]

        if (currentUser.photo != null) Glide.with(holder.accountImage.context)
            .load(currentUser.photo).into(holder.accountImage)
        else holder.accountImage.setImageResource(R.drawable.ic_profile_solid)

        holder.accountName.text = currentUser.firstName + " " + currentUser.lastName

        if (AppData.instance.user.role == "admin") {
            holder.accountRoleSpinner.visibility = View.VISIBLE
            holder.accountDelete.visibility = View.VISIBLE
            holder.accountRoleTextView.visibility = View.GONE

            holder.accountRoleSpinner.setSelection(
                holder.accountRoleSpinner.getItemIdAtPosition(currentUser.roleGroup!! - 1).toInt()
            )

            var done = false
            holder.accountRoleSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (!done) done = true
                        else {
                            currentUser.role = holder.accountRoleSpinner.selectedItem.toString()
                            currentUser.updateRoleGroup()

                            updateUserRole(currentUser)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }


            holder.accountDelete.setOnClickListener {
                val altDial = AlertDialog.Builder(binding.root.context)

                val message = String.format(
                    "%s %s %s?\n\n%s: %s!\n%s.",
                    binding.root.context.resources.getString(R.string.qst_delete),
                    currentUser.firstName,
                    currentUser.lastName,
                    binding.root.context.resources.getString(R.string.attention),
                    binding.root.context.resources.getString(R.string.att_delete_recover),
                    binding.root.context.resources.getString(R.string.inf_delete_user)
                )

                altDial.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                        deleteCurrentUser(currentUser)
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

        } else {
            holder.accountRoleTextView.visibility = View.VISIBLE
            holder.accountRoleSpinner.visibility = View.GONE
            holder.accountDelete.visibility = View.GONE

            holder.accountRoleTextView.text = currentUser.role
        }
    }

    override fun getItemCount() = itemList.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountDelete: ImageView = itemView.iv_delete_account
        val accountImage: ImageView = itemView.civ_account_image
        val accountName: TextView = itemView.tv_account_name
        val accountRoleSpinner: Spinner = itemView.spin_account_role
        val accountRoleTextView: TextView = itemView.lb_account_role
    }


    private fun deleteCurrentUser(currentUser: User) {

        val userDeleteResponseCall =
            ApiClient.service.deleteUser(AppData.instance.user.token, currentUser.id!!)
        userDeleteResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    binding.rvHomeAccounts.adapter!!.notifyDataSetChanged()

                    val emailBody = String.format(
                        "%s %s %s,\n\n%s [%s], %s %s, %s %s %s.",
                        binding.root.context.resources.getString(R.string.email_dear),
                        currentUser.firstName,
                        currentUser.lastName,
                        binding.root.context.resources.getString(R.string.email_delete_account_body_1),
                        currentUser.username,
                        binding.root.context.resources.getString(R.string.email_delete_account_body_2),
                        AppData.instance.home.name,
                        binding.root.context.resources.getString(R.string.email_delete_account_body_3),
                        AppData.instance.user.firstName,
                        AppData.instance.user.lastName
                    )

                    sendEmail(
                        currentUser.email!!,
                        binding.root.context.resources.getString(R.string.email_delete_account_subject),
                        emailBody
                    )

                    itemList.remove(currentUser)
                    binding.rvHomeAccounts.adapter?.notifyDataSetChanged()

                    val message = "User deleted successfully"
                    showSnack(message)
                } else {
                    val message = "User not deleted"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", t.localizedMessage!!)
                showSnack("Delete user failed")
            }
        }))
    }

    private fun updateUserRole(currentUser: User) {

        val body = RoleGroupRequest(arrayListOf(currentUser.roleGroup!!))

        val userRoleUpdateResponseCall =
            ApiClient.service.updateUserRole(
                AppData.instance.user.token, currentUser.id!!, body
            )
        userRoleUpdateResponseCall.enqueue((object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {

                    val emailBody = String.format(
                        "%s %s %s,\n\n%s [%s], %s %s, %s.\n\n%s \"%s\" %s.",
                        binding.root.context.resources.getString(R.string.email_dear),
                        currentUser.firstName,
                        currentUser.lastName,
                        binding.root.context.resources.getString(R.string.email_change_account_role_body_1),
                        currentUser.username,
                        binding.root.context.resources.getString(R.string.email_change_account_role_body_2),
                        AppData.instance.home.name,
                        binding.root.context.resources.getString(R.string.email_change_account_role_body_3),
                        binding.root.context.resources.getString(R.string.email_change_account_role_body_4),
                        currentUser.role,
                        binding.root.context.resources.getString(R.string.email_change_account_role_body_5)
                    )

                    sendEmail(
                        currentUser.email!!,
                        binding.root.context.resources.getString(R.string.email_change_account_role_subject),
                        emailBody
                    )

                    val message = "User role updated with success"
                    showSnack(message)
                } else {
                    val message = "User role not updated"
                    showSnack(message)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Request failed", "Update User Role failed")
                val message = t.localizedMessage
                if (message!= null){
                    showSnack(message)
                }
            }
        }))
    }

    private fun sendEmail(receiverEmail: String, subject: String, body: String) {
        Thread {
            try {
                val sender = GMailSender()
                sender.sendMail(subject, body, null, receiverEmail)
            } catch (e: java.lang.Exception) {
                Log.e("SendMail", e.message, e)
            }
        }.start()
    }

    private fun showSnack(message: String){
        val snack = Snackbar.make((binding.root.context as Activity).findViewById(android.R.id.content),message, Snackbar.LENGTH_SHORT)
        snack.setAction("Dismiss", View.OnClickListener { snack.dismiss() })
        snack.show()
    }
}