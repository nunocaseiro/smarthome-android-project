package com.ipleiria.estg.meicm.arcismarthome.ui.house

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.ipleiria.estg.meicm.arcismarthome.R
import com.ipleiria.estg.meicm.arcismarthome.databinding.ExpandableChildBinding
import com.ipleiria.estg.meicm.arcismarthome.databinding.ExpandableGroupBinding
import com.ipleiria.estg.meicm.arcismarthome.databinding.FragmentHouseBinding
import com.ipleiria.estg.meicm.arcismarthome.models.AppData
import com.ipleiria.estg.meicm.arcismarthome.models.Room
import com.ipleiria.estg.meicm.arcismarthome.models.Sensor
import java.util.*
import kotlin.collections.HashMap

class ExpandableListAdapter(
    var context: Context,
    var header: List<Room>,
    var body: HashMap<Room, LinkedList<Sensor>>,
    var binding: FragmentHouseBinding
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return header.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return body[header[groupPosition]]?.size!!
    }

    override fun getGroup(groupPosition: Int): Room {
        return header[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Sensor {
        return body[header[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DataBindingUtil.inflate<ExpandableGroupBinding>(
            inflater,
            R.layout.expandable_group,
            parent,
            false
        )

        val room = getGroup(groupPosition)
        binding.room = room

        if(AppData.instance.user.role == "admin") {
            binding.ivEdit.visibility = View.VISIBLE

            binding.ivEdit.setOnClickListener {
                if (room.id != null)
                    it.findNavController().navigate(
                        HouseFragmentDirections.actionNavigationHouseToRoomAddEditFragment(room.id!!)
                    )
            }
        }
        else {
            binding.ivEdit.visibility = View.GONE
        }

        return binding.root
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val ex = DataBindingUtil.inflate<ExpandableChildBinding>(
            LayoutInflater.from(parent?.context),
            R.layout.expandable_child,
            parent,
            false
        )

        val child = getChild(groupPosition, childPosition)
        ex.sensor = child

        ex.tvSAStatus.visibility = View.VISIBLE
        if (child.sensortype == "luminosity" || child.sensortype == "temperature") {
            ex.tvSAValue.visibility = View.VISIBLE
        } else {
            ex.tvSAValue.visibility = View.INVISIBLE
        }

        ex.ivSAIcon.setImageResource(child.icon)

        return ex!!.root
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}