package com.example.bikerentalmanager.ui.VehicleAvailability

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikerentalmanager.R

class UpcomingBookingsAdapter(private val bookings: List<UpcomingBooking>) :
    RecyclerView.Adapter<UpcomingBookingsAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bikeNameTextView: TextView = itemView.findViewById(R.id.bikeNameTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_booking, parent, false) // Your item layout
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bikeNameTextView.text = booking.bikeName
        holder.dateTextView.text = booking.date
    }

    override fun getItemCount(): Int = bookings.size
}
