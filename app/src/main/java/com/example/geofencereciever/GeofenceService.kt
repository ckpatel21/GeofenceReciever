package com.example.geofencereciever

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
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
            GeofenceData("Home", 43.4741777185106, -80.53383571282681, 1000.0), // 43.4741777185106, -80.53383571282681
            GeofenceData("Sunview Stop", 43.47329509289114,  -80.53306424976606, 1000.0), // 43.47329509289114, -80.53306424976606
            GeofenceData("in & Out", 43.47461098007839,  -80.53291602910795, 1000.0), // 43.47461098007839, -80.53291602910795
            GeofenceData("Dough Box", 43.47345377621494,  -80.53199320971105, 1000.0), // 43.47345377621494, -80.53199320971105
            GeofenceData("WLU", 43.474962,   -80.528186, 1000.0), //
            GeofenceData("Starbucks", 43.476183,    -80.525008, 1000.0),
            GeofenceData("Waterloo Park", 43.466182,    -80.525852, 1000.0),
            GeofenceData("Victoria park", 43.446920,    -80.494267, 1000.0),
            GeofenceData("CN Tower", 43.641781,    -79.3864431, 1000.0),
            GeofenceData("Walmart bridgeport", 43.47046265090501,    -80.51550086514048, 1000.0), // 43.47046265090501, -80.51550086514048
            GeofenceData("Mad over spices", 43.47097653982065,    -80.47097653982065, 1000.0), //43.47097653982065, -80.47097653982065
            GeofenceData("Fairview Mall", 43.42436305370834,    -80.43890218048493, 1000.0), // 43.42436305370834, -80.43890218048493
            GeofenceData("Royal paan", 43.47655169237212,    -80.5230256352308, 1000.0), //43.47655169237212, -80.5230256352308
            GeofenceData("Grand mehfil", 43.4334826692154,    -80.43964414907781, 1000.0), // 43.4334826692154, -80.43964414907781
            GeofenceData("Paranthe wali gali", 43.433591196864086,    -80.42187312024573, 1000.0), // 43.433591196864086, -80.42187312024573
            GeofenceData("Indian store at Fairview", 43.42059769530186,    -80.42059769530186, 1000.0), // 43.42059769530186, -80.42059769530186
            GeofenceData("132 rogers st", 43.46199764435904,    -80.46199764435904, 1000.0), // 43.46199764435904, -80.46199764435904
            GeofenceData("Laurier-Waterloo Park", 43.46890059497084,    -80.5356392705021, 1000.0), // 43.46890059497084, -80.5356392705021
            // Add more geofences as needed
        )

        addGeofence(geofenceDataList)

        return START_STICKY
    }



    private fun addGeofence(geofenceDataList: List<GeofenceData>) {

//        val geofenceId = "My home"
//        val latitude = 43.474264
//        val longitude = -80.533522
//        val radius = 100.0 // in meters

//        val geofence = Geofence.Builder()
//            .setRequestId(geofenceData.geofenceId)
//            .setCircularRegion(geofenceData.latitude, geofenceData.longitude, geofenceData.radius.toFloat())
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
//            .setExpirationDuration(Geofence.NEVER_EXPIRE)
//            .build()


        val geofenceList = geofenceDataList.map { geofenceData ->
            Geofence.Builder()
                .setRequestId(geofenceData.geofenceId)
                .setCircularRegion(
                    geofenceData.latitude,
                    geofenceData.longitude,
                    geofenceData.radius.toFloat()
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
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
                    for (geofenceData in geofenceDataList) {
                        writeLogToFile(
                            TAG,
                            "Geofence is added for latitude: ${geofenceData.latitude}, longitude: ${geofenceData.longitude}, and radius: ${geofenceData.radius}"
                        )
                    }

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val CHANNEL_ID = "GeofenceServiceChannel"
    }
}