package com.example.geofencereciever

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CircleOptions

class GeofenceService: Service() {
    private lateinit var geofencingClient: GeofencingClient
    private var TAG: String = "GeofenceService"

    override fun onCreate() {
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "Geofences")
        val notificationIntent = Intent(this, applicationContext::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "My geo fence service",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(notificationChannel)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Geofence Service")
            .setContentText("Monitoring geofence")
            .setContentIntent(pendingIntent)
            .build()

        // Start the service in the foreground
        startForeground(1, notification)

        val geofenceDataList = listOf(
            GeofenceData("Home", 43.474201, -80.533469, 100.0),
            GeofenceData("Location1", 43.473447,  -80.532898, 1000.0),
            GeofenceData("Location2", 43.472968,  -80.532411, 1000.0),
            GeofenceData("Location3", 43.471099,  -80.537939, 1000.0),
            GeofenceData("WLU", 43.474962,   -80.528186, 1000.0),
            GeofenceData("Starbucks", 43.476183,    -80.525008, 1000.0),
            GeofenceData("Waterloo Park", 43.466182,    -80.525852, 1000.0),
            GeofenceData("Victoria park", 43.446920,    -80.494267, 1000.0),
            GeofenceData("CN Tower", 43.641781,    -79.3864431, 1000.0),
            // Add more geofences as needed
        )

            for (geofenceData in geofenceDataList) {
                addGeofence(geofenceData)
            }

        // Add the geofence
//        addGeofence()

        return START_STICKY
    }



    private fun addGeofence(geofenceData: GeofenceData) {
//        val geofenceId = "My home"
//        val latitude = 43.474264
//        val longitude = -80.533522
//        val radius = 100.0 // in meters

        val geofence = Geofence.Builder()
            .setRequestId(geofenceData.geofenceId)
            .setCircularRegion(geofenceData.latitude, geofenceData.longitude, geofenceData.radius.toFloat())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val geofencePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, GeofenceBroadcastReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    // You can perform additional actions if needed
                    writeLogToFile(
                        TAG,
                        "Geofence is added for latitude: $geofenceData.latitude, longitude: $geofenceData.longitude and radius: $geofenceData.radius"
                    )

//                    drawCircleOnMap(latitude,longitude)
                    Toast.makeText(applicationContext, "Added", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    // You can handle the failure here
                    it.printStackTrace()
                    writeLogToFile(
                        TAG,
                        "Failed to add geofence with error: ${it.message}"
                    )
                    Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val sum: (Int, Int) -> Int = { x: Int, y: Int ->
            x + y
        }

    }
    data class GeofenceData(val geofenceId: String, val latitude: Double, val longitude: Double, val radius: Double)

//    private fun drawCircleOnMap(latitude: Double, longitude: Double) {
//        val circleOptions = CircleOptions()
//            .center()
//            .radius(RADIUS.toDouble())
//            .fillColor(0x40ff0000)
//            .strokeColor(Color.BLUE)
//            .strokeWidth(2f)
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
//        mMap.addMarker(MarkerOptions().position(latLng))
//        mMap.addCircle(circleOptions)
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val CHANNEL_ID = "GeofenceServiceChannel"
    }
}