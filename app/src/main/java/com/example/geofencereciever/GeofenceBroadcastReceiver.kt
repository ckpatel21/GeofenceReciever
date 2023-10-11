package com.example.geofencereciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.writeLogToFile(TAG, "onReceive")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            context.writeLogToFile(TAG, errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach {
                val transitionType = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "entered" else "exited"
                Toast.makeText(context, "$transitionType  ${it.requestId}", Toast.LENGTH_SHORT).show()
                context.writeLogToFile(
                    TAG,
                    "Triggered $transitionType geofence for ${it.requestId} latitude: ${it.latitude}, longitude: ${it.longitude}, and radius: ${it.radius}"
                )
            }


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