package com.example.geofencereciever

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

class GeofenceService: Service() {
    private lateinit var geofencingClient: GeofencingClient
    private var TAG: String = "GeofenceService"

    override fun onCreate() {
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        // Add the geofence
        addGeofence()

        return START_STICKY
    }

    private fun addGeofence() {
        val geofenceId = "MyGeofence"
        val latitude = 43.475924
        val longitude = -80.525835
        val radius = 1.0 // in meters

        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(latitude, longitude, radius.toFloat())
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
                        "Geofence is added for latitude: $latitude, longitude: $longitude and radius: $radius"
                    )

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val CHANNEL_ID = "GeofenceServiceChannel"
    }
}