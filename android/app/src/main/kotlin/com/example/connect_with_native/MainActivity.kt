package com.example.connect_with_native

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.BatteryManager
import android.os.Bundle
import android.provider.CallLog
import androidx.core.app.ActivityCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val channelName = "focal"
    private val REQUEST_CALL_LOG_PERMISSION = 1

    private lateinit var channel: MethodChannel // Declare MethodChannel here

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName) // Initialize MethodChannel here
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "getBattery" -> {
                    val batteryLevel = getBatteryLevel()
                    if (batteryLevel != -1) {
                        result.success(batteryLevel)
                    } else {
                        result.error("UNAVAILABLE", "Battery level not available.", null)
                    }
                }
                "getCallLogs" -> {
                    // Check for permission before accessing call logs
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                        // Request permission
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), REQUEST_CALL_LOG_PERMISSION)
                        result.error("PERMISSION_DENIED", "Call log permission not granted.", null)
                    } else {
                        // If permission is granted, fetch call logs
                        val callLogs = getCallLogs()
                        result.success(callLogs)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getCallLogs(): List<Map<String, String>> {
        val callLogsList = mutableListOf<Map<String, String>>()
        val cursor: Cursor? = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val number = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                val type = it.getString(it.getColumnIndex(CallLog.Calls.TYPE))
                val date = it.getString(it.getColumnIndex(CallLog.Calls.DATE))
                val duration = it.getString(it.getColumnIndex(CallLog.Calls.DURATION))

                val callLog = mapOf(
                    "number" to number,
                    "type" to type,
                    "date" to date,
                    "duration" to duration
                )
                callLogsList.add(callLog)
            }
            it.close()
        }
        return callLogsList
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_LOG_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch call logs again
                val callLogs = getCallLogs()
                channel.invokeMethod("getCallLogs", callLogs) // Send call logs back to Flutter
            } else {
                // Permission denied
                channel.invokeMethod("getCallLogs", null) // Or handle appropriately
            }
        }
    }
}
