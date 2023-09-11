package com.example.geofencereciever

import android.os.Bundle
import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val BACKGROUND_LOCATION_PERMISSION_CODE: Int = 888
    private val LOCATION_PERMISSION_CODE: Int = 999
    private val TAG = MainActivity::class.simpleName.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        writeLogToFile(TAG, "==============================================")
        writeLogToFile(TAG, "App is launched")
        checkPermission()
    }

    private fun checkPermission() {
        writeLogToFile(TAG, "Checking location permission")

        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Fine Location permission is granted
            writeLogToFile(TAG, "Fine Location permission is granted")
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    writeLogToFile(TAG, "Background Location Permission is granted")
                    // Background Location Permission is granted so do your work here
                    startService(Intent(this, GeofenceService::class.java))
                } else {
                    // Ask for Background Location Permission
                    askPermissionForBackgroundUsage()
                }
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
            askForLocationPermission()
        }
    }

    private fun askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), LOCATION_PERMISSION_CODE
                    )
                }
                .setNegativeButton("CANCEL") { _, _ ->
                    // Permission is denied by the user
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            writeLogToFile(TAG, "Background Location Permission Needed... Showing alert dialog")
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        writeLogToFile(TAG, "Launching App Info page for getting background location permission")
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), BACKGROUND_LOCATION_PERMISSION_CODE
                        )
                    }
                }
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                    // User declined for Background Location Permission.
                })
                .create().show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted location permission
                // Now check if android version >= 11, if >= 11 check for Background Location Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Background Location Permission is granted so do your work here
                        startService(Intent(this, GeofenceService::class.java))
                    } else {
                        // Ask for Background Location Permission
                        askPermissionForBackgroundUsage()
                    }
                }
            } else {
                // User denied location permission
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
                startService(Intent(this, GeofenceService::class.java))
            } else {
                // User declined for Background Location Permission.
                askPermissionForBackgroundUsage()
            }
        }
    }
}