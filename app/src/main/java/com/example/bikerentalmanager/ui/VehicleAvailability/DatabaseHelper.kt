package com.example.bikerentalmanager.ui.VehicleAvailability

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Vehicles table creation
        db.execSQL("CREATE TABLE vehicles (id INTEGER PRIMARY KEY, name TEXT, type TEXT, image TEXT, status TEXT, start_date TEXT, end_date TEXT)")

        // Upcoming bookings table creation
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS upcoming_bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "bike_name TEXT NOT NULL, " +
                    "image_url TEXT, " +
                    "date TEXT NOT NULL)"
        )

        populateDatabase(db)
    }

    private fun populateDatabase(db: SQLiteDatabase) {
        // Existing vehicles data
        val vehicles = listOf(
            arrayOf("GT 650", "Bike", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/royal-enfield-select-model-mr-clean-1727349017340.jpg?q=80&wm=3"),
            arrayOf("Bajaj Pulsar", "Bike", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/185667/pulsar-150-right-side-view-9.jpeg?isig=0&q=80&wm=3"),
            arrayOf("Royal Enfield Classic", "Bike", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/183389/classic-350-right-side-view-62.jpeg?isig=0&q=80&wm=3"),
            arrayOf("KTM Duke", "Bike", "https://imgd.aeplcdn.com/664x374/n/bw/models/colors/ktm-select-model-atlantic-blue-1694407102580.png?q=80"),
            arrayOf("Yamaha MT-15", "Bike", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/yamaha-select-model-metallic-black-2023-1680847548270.png?q=80&wm=3"),
            arrayOf("Suzuki Gixxer", "Bike", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/suzuki-select-model-glass-sparkle-black-1671516577831.png?q=80&wm=3"),
            arrayOf("TVS Apache RTR", "Bike", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/tvs-select-model-pearl-white-1697704917523.png?q=80&wm=3"),
            arrayOf("Interceptor", "Bike", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/royal-enfield-select-model-cali-green-1727351655663.jpg?q=80&wm=3"),
            arrayOf("Kawasaki Ninja", "Bike", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/149821/ninja-400-right-side-view-3.png?isig=0&q=80&wm=3"),
            arrayOf("Hunter", "Bike", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/124013/hunter-350-right-side-view-5.png?isig=0&q=80&wm=3"),
            // Scooters
            arrayOf("Honda Activa", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/honda-select-model-pearl-precious-white-1674535479295.png?q=80&wm=3"),
            arrayOf("TVS Jupiter", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/tvs-select-model-starlight-blue-gloss-1725460962533.jpg?q=80&wm=3"),
            arrayOf("Hero Maestro", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/49454/maestro-right-side-view-2.png?isig=0&q=80&wm=3"),
            arrayOf("Suzuki Access", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/suzuki-select-model-metallic-matte-black-se-1727435919240.jpg?q=80&wm=3"),
            arrayOf("Honda Dio", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/150373/dio-right-side-view.png?isig=0&q=80&wm=3"),
            arrayOf("Yamaha Fascino", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/yamaha-select-model-yellow-cocktaildrum-1712697848585.png?q=80&wm=3"),
            arrayOf("Yamaha Vespa", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/vespa-select-model-blue-1683718920804.jpg?q=80&wm=3"),
            arrayOf("Bajaj Chetak", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/bajaj-select-model-racing-red-1725626169063.jpg?q=80&wm=3"),
            arrayOf("Ola S1", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/cw/ec/155297/s1-air-right-front-three-quarter-8.png?isig=0&q=80&wm=3"),
            arrayOf("Ather 450X", "Scooter", "https://imgd.aeplcdn.com/1056x594/n/bw/models/colors/ather-select-model-cosmic-black-1709794603634.png?q=80&wm=3"),

            )

        for (vehicle in vehicles) {
            val name = vehicle[0]
            val type = vehicle[1]
            val image = vehicle[2]
            val status = "available"
            db.execSQL("INSERT INTO vehicles (name, type, image, status) VALUES ('$name', '$type', '$image', '$status')")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS vehicles")
        db.execSQL("DROP TABLE IF EXISTS upcoming_bookings")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "vehicles.db"
    }

    // Method to insert a booking into upcoming_bookings
    fun addUpcomingBooking(bikeName: String, imageUrl: String, date: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("bike_name", bikeName)
            put("image_url", imageUrl)
            put("date", date)
        }
        db.insert("upcoming_bookings", null, values)
        db.close()
    }

    // Method to retrieve upcoming bookings
    fun getUpcomingBookings(): List<Map<String, String>> {
        val bookings = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM upcoming_bookings", null)
        if (cursor.moveToFirst()) {
            do {
                val booking = mapOf(
                    "bike_name" to cursor.getString(cursor.getColumnIndexOrThrow("bike_name")),
                    "image_url" to cursor.getString(cursor.getColumnIndexOrThrow("image_url")),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )
                bookings.add(booking)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bookings
    }
    fun getVehicleNames(): List<String> {
        val vehicleNames = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT name FROM vehicles", null)
        if (cursor.moveToFirst()) {
            do {
                vehicleNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return vehicleNames
    }
    fun updateVehicleStatus(vehicleId: Int, status: String, startDate: String?, endDate: String?) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("status", status)
            if (startDate != null) put("start_date", startDate)
            if (endDate != null) put("end_date", endDate)
        }
        db.update("vehicles", values, "id = ?", arrayOf(vehicleId.toString()))
        db.close()
    }
}