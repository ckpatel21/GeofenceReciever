package com.example.geofencereciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    private lateinit var mGoogleMap: GoogleMap

    override fun onReceive(context: Context, intent: Intent) {
        context.writeLogToFile(TAG, "onReceive")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            context.writeLogToFile(TAG, errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach {
                context.writeLogToFile(
                    TAG,
                    "Triggered geofence for latitude: ${it.latitude}, longitude: ${it.longitude}, and radius: ${it.radius}"
                )
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    // Show a marker on the map when entering the geofence
                    val marker = MarkerOptions()
                        .position(LatLng(it.latitude, it.longitude))
                        .title("Geofence Location")
                    // Assuming you have a reference to your map, mGoogleMap
                    mGoogleMap?.addMarker(marker)
                }
            }
            Toast.makeText(context, "Geofence transition: $geofenceTransition", Toast.LENGTH_SHORT)
                .show()


            context.writeLogToFile(TAG, "Geofence transition: $geofenceTransition")
            context.writeLogToFile(TAG, "Geofence event finished")
            context.writeLogToFile(TAG, "==============================================")
        } else {
            context.writeLogToFile(TAG, "Invalid geofenceTransition= $geofenceTransition")
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }


}