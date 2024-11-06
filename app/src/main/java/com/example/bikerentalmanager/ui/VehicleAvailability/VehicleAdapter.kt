package com.example.bikerentalmanager.ui.VehicleAvailability

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bikerentalmanager.R

class VehicleAdapter(private val vehicleList: List<Vehicle>) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vehicleName: TextView = view.findViewById(R.id.vehicle_name)
        val vehicleImage: ImageView = view.findViewById(R.id.vehicle_image)
        val rentNowButton: Button = view.findViewById(R.id.rent_now_button) // Added rent button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicleList[position]
        holder.vehicleName.text = vehicle.name

        // Load image using Glide with error handling and placeholder
        Glide.with(holder.itemView.context)
            .load(vehicle.image)
            .placeholder(R.drawable.baseline_downloading_24) // Ensure these images are in drawable
            .error(R.drawable.baseline_two_wheeler_24)
            .into(holder.vehicleImage)

        // Rent Now button functionality can be added here if needed
    }

    override fun getItemCount(): Int = vehicleList.size
}
