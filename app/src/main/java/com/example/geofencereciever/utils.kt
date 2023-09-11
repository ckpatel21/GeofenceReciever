package com.example.geofencereciever

import android.content.Context
import android.util.Log
import java.io.File

fun Context.writeLogToFile(tag: String, logMessage: String) {
    val logFileName = "app_logs.txt"
    val logFile = File(filesDir, logFileName)

    try {
        when {
            logFile.exists() -> logFile.appendText("\n[$tag] : \n$logMessage")
            else -> logFile.writeText("[$tag] : $logMessage")
        }
    } catch (e: Exception) {
        Log.e("LogFile", "Failed to write log file: ${e.message}")
    }
}