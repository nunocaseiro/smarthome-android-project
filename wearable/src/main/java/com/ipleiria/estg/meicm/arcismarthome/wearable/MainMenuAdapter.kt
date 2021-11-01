package com.ipleiria.estg.meicm.arcismarthome.wearable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ipleiria.estg.meicm.arcismarthome.wearable.models.Sensor
import java.util.*

class MainMenuAdapter(private val context: Context, private val dataSource: ArrayList<Sensor>,  private val cellClickListener: CellClickListener) :
    RecyclerView.Adapter<RecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_menu_item, parent, false)
        return RecyclerViewHolder(view)
    }

    @SuppressLint("ShowToast")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val dataProvider = dataSource[position]
        val isSensor = dataProvider.sensortype == "temperature" || dataProvider.sensortype == "luminosity" || dataProvider.sensortype == "motion"
        holder.menuItem.text = dataProvider.name
        holder.menuIcon.setImageResource(dataProvider.icon)

        if(isSensor) {
            holder.status.setBackgroundColor(context.getColor(R.color.ARCIBlue))
        }
        else {
            if (dataProvider.value == 1.0) holder.status.setBackgroundColor(context.getColor(R.color.green))
            else holder.status.setBackgroundColor(context.getColor(R.color.red))
        }

        holder.itemView.setOnClickListener {
            if(isSensor) Toast.makeText(context, "This sensor is OK", Toast.LENGTH_SHORT)
            else cellClickListener.onCellClickListener(dataSource[position])
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    interface CellClickListener {
        fun onCellClickListener(sensor: Sensor)
    }
}