package com.example.bikerentalmanager.ui.VehicleAvailability

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikerentalmanager.R
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class VehicleAvailabilityFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var availableButton: Button
    private lateinit var rentedButton: Button
    private lateinit var upcomingButton: Button
    private lateinit var setNotificationButton: Button
    private var vehicleList = mutableListOf<Vehicle>()
    private lateinit var upcomingBookingsRecyclerView: RecyclerView
    private lateinit var upcomingBookingsAdapter: UpcomingBookingsAdapter
    private val upcomingBookingsList = mutableListOf<UpcomingBooking>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_vehicleavailability, container, false)
        checkAndRequestNotificationPermission()

        databaseHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.recycler_view)
        searchBar = view.findViewById(R.id.search_bar)
        availableButton = view.findViewById(R.id.button_available)
        rentedButton = view.findViewById(R.id.button_rented)
        upcomingButton = view.findViewById(R.id.button_upcoming)
        setNotificationButton = view.findViewById(R.id.setNotificationButton)
        upcomingBookingsRecyclerView = view.findViewById(R.id.upcomingBookingsRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        upcomingBookingsRecyclerView.layoutManager = LinearLayoutManager(context)
        upcomingBookingsAdapter = UpcomingBookingsAdapter(upcomingBookingsList)
        upcomingBookingsRecyclerView.adapter = upcomingBookingsAdapter

        loadAvailableVehicles()

        availableButton.setOnClickListener {
            showAvailableVehicles()
        }

        rentedButton.setOnClickListener {
            loadRentedVehicles()
        }

        upcomingButton.setOnClickListener {
            showUpcomingBookingView()
        }

        setNotificationButton.setOnClickListener {
            showNotificationDialog()
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchVehicles(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun showAvailableVehicles() {
        loadAvailableVehicles()
        recyclerView.visibility = View.VISIBLE
        setNotificationButton.visibility = View.GONE
        upcomingBookingsRecyclerView.visibility = View.GONE
    }

    private fun showUpcomingBookingView() {
        recyclerView.visibility = View.GONE
        setNotificationButton.visibility = View.VISIBLE
        upcomingBookingsRecyclerView.visibility = View.VISIBLE
        loadUpcomingBookings()
    }

    private fun loadAvailableVehicles() {
        vehicleList.clear()
        val cursor: Cursor = databaseHelper.readableDatabase.rawQuery("SELECT * FROM vehicles WHERE status = 'available'", null)
        while (cursor.moveToNext()) {
            vehicleList.add(Vehicle(
                cursor.getString(1), // name
                cursor.getString(2), // type
                cursor.getString(3), // image
                cursor.getString(4)  // status
            ))
        }
        cursor.close()
        recyclerView.adapter = VehicleAdapter(vehicleList)
    }

    private fun loadRentedVehicles() {
        vehicleList.clear()
        val cursor: Cursor = databaseHelper.readableDatabase.rawQuery("SELECT * FROM vehicles WHERE status = 'rented'", null)
        while (cursor.moveToNext()) {
            vehicleList.add(Vehicle(
                cursor.getString(1), // name
                cursor.getString(2), // type
                cursor.getString(3), // image
                cursor.getString(4)  // status
            ))
        }
        cursor.close()
        recyclerView.adapter = VehicleAdapter(vehicleList)
    }

    private fun loadUpcomingBookings() {
        upcomingBookingsList.clear()
        val cursor: Cursor = databaseHelper.readableDatabase.rawQuery("SELECT * FROM upcoming_bookings", null)
        while (cursor.moveToNext()) {
            upcomingBookingsList.add(UpcomingBooking(
                cursor.getString(1), // bike name
                cursor.getString(2)  // date
            ))
        }
        cursor.close()
        upcomingBookingsAdapter.notifyDataSetChanged()
    }

    private fun showNotificationDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_notification, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Set Notification")

        val bikeNameSpinner = dialogView.findViewById<Spinner>(R.id.bikeNameSpinner)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)

        val bikeNames = databaseHelper.getVehicleNames()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bikeNames)
        bikeNameSpinner.adapter = adapter

        dialogBuilder.setPositiveButton("Set") { _, _ ->
            val bikeName = bikeNameSpinner.selectedItem.toString()
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

//            setNotification(bikeName, calendar.timeInMillis)
            saveUpcomingBooking(bikeName, "$day-${month + 1}-$year")

            // Display Snackbar for successful notification
            Snackbar.make(requireView(), "Notification successfully created!", Snackbar.LENGTH_SHORT).show()
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.create().show()
    }

    private fun searchVehicles(query: String) {
        vehicleList.clear()
        val cursor: Cursor = databaseHelper.readableDatabase.rawQuery("SELECT * FROM vehicles WHERE name LIKE ?", arrayOf("%$query%"))
        while (cursor.moveToNext()) {
            vehicleList.add(Vehicle(
                cursor.getString(1), // name
                cursor.getString(2), // type
                cursor.getString(3), // image
                cursor.getString(4)  // status
            ))
        }
        cursor.close()
        recyclerView.adapter = VehicleAdapter(vehicleList)
    }

    private fun setNotification(bikeName: String, timeInMillis: Long) {
        val upcomingBooking = UpcomingBooking(bikeName, "Selected Date")
        upcomingBookingsList.add(upcomingBooking)
        upcomingBookingsAdapter.notifyDataSetChanged()

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleAlarm(bikeName, timeInMillis)
            }
        } else {
            scheduleAlarm(bikeName, timeInMillis)
        }
    }

    private fun scheduleAlarm(bikeName: String, timeInMillis: Long) {
        val notificationIntent = Intent(requireContext(), NotificationReceiver::class.java)
        notificationIntent.putExtra("bikeName", bikeName)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
            }
        }
    }

    companion object {
        const val NOTIFICATION_PERMISSION_CODE = 1001
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
        }
    }
    private fun saveUpcomingBooking(bikeName: String, date: String) {
        // Save booking in the list and update the RecyclerView
        upcomingBookingsList.add(UpcomingBooking(bikeName, date))
        upcomingBookingsAdapter.notifyDataSetChanged()
    }
}
